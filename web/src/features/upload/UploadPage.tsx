import { useRef, useState, type DragEvent, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useCreateBatch } from "../../api/hooks";
import { PageHeader } from "../../components/ui";

import { MAX_ARCHIVE_BYTES, MAX_ARCHIVE_MB, MAX_WORKS, MIN_WORKS } from "../../lib/constants";

function validateArchive(f: File): string | null {
  if (!/\.zip$/i.test(f.name)) return `${f.name}: нужен архив .zip`;
    if (f.size > MAX_ARCHIVE_BYTES) return `${f.name}: больше ${MAX_ARCHIVE_MB} МБ`;
  if (f.size === 0) return `${f.name}: пустой файл`;
  return null;
}

const formatSize = (bytes: number) =>
  bytes < 1024 * 1024 ? `${(bytes / 1024).toFixed(1)} КБ` : `${(bytes / 1024 / 1024).toFixed(1)} МБ`;

export function UploadPage() {
  const navigate = useNavigate();
  const create = useCreateBatch();
  const inputRef = useRef<HTMLInputElement>(null);
  const [course, setCourse] = useState("Алгоритмы, поток 2");
  const [assignment, setAssignment] = useState("");
  const [archive, setArchive] = useState<File | null>(null);
  const [problem, setProblem] = useState<string | null>(null);
  const [over, setOver] = useState(false);

  const pick = (list: FileList | null) => {
    const f = list?.[0];
    if (!f) return;
    const err = validateArchive(f);
    setProblem(err);
    setArchive(err ? null : f);
    create.reset();
  };

  const onDrop = (e: DragEvent) => {
    e.preventDefault();
    setOver(false);
    pick(e.dataTransfer.files);
  };

  const canSubmit = !!course.trim() && !!assignment.trim() && !!archive && !create.isPending;

  const submit = (e: FormEvent) => {
    e.preventDefault();
    if (!canSubmit || !archive) return;
    create.mutate(
      { course: course.trim(), assignment: assignment.trim(), language: "java", archive },
      { onSuccess: (batch) => navigate(`/batches/${batch.id}`) },
    );
  };

  return (
    <>
      <PageHeader step="Шаг 1" title="Загрузка архива с работами" />
      <form onSubmit={submit} className="grid gap-4 lg:grid-cols-[minmax(0,1.3fr)_minmax(0,1fr)]">
        <div className="panel flex flex-col gap-4 p-4">
          <div className="grid gap-3 sm:grid-cols-3">
            <label className="flex flex-col gap-1 text-xs font-medium text-muted">
              Курс
              <input id="course" className="field" value={course} onChange={(e) => setCourse(e.target.value)} required />
            </label>
            <label className="flex flex-col gap-1 text-xs font-medium text-muted">
              Задание
              <input
                id="assignment"
                className="field"
                placeholder="ДЗ-5. Кратчайшие пути"
                value={assignment}
                onChange={(e) => setAssignment(e.target.value)}
                required
              />
            </label>
            <label className="flex flex-col gap-1 text-xs font-medium text-muted">
              Язык работ
              <select id="language" className="field" defaultValue="java">
                <option value="java">Java</option>
              </select>
            </label>
          </div>

          <div
            role="button"
            tabIndex={0}
            aria-label="Выбрать архив .zip"
            onClick={() => inputRef.current?.click()}
            onKeyDown={(e) => {
              if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                inputRef.current?.click();
              }
            }}
            onDragOver={(e) => {
              e.preventDefault();
              setOver(true);
            }}
            onDragLeave={() => setOver(false)}
            onDrop={onDrop}
            className={`cursor-pointer rounded-lg border-[1.5px] border-dashed px-5 py-7 text-center ${
              over ? "border-accent bg-f2-bg" : "border-line bg-sunk"
            }`}
          >
            {archive ? (
              <>
                <p className="font-mono font-semibold">{archive.name}</p>
                <p className="text-muted">{formatSize(archive.size)} · нажмите, чтобы выбрать другой архив</p>
              </>
            ) : (
              <>
                <p className="font-semibold">Перетащите архив .zip или нажмите, чтобы выбрать</p>
                <p className="text-muted">Внутри — от {MIN_WORKS} до {MAX_WORKS} файлов .java, один файл на одну работу. До {MAX_ARCHIVE_MB} МБ.</p>
              </>
            )}
            <input
              ref={inputRef}
              type="file"
              accept=".zip,application/zip"
              hidden
              data-testid="archive-input"
              onChange={(e) => {
                pick(e.target.files);
                e.target.value = "";
              }}
            />
          </div>

          {(problem || create.isError) && (
            <p role="alert" className="text-high">
              {problem ?? (create.error as Error).message}
            </p>
          )}

          <div className="flex flex-wrap items-center justify-between gap-3">
            <span className="text-muted">
              {!assignment.trim() ? "Укажите задание." : !archive ? "Выберите архив." : "Всё готово к анализу."}
            </span>
            <button type="submit" className="btn btn-primary" disabled={!canSubmit}>
              {create.isPending ? "Загружаю…" : "Запустить анализ"}
            </button>
          </div>
        </div>

        <aside className="flex flex-col gap-4">
          <div className="panel flex flex-col gap-2 p-4">
            <h2 className="text-lg">Что произойдёт</h2>
            <ol className="list-decimal pl-5 text-muted">
              <li>Архив распаковывается, каждой работе присваивается псевдоним S-xxx.</li>
              <li>Стартовый код курса, импорты и комментарии исключаются из сравнения.</li>
              <li>Каждая пара оценивается несколькими методами: лексический baseline, структурное сходство, эмбеддинги.</li>
              <li>Пары ранжируются по risk score; выше порога — в очередь ручной проверки.</li>
            </ol>
          </div>
          <div className="panel flex flex-col gap-2 p-4">
            <h2 className="text-lg">Приватность</h2>
            <ul className="list-disc pl-5 text-muted">
              <li>Имена папок и файлов в архиве часто содержат ФИО. Сервер их не возвращает.</li>
              <li>В интерфейсе и экспорте используются только псевдонимы.</li>
              <li>Код не передаётся во внешние сервисы.</li>
            </ul>
          </div>
        </aside>
      </form>
    </>
  );
}