import { test, expect, type Page } from "@playwright/test";

const password = "E2E-password-2026";
const java = `class Example {
  static long evaluate(int limit) {
    long sum = 0;
    for (int i = 0; i < limit; i++) {
      int value = i * i;
      if (value % 3 == 0) sum += value;
    }
    return sum;
  }
  public static void main(String[] args) {
    System.out.println(evaluate(42)); // Проверка UTF-8
  }
}
`;

async function register(page: Page, email: string, role: "teacher" | "student", name: string) {
  await page.getByRole("link", { name: "Зарегистрироваться", exact: true }).click();
  await page.getByLabel("ФИО", { exact: true }).fill(name);
  await page.getByLabel("Email", { exact: true }).fill(email);
  await page.getByLabel("Пароль", { exact: true }).fill(password);
  await page.getByLabel(role === "teacher" ? "Преподаватель" : "Студент", { exact: true }).check();
  await page.getByRole("button", { name: "Зарегистрироваться", exact: true }).click();
  await expect(page).toHaveURL(/\/login$/);
  await page.getByLabel("Email", { exact: true }).fill(email);
  await page.getByLabel("Пароль", { exact: true }).fill(password);
  await page.getByRole("button", { name: "Войти", exact: true }).click();
}

async function createAssignment(page: Page, title: string, source?: string) {
  await page.goto("/assignments/new");
  await page.getByLabel("Название", { exact: true }).fill(title);
  if (source) await page.getByRole("checkbox", { name: new RegExp(source) }).check();
  await page.getByRole("button", { name: "Сохранить", exact: true }).click();
  await expect(page.getByRole("heading", { name: "Задание создано" })).toBeVisible();
  const link = await page.getByLabel("Ссылка для сдачи", { exact: true }).inputValue();
  await page.getByRole("link", { name: "Открыть задание", exact: true }).click();
  return { link, url: page.url() };
}

async function submit(page: Page, contents = java) {
  await page.getByLabel("Java-файл", { exact: true }).setInputFiles({
    name: "Example.java", mimeType: "text/plain", buffer: Buffer.from(contents),
  });
  await page.getByRole("button", { name: "Отправить работу", exact: true }).click();
  await expect(page.getByRole("heading", { name: "Работа принята", exact: true })).toBeVisible();
}

test("real backend: registration, submissions, comparison, reset, sources and deletion", async ({ browser, baseURL }) => {
  const contexts = await Promise.all([1, 2, 3].map(() => browser.newContext({ baseURL })));
  for (const context of contexts) context.setDefaultTimeout(15_000);
  const [teacher, first, second] = await Promise.all(contexts.map(c => c.newPage()));
  const errors: string[] = [];
  for (const page of [teacher, first, second]) page.on("pageerror", error => errors.push(error.message));
  const suffix = Date.now();
  try {
    await teacher.goto("/login");
    await register(teacher, `teacher-${suffix}@example.org`, "teacher", "E2E Teacher");
    await expect(teacher).toHaveURL(/\/assignments$/);
    expect((await contexts[0].cookies()).find(c => c.name === "session")).toMatchObject({ httpOnly: true, secure: true });
    const sourceTitle = `Source ${suffix}`;
    const source = await createAssignment(teacher, sourceTitle);
    const target = await createAssignment(teacher, `Target ${suffix}`, sourceTitle);

    await first.goto(source.link);
    await expect(first).toHaveURL(/\/login$/);
    await register(first, `first-${suffix}@example.org`, "student", "E2E First Student");
    await expect(first).toHaveURL(source.link);
    await first.getByLabel("Java-файл", { exact: true }).setInputFiles({
      name: "bad.txt", mimeType: "text/plain", buffer: Buffer.from(java),
    });
    await expect(first.getByRole("alert")).toContainText(".java");
    await expect(first.getByRole("button", { name: "Отправить работу", exact: true })).toBeDisabled();
    await submit(first);
    await first.goto(target.link);
    await submit(first);
    await first.reload();
    await expect(first.getByRole("heading", { name: "Работа уже принята", exact: true })).toBeVisible();

    await second.goto(target.link);
    await register(second, `second-${suffix}@example.org`, "student", "E2E Second Student");
    await expect(second.getByLabel("Java-файл", { exact: true })).toBeVisible();
    await submit(second, java.replace("42", "47"));

    // The already-open teacher page must discover new work and finished scores without a reload.
    await expect(teacher.getByRole("link", { name: /Открыть работу A-/ })).toHaveCount(2);
    await expect(teacher.locator("tbody")).not.toContainText("В анализе", { timeout: 180_000 });
    await expect(teacher.locator("tbody")).not.toContainText("Ошибка");
    await teacher.getByLabel("Показать ФИО", { exact: true }).check();
    const firstRow = teacher.getByRole("row").filter({ hasText: "E2E First Student" });
    await expect(firstRow).toBeVisible();
    await teacher.screenshot({ path: test.info().outputPath("assignment.png"), fullPage: true });
    await teacher.getByLabel("Сортировка работ").selectOption("ai");
    await expect(teacher).toHaveURL(/sort=ai/);
    await teacher.getByLabel("Сортировка работ").selectOption("similarity");
    await firstRow.getByRole("link").click();
    await expect(teacher.getByRole("heading", { name: "Исходный Java-код" })).toBeVisible();
    await expect(teacher.locator("pre")).toContainText("Проверка UTF-8");
    const sourceMatch = teacher.getByRole("link").filter({ hasText: sourceTitle });
    await expect(sourceMatch).toHaveCount(1);
    await sourceMatch.click();
    await expect(teacher.getByRole("heading", { name: "Сравнение двух работ" })).toBeVisible();
    const fragments = teacher.getByRole("group", { name: "Совпадающие фрагменты" });
    await expect(fragments).toBeVisible();
    await fragments.getByRole("button").first().click();
    await expect(fragments.getByRole("button").first()).toHaveAttribute("aria-pressed", "true");
    await teacher.screenshot({ path: test.info().outputPath("comparison.png"), fullPage: true });
    await teacher.getByRole("link", { name: "Открыть работу B как основную" }).click();
    await expect(teacher).toHaveURL(new RegExp(source.url.split("/assignments/")[1] + "/submissions/"));

    await teacher.goto(target.url);
    await teacher.getByRole("button", { name: "Изменить источники", exact: true }).click();
    await teacher.getByRole("checkbox", { name: new RegExp(sourceTitle) }).uncheck();
    await teacher.getByRole("button", { name: "Сохранить источники", exact: true }).click();
    await teacher.getByRole("link", { name: /Открыть работу A-/ }).first().click();
    await expect(teacher.getByRole("link").filter({ hasText: sourceTitle })).toHaveCount(0);
    await teacher.goto(target.url);
    await teacher.getByLabel("Показать ФИО", { exact: true }).check();
    await teacher.getByRole("row").filter({ hasText: "E2E First Student" }).getByRole("button", { name: /Сбросить попытку/ }).click();
    await teacher.getByRole("button", { name: "Подтвердить сброс", exact: true }).click();
    await first.getByRole("button", { name: "Проверить доступность повторной сдачи" }).click();
    await submit(first, java.replace("42", "45"));
    await teacher.goto(source.url);
    await teacher.getByRole("button", { name: "Удалить задание", exact: true }).click();
    await teacher.getByRole("alertdialog").getByRole("button", { name: "Подтвердить удаление", exact: true }).click();
    await expect(teacher).toHaveURL(/\/assignments$/);
    await second.goto(source.link);
    await expect(second.getByRole("heading", { name: "Не удалось открыть задание" })).toBeVisible();
    // A student cannot read the teacher's assignment, including by a direct API request.
    const forbidden = await second.request.get(target.url.replace("/assignments/", "/api/assignments/"));
    expect(forbidden.status()).toBe(403);
    await teacher.getByRole("button", { name: "Выйти", exact: true }).click();
    await expect(teacher).toHaveURL(/\/login$/);
    expect((await teacher.request.get("/api/auth/me")).status()).toBe(401);
    expect(errors).toEqual([]);
  } finally {
    await Promise.allSettled(contexts.map(c => c.close()));
  }
});
