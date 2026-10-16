import { useRef, useState, type DragEvent, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useCreateBatch } from "../../api/hooks";
import type { Language } from "../../api/types";
import { PageHeader } from "../../components/ui";
import { LANGUAGES, MAX_ARCHIVE_BYTES, MAX_ARCHIVE_MB, MAX_WORKS, MIN_WORKS } from "../../lib/constants";

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
  const [language, setLanguage] = useState<Language>("java");
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

  const ext = LANGUAGES.find((l) => l.value === language)?.ext ?? "";

  const onDrop = (e: DragEvent) => {
    e.preventDefault();
    setOver(false);
    pick(e.dataTransfer.files);
  };

  const submit = (e: FormEvent) => {
    e.preventDefault();
    if (!archive || create.isPending) return;
    create.mutate({ language, archive }, { onSuccess: (batch) => navigate(`/batches/${batch.id}`) });
  };

  return (
    <>
      <PageHeader step="Шаг 1" title="Загрузка архива с работами" />
      <form onSubmit={submit} className="panel flex flex-col gap-4 p-4">
        <label className="flex w-48 flex-col gap-1 text-xs font-medium text-muted">
          Язык работ
          <select id="language" className="field" value={language} onChange={(e) => setLanguage(e.target.value as Language)}>
            {LANGUAGES.map((l) => (
              <option key={l.value} value={l.value}>
                {l.label}
              </option>
            ))}
          </select>
        </label>

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
          className={`flex min-h-[320px] cursor-pointer flex-col items-center justify-center gap-1 rounded-lg border-2 border-dashed px-5 text-center ${
            over ? "border-accent bg-f2-bg" : "border-line bg-sunk"
          }`}
        >
          {archive ? (
            <>
              <p className="font-mono text-lg font-semibold">{archive.name}</p>
              <p className="text-muted">{formatSize(archive.size)} · нажмите, чтобы выбрать другой архив</p>
            </>
          ) : (
            <>
              <p className="text-lg font-semibold">Перетащите архив .zip или нажмите, чтобы выбрать</p>
              <p className="text-muted">
                Внутри — от {MIN_WORKS} до {MAX_WORKS} файлов {ext}, один файл на одну работу. До {MAX_ARCHIVE_MB} МБ.
              </p>
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

        <div className="flex justify-end">
          <button type="submit" className="btn btn-primary" disabled={!archive || create.isPending}>
            {create.isPending ? "Загружаю…" : "Запустить анализ"}
          </button>
        </div>
      </form>
    </>
  );
}