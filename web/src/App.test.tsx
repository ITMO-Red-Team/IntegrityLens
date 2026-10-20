import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { strToU8, zipSync } from "fflate";
import { describe, expect, it } from "vitest";
import { App } from "./App";
import { DEMO_CORPUS } from "./mocks/corpus";
import { DEMO_BATCH_ID, demoItems, demoSubmissionId, listSubmissions } from "./mocks/db";

const queueUrl = `/batches/${DEMO_BATCH_ID}/queue?method=jplag&threshold=45&k=5`;

function uiLabel(corpusId: string) {
  const id = demoSubmissionId(corpusId);
  const i = listSubmissions(DEMO_BATCH_ID)!.findIndex((s) => s.id === id);
  return `S-${String(i + 1).padStart(3, "0")}`;
}

function zipOf(items: { originalName: string; code: string }[], name = "hw4.zip") {
  const zip = zipSync(Object.fromEntries(items.map((s) => [s.originalName, strToU8(s.code)])));
  return new File([zip], name, { type: "application/zip" });
}

describe("сценарий проверяющего end-to-end (на моках api)", () => {
  it("очередь: копия с переименованием сверху, кластеры, дисклеймер", async () => {
    render(<App initialEntries={[queueUrl]} />);
    const rows = within(await screen.findByRole("table")).getAllByRole("row").slice(1);
    expect(rows).toHaveLength(28);
    const [x, y] = [uiLabel("S-014"), uiLabel("S-027")].sort();
    expect(rows[0]).toHaveTextContent(new RegExp(`${x}.*${y}|${y}.*${x}`));
    expect(await screen.findByText(/Кластер 2/)).toBeInTheDocument();
    expect(within(screen.getByLabelText("Сводка")).getByText("К проверке").parentElement).toHaveTextContent("4");
    expect(screen.getByText("Оценка — не вердикт.")).toBeInTheDocument();
  });

  it("сравнение: два исходника рядом, имя файла скрыто до клика, переход к следующей паре", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={[queueUrl]} />);
    const firstRow = within(await screen.findByRole("table")).getAllByRole("row")[1]!;
    await user.click(firstRow);

    await waitFor(() => expect(screen.getByTestId("pane-A")).toHaveTextContent(/boolean\[\] (visited|seen)/));
    expect(screen.getByTestId("pane-B")).toHaveTextContent(/boolean\[\] (visited|seen)/);
    expect(document.body.textContent).not.toMatch(/ivanov|petrov/);
    await user.click(screen.getAllByRole("button", { name: "показать имя файла" })[0]!);
    expect(screen.getByText(/hw4\/\w+\/Main\.java/)).toBeInTheDocument();

    // Кнопка переключения участков — слева от выбора пары.
    const toggle = screen.getByRole("button", { name: "Скрыть подозрительные участки" });
    const select = screen.getByRole("combobox", { name: "Выбрать пару" });
    expect(toggle.compareDocumentPosition(select) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();

    // Клик по участку ставит его в фокус (подсветка region-active).
    const region = (await screen.findAllByTestId("region"))[0]!;
    const group = region.getAttribute("data-group")!;
    await user.click(region);
    await waitFor(() => {
      const focused = screen.getAllByTestId("region").find((r) => r.getAttribute("data-group") === group);
      expect(focused).toHaveClass("region-active");
    });

    const title = screen.getByRole("heading", { level: 1 }).textContent;
    await user.click(screen.getByRole("button", { name: "Следующая пара" }));
    await waitFor(() => expect(screen.getByRole("heading", { level: 1 }).textContent).not.toBe(title));
    // Фокус региона не должен переживать смену пары: ни один регион не активен.
    await waitFor(() => {
      const regions = screen.queryAllByTestId("region");
      expect(regions.every((r) => !r.classList.contains("region-active"))).toBe(true);
    });
  });

  it("сравнение: hover имеет приоритет над focus и не затемняет весь код", async () => {
    const user = userEvent.setup();
    // Пара S-008 vs S-019 имеет несколько подозрительных участков (3 группы),
    // что позволяет проверить приоритет hover над focus между разными группами.
    const [aid, bid] = [demoSubmissionId("S-008"), demoSubmissionId("S-019")].sort();
    const pairUrl = `/batches/${DEMO_BATCH_ID}/pairs/${DEMO_BATCH_ID}:${aid}:${bid}?method=jplag&threshold=45&k=5`;
    render(<App initialEntries={[pairUrl]} />);
    await waitFor(() => expect(screen.getByTestId("pane-A")).toHaveTextContent(/ArrayDeque|HashSet/));

    const regions = await screen.findAllByTestId("region");
    // Берём два участка из разных групп.
    const first = regions[0]!;
    const firstGroup = first.getAttribute("data-group")!;
    const second = regions.find((r) => r.getAttribute("data-group") !== firstGroup)!;
    expect(second).toBeDefined();

    // Закрепляем первый блок.
    await user.click(first);
    await waitFor(() => expect(first).toHaveClass("region-active"));

    // Наводим на блок другой группы — он становится активным, а первый — нет.
    await user.hover(second);
    await waitFor(() => {
      expect(second).toHaveClass("region-active");
      expect(first).not.toHaveClass("region-active");
    });
  });

  it("сравнение: клик вне участка сбрасывает фокус", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={[queueUrl]} />);
    const firstRow = within(await screen.findByRole("table")).getAllByRole("row")[1]!;
    await user.click(firstRow);
    await waitFor(() => expect(screen.getByTestId("pane-A")).toHaveTextContent(/boolean\[\] (visited|seen)/));

    const region = (await screen.findAllByTestId("region"))[0]!;
    await user.click(region);
    await waitFor(() => expect(region).toHaveClass("region-active"));

    // Клик по коду вне участка (сама панель) сбрасывает фокус.
    await user.click(screen.getByTestId("pane-A"));
    await waitFor(() => expect(region).not.toHaveClass("region-active"));
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
    expect(await screen.findByRole("alert")).toHaveTextContent("Файл не является корректным ZIP-архивом");
  });

  it("загрузка: меньше 5 работ — ошибка api", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={["/upload"]} />);
    await screen.findByRole("heading", { name: "Загрузка архива с работами" });
    await user.upload(screen.getByTestId("archive-input"), zipOf(demoItems().slice(0, 3), "small.zip"));
    await user.click(screen.getByRole("button", { name: "Запустить анализ" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Требуется от 5 до 50 работ");
  });

  it("загрузка: лишние файлы в архиве перечислены в ошибке", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={["/upload"]} />);
    await screen.findByRole("heading", { name: "Загрузка архива с работами" });
    const items = [...demoItems(), { originalName: "hw4/readme.txt", code: "hi" }];
    await user.upload(screen.getByTestId("archive-input"), zipOf(items));
    await user.click(screen.getByRole("button", { name: "Запустить анализ" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Допустимы только файлы .java: hw4/readme.txt");
  });

  it("загрузка архива: страница пакета с методами и работами под псевдонимами", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={["/upload"]} />);
    await screen.findByRole("heading", { name: "Загрузка архива с работами" });
    await user.upload(screen.getByTestId("archive-input"), zipOf(demoItems(), "hw4-repeat.zip"));
    await user.click(screen.getByRole("button", { name: "Запустить анализ" }));
    expect(await screen.findByRole("heading", { name: "hw4-repeat" })).toBeInTheDocument();
    expect(await screen.findByText("S-001")).toBeInTheDocument();
    expect(screen.getByText(/JPlag/)).toBeInTheDocument();
    expect(screen.getByText(/structsim/)).toBeInTheDocument();
    expect(document.body.textContent).not.toMatch(/ivanov|petrov|sidorova/);
    expect(DEMO_CORPUS).toHaveLength(8);
  });

  it("отзыв: обязательный комментарий, предупреждение и сохранение", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={[queueUrl]} />);
    const firstRow = within(await screen.findByRole("table")).getAllByRole("row")[1]!;
    await user.click(firstRow);
    await waitFor(() => expect(screen.getByTestId("pane-A")).toHaveTextContent(/boolean\[\] (visited|seen)/));

    const panel = screen.getByLabelText("Отзыв по паре");
    const comment = within(panel).getByLabelText("Комментарий");
    const saveBtn = within(panel).getByRole("button", { name: "Сохранить отзыв" });

    // Без вердикта и комментария сохранить нельзя.
    expect(saveBtn).toBeDisabled();

    // Заполняем только комментарий — всё равно нельзя (нужен вердикт).
    await user.type(comment, "Совпадение по алгоритму");
    expect(saveBtn).toBeDisabled();

    // Выбираем вердикт → можно сохранить, появляется предупреждение «не сохранено».
    await user.click(within(panel).getByRole("button", { name: "Подтверждено" }));
    expect(saveBtn).toBeEnabled();
    expect(within(panel).getByRole("status")).toHaveTextContent("не сохранено");

    // Сохраняем → предупреждение исчезает, появляется «сохранено».
    await user.click(saveBtn);
    await waitFor(() => expect(within(panel).getByRole("status")).toHaveTextContent("сохранено"));
  });

  it("отзыв: черновик не теряется при переходе между парами", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={[queueUrl]} />);
    const firstRow = within(await screen.findByRole("table")).getAllByRole("row")[1]!;
    await user.click(firstRow);
    await waitFor(() => expect(screen.getByTestId("pane-A")).toHaveTextContent(/boolean\[\] (visited|seen)/));

    const panel = screen.getByLabelText("Отзыв по паре");
    const commentField = within(panel).getByLabelText("Комментарий");
    await user.clear(commentField);
    await user.type(commentField, "набросок, не сохранён");

    // Переключаемся на следующую пару и обратно.
    await user.click(screen.getByRole("button", { name: "Следующая пара" }));
    await waitFor(() =>
      expect(screen.getByTestId("pane-A")).not.toHaveTextContent(/boolean\[\] (visited|seen)/),
    );
    await user.click(screen.getByRole("button", { name: "Предыдущая пара" }));
    await waitFor(() => expect(screen.getByTestId("pane-A")).toHaveTextContent(/boolean\[\] (visited|seen)/));

    // Черновик комментария сохранился.
    expect(within(screen.getByLabelText("Отзыв по паре")).getByLabelText("Комментарий")).toHaveValue(
      "набросок, не сохранён",
    );
  });

  it("тема: тоггл переключает атрибут data-theme на <html>", async () => {
    const user = userEvent.setup();
    render(<App initialEntries={["/upload"]} />);
    await screen.findByRole("heading", { name: "Загрузка архива с работами" });

    const toggle = screen.getByRole("switch", { name: "Переключить тему" });
    // Атрибут data-theme всегда присутствует (light или dark) — это
    // переопределяет системную настройку и в светлой, и в тёмной теме.
    const before = document.documentElement.getAttribute("data-theme");
    await user.click(toggle);
    const after = document.documentElement.getAttribute("data-theme");
    expect(["light", "dark"]).toContain(after);
    expect(after).not.toBe(before);
  });
});