"""Настоящий движок метода ai: GigaCheck из tools/aicode, CPU.

aicode отдаёт span'ы символьными смещениями по декодированному тексту, контракт
требует байтовые смещения UTF-8 — конвертация и слияние перекрывающихся span'ов
здесь. Импорты torch и aicode ленивые: контракт и мок живут и без них.
"""
import os
import re

from .contract import CONTRACT, InvalidInput
from .engine import input_paths
from .version import DEFAULT_MODEL_DIR, method_version

# Ошибка файла — не ошибка прогона: done допускает ошибки отдельных файлов.
NOT_PARSED = "NOT_PARSED"
TOOL_ERROR = "TOOL_ERROR"
# Число потоков фиксировано и не равно числу ядер машины: один поток медленнее
# вчетверо, а оценка не должна зависеть от того, сколько ядер досталось воркеру.
DEFAULT_THREADS = 4
NEWLINE = re.compile(r"(\r\n?)")  # группа — чтобы split вернул и переводы строк

_MODEL = None


def analyze(job, params, input_dir):
    version = method_version()
    if version != job["methodVersion"]:
        # Образ с одной версией модели получил задание под другую; повтор не поможет.
        raise InvalidInput()
    paths = input_paths(job, input_dir)
    model, tokenizer, torch, analyze_text = _load()
    files = [_analyze_one(entry, paths[entry["submissionId"]], model, tokenizer, torch, analyze_text,
                          params["threshold"])
             for entry in job["inputFiles"]]
    return {"contract": CONTRACT, "runId": job["runId"], "methodVersion": version,
            "params": params, "files": files}


def _load():
    global _MODEL
    if _MODEL is None:
        import torch
        from aicode import detector

        torch.set_num_threads(int(os.environ.get("AICODE_THREADS", DEFAULT_THREADS)))
        model_dir = os.environ.get("AICODE_MODEL_DIR", DEFAULT_MODEL_DIR)
        model, tokenizer, torch = detector.load_model(model_id=model_dir, local_files_only=True)
        _MODEL = (model, tokenizer, torch, detector.analyze_text)
    return _MODEL


def _analyze_one(entry, path, model, tokenizer, torch, analyze_text, threshold):
    sid = entry["submissionId"]
    try:
        data = path.read_bytes()
    except OSError as exc:
        return _failed(sid, NOT_PARSED, f"cannot read {entry['path']}: {exc}")
    try:
        text, bom = decode_source(data)
    except UnicodeDecodeError as exc:
        return _failed(sid, NOT_PARSED, f"{entry['path']} is not valid UTF-8: {exc}")
    # Инструмент читает файл в текстовом режиме, где `\r\n` уже сведены к `\n`; с `\r` модель
    # теряет уверенность. Здесь то же приведение, а границы возвращаются в байты файла.
    model_text, origin = normalize_newlines(text)
    try:
        result = analyze_text(path=path, text=model_text, model=model, tokenizer=tokenizer, torch=torch,
                              threshold=threshold)
    except Exception as exc:  # DetectorError и прочие сбои инференса
        return _failed(sid, TOOL_ERROR, f"{type(exc).__name__}: {exc}")
    fragments = byte_fragments(result.spans, text, bom, origin)
    score = sum(f["endIndex"] - f["startIndex"] for f in fragments) / len(data) if data else 0.0
    return {"submissionId": sid, "status": "ok", "score": score, "suspicious": fragments}


def _failed(sid, code, message):
    return {"submissionId": sid, "status": "failed", "score": None, "suspicious": [],
            "reason": {"code": code, "message": message}}


def decode_source(data):
    """Текст без BOM и длина BOM в байтах: aicode читает как utf-8-sig, а смещения
    контракта считаются от начала файла, BOM нужно вернуть в учёт."""
    bom = 3 if data.startswith(b"\xef\xbb\xbf") else 0
    text = data[bom:].decode("utf-8")  # строгий decode: битый UTF-8 — ошибка файла
    return text, bom


def normalize_newlines(text):
    """Текст с переводами строк `\n` и позиции его символов в исходном.

    `origin[i]` — где начинается i-й символ приведённого текста в `text`, последний
    элемент — длина `text`. Без `\r` текст не меняется, и позиции не нужны (None).
    """
    if "\r" not in text:
        return text, None
    normalized, origin, position = [], [], 0
    for part in NEWLINE.split(text):
        if part.startswith("\r"):
            normalized.append("\n")
            origin.append(position)
        else:
            normalized.append(part)
            origin.extend(range(position, position + len(part)))
        position += len(part)
    origin.append(len(text))
    return "".join(normalized), origin


def byte_fragments(spans, text, bom=0, origin=None):
    """Символьные span'ы aicode → байтовые фрагменты контракта.

    Смещения aicode считаются по тексту модели; `origin` переводит их в символы `text`,
    если переводы строк приводились. Перекрывающиеся и касательные span'ы сливаются
    (confidence — максимум), как `_merge_ranges` в aicode считает долю помеченного.
    """
    # Текст кодируется один раз и только до последней нужной границы.
    offsets, base, size = {}, 0, bom
    for end in sorted({origin[i] if origin else i for span in spans for i in (span.start_char, span.end_char)}):
        size += len(text[base:end].encode("utf-8"))
        base = end
        offsets[end] = size

    def offset(index):
        return offsets[origin[index] if origin else index]

    byte_spans = sorted((offset(span.start_char), offset(span.end_char), float(span.confidence)) for span in spans)
    fragments = []
    for start, end, confidence in byte_spans:
        if end <= start:
            continue
        if fragments and start <= fragments[-1]["endIndex"]:
            previous = fragments[-1]
            previous["endIndex"] = max(previous["endIndex"], end)
            previous["confidence"] = max(previous["confidence"], confidence)
        else:
            fragments.append({"startIndex": start, "endIndex": end, "confidence": confidence})
    return fragments
