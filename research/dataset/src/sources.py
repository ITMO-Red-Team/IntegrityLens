"""Внешние наборы, из которых собран датасет: адрес, контрольная сумма, лицензия.

Архивы — недоверенные данные: распаковываются в отдельную директорию, пути
внутри проверяются, ничего из них не исполняется.
"""

from __future__ import annotations

import hashlib
import shutil
import urllib.request
import zipfile
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "data"


@dataclass(frozen=True)
class Source:
    name: str
    url: str
    sha256: str
    license: str
    cite: str

    @property
    def archive(self) -> Path:
        return DATA / "archives" / f"{self.name}.zip"

    @property
    def root(self) -> Path:
        return DATA / self.name


CONPLAG = Source(
    name="conplag",
    url="https://zenodo.org/api/records/7332790/files/conplag.zip/content",
    sha256="df4ea274e82a0d1c8587286612447be2c5ece514f74a5c4d0678236a160495da",
    license="CC BY 4.0",
    cite=(
        "Slobodkin E., Sadovnikov A. A Dataset of Programming Contest Plagiarism "
        "in Java. Zenodo, 2022. https://doi.org/10.5281/zenodo.7332790"
    ),
)


def sha256_of(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1 << 20), b""):
            digest.update(chunk)
    return digest.hexdigest()


def fetch(source: Source) -> Path:
    """Скачать (если архива нет), сверить сумму, распаковать. Вернуть корень набора."""
    archive = source.archive
    if not archive.exists():
        archive.parent.mkdir(parents=True, exist_ok=True)
        partial = archive.with_suffix(".part")
        with urllib.request.urlopen(source.url) as response, partial.open("wb") as out:
            shutil.copyfileobj(response, out)
        partial.rename(archive)
    actual = sha256_of(archive)
    if actual != source.sha256:
        raise SystemExit(f"{archive}: sha256 {actual}, ожидалась {source.sha256}")

    marker = source.root / ".unpacked"
    if marker.exists() and marker.read_text() == source.sha256:
        return source.root
    if source.root.exists():
        shutil.rmtree(source.root)
    source.root.mkdir(parents=True)
    base = source.root.resolve()
    with zipfile.ZipFile(archive) as bundle:
        for info in bundle.infolist():
            target = (base / info.filename).resolve()
            if base != target and base not in target.parents:
                raise SystemExit(f"{archive}: небезопасный путь {info.filename!r}")
            if (info.external_attr >> 16) & 0o170000 == 0o120000:
                raise SystemExit(f"{archive}: символическая ссылка {info.filename!r}")
        bundle.extractall(base)
    marker.write_text(source.sha256)
    return source.root
