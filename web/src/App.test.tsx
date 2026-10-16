import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { strToU8, zipSync } from "fflate";
import { describe, expect, it } from "vitest";
import { App } from "./App";
import { DEMO_CORPUS } from "./mocks/corpus";

const queueUrl = "/batches/demo-hw4/queue?threshold=45&k=5";

function demoArchive() {
  const names = ["ivanov", "petrov", "sidorova", "kuznetsov", "smirnova", "popov", "volkova", "sokolov"];
  const zip = zipSync(Object.fromEntries(DEMO_CORPUS.map((s, i) => [`hw4/${names[i]}/${s.fileName}`, strToU8(s.code)])));
  return new File([zip], "hw4.zip", { type: "application/zip" });
}

describe("сценарий проверяющего end-to-end (на моках)", () => {
  it("очередь: самая рискованная пара сверху, кластеры, дисклеймер", async () => {
    render(<App initialEntries={[queueUrl]} />);
    const rows = within(await screen.findByRole("table")).getAllByRole("row").slice(1);
    expect(rows).toHaveLength(28);
    expect(rows[0]).toHaveTextContent("S-014");
    expect(rows[0]).toHaveTextContent("S-027");
    expect(screen.getByText(/Кластер 1/)).toBeInTheDocument();
    expect(screen.getByText("Риск — не вердикт.")).toBeInTheDocument();
  });

    it("сравнение: два решения пары рядом и переход к следующей", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={[queueUrl]} />);
    await user.click(await screen.findByRole("row", { name: /S-014 и S-027/ }));

    expect(await screen.findByRole("heading", { name: "S-014 ↔ S-027" })).toBeInTheDocument();
    expect(screen.getByTestId("pane-A")).toHaveTextContent("boolean[] visited");
    expect(screen.getByTestId("pane-B")).toHaveTextContent("boolean[] seen");

    await user.click(screen.getByRole("button", { name: "Следующая пара" }));
    expect(await screen.findByRole("heading", { name: "S-008 ↔ S-019" })).toBeInTheDocument();
  });

  it("загрузка: отклоняет не-zip до отправки", async () => {
    const user = userEvent.setup({ applyAccept: false });
    render(<App initialEntries={["/upload"]} />);
        await screen.findByRole("heading", { name: "Загрузка архива с работами" });
    await user.upload(screen.getByTestId("archive-input"), new File(["x"], "notes.txt"));
    expect(screen.getByRole("alert")).toHaveTextContent("notes.txt: нужен архив .zip");
    expect(screen.getByRole("button", { name: "Запустить анализ" })).toBeDisabled();
  });

  it("загрузка: показывает ошибку api для битого архива", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={["/upload"]} />);
        await screen.findByRole("heading", { name: "Загрузка архива с работами" });
    await user.upload(screen.getByTestId("archive-input"), new File(["not a zip"], "broken.zip"));
    await user.click(screen.getByRole("button", { name: "Запустить анализ" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Файл не похож на zip-архив");
  });

    it("загрузка: отклоняет архив, где меньше 5 работ", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={["/upload"]} />);
        await screen.findByRole("heading", { name: "Загрузка архива с работами" });
    const zip = zipSync(Object.fromEntries(DEMO_CORPUS.slice(0, 3).map((s, i) => [`hw/${i}/${s.fileName}`, strToU8(s.code)])));
    await user.upload(screen.getByTestId("archive-input"), new File([zip], "small.zip"));
    await user.click(screen.getByRole("button", { name: "Запустить анализ" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("В архиве файлов .java: 3. Нужно от 5 до 50");
  });

  it("загрузка архива: работы псевдонимизированы, ФИО из имён папок не видны", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={["/upload"]} />);
        await screen.findByRole("heading", { name: "Загрузка архива с работами" });
    await user.upload(screen.getByTestId("archive-input"), demoArchive());
    await user.click(screen.getByRole("button", { name: "Запустить анализ" }));
        expect(await screen.findByRole("heading", { name: "hw4" })).toBeInTheDocument();
    expect(screen.getByText("S-001")).toBeInTheDocument();
    expect(screen.getByText("work_001.java")).toBeInTheDocument();
    expect(document.body.textContent).not.toMatch(/ivanov|petrov|sidorova/);
  });
});