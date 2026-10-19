"""Скачать всё внешнее: jar JPlag, Dolos CLI и два набора данных.

    uv run python -m research_one.fetch

Версии и контрольные суммы прибиты: релизные артефакты и архивы на GitHub
теоретически заменяемы, а результат должен воспроизводиться. Сумма jar та же,
что в tools/jplag/jplag.Dockerfile. Dolos ставится `npm ci` по
package-lock.json рядом с pyproject.toml: там прибиты версии и sha512 всех
пакетов, включая транзитивные.

Архивы — недоверенные данные: распаковываются в отдельную пустую директорию,
пути внутри проверяются, ничего из них не исполняется.
"""

from __future__ import annotations

import hashlib
import shutil
import subprocess
import sys
import urllib.request
import zipfile
from dataclasses import dataclass
from pathlib import Path

from research_one.paths import DATA, DOLOS_BIN, ROOT, VENDOR


@dataclass(frozen=True)
class Artifact:
    name: str
    url: str
    sha256: str
    target: Path            # куда положить файл
    unpack_to: Path | None  # куда распаковать zip; None — не распаковывать


JPLAG_VERSION = "6.3.0"
JPLAG_JAR = VENDOR / f"jplag-{JPLAG_VERSION}-jar-with-dependencies.jar"

# Та же версия прибита в package.json; dolos.py сверяет её с `dolos --version`.
DOLOS_VERSION = "2.9.3"

ARTIFACTS = (
    Artifact(
        name="JPlag 6.3.0",
        url=(
            "https://github.com/jplag/JPlag/releases/download/"
            f"v{JPLAG_VERSION}/jplag-{JPLAG_VERSION}-jar-with-dependencies.jar"
        ),
        sha256="5f2c21e8b88ed77134effcb3a5a3ab13d188f6a3e16d401387f7479e92db9aa2",
        target=JPLAG_JAR,
        unpack_to=None,
    ),
    # Karnalim et al., Informatics in Education 18(2), 2019. Apache-2.0.
    # Коммит прибит: master может уехать.
    Artifact(
        name="IR-Plag",
        url=(
            "https://github.com/oscarkarnalim/sourcecodeplagiarismdataset/raw/"
            "f07e95120680b25f78659ed63cd6130fd05b2df8/IR-Plag-Dataset.zip"
        ),
        sha256="1b96b320fd05d164b3a7ee17d8cc36992b728d3be099dc7ec39ddb7fae2ec4a4",
        target=DATA / "archives" / "IR-Plag-Dataset.zip",
        unpack_to=DATA / "irplag",
    ),
    # Slobodkin, Sadovnikov, 2022. Zenodo record 7332790, CC BY 4.0.
    Artifact(
        name="ConPlag",
        url="https://zenodo.org/api/records/7332790/files/conplag.zip/content",
        sha256="df4ea274e82a0d1c8587286612447be2c5ece514f74a5c4d0678236a160495da",
        target=DATA / "archives" / "conplag.zip",
        unpack_to=DATA / "conplag",
    ),
)


def sha256_of(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1 << 20), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _download(artifact: Artifact) -> None:
    artifact.target.parent.mkdir(parents=True, exist_ok=True)
    if not artifact.target.exists():
        print(f"скачиваю {artifact.name}: {artifact.url}")
        partial = artifact.target.with_suffix(artifact.target.suffix + ".part")
        with urllib.request.urlopen(artifact.url, timeout=300) as response:
            with partial.open("wb") as handle:
                shutil.copyfileobj(response, handle)
        partial.rename(artifact.target)
    actual = sha256_of(artifact.target)
    if actual != artifact.sha256:
        raise SystemExit(
            f"{artifact.name}: контрольная сумма не совпала\n"
            f"  ждали   {artifact.sha256}\n  пришло  {actual}\n"
            f"  файл    {artifact.target}"
        )


def _safe_unpack(archive: Path, destination: Path) -> None:
    """Распаковка zip без выхода за пределы `destination`."""
    if destination.exists():
        return
    staging = destination.with_name(destination.name + ".part")
    shutil.rmtree(staging, ignore_errors=True)
    staging.mkdir(parents=True)
    root = staging.resolve()
    with zipfile.ZipFile(archive) as zf:
        for member in zf.infolist():
            target = (staging / member.filename).resolve()
            if not target.is_relative_to(root):
                raise SystemExit(f"{archive.name}: путь {member.filename!r} вне архива")
        zf.extractall(staging)
    staging.rename(destination)


def install_dolos() -> None:
    """`npm ci` в research/one/: node_modules/ строго по package-lock.json.

    Пакеты Dolos собирают tree-sitter под текущий Node при установке, поэтому
    это установка, а не скачивание одного файла.
    """
    if DOLOS_BIN.exists():
        return
    npm = shutil.which("npm")
    if npm is None:
        raise SystemExit("npm не найден: для Dolos нужен Node.js >= 18 с npm")
    print(f"ставлю Dolos {DOLOS_VERSION}: npm ci в {ROOT}")
    subprocess.run(
        [npm, "ci", "--no-audit", "--no-fund", "--loglevel=error"], cwd=ROOT, check=True
    )


def fetch_all() -> None:
    for artifact in ARTIFACTS:
        _download(artifact)
        if artifact.unpack_to is not None:
            _safe_unpack(artifact.target, artifact.unpack_to)
        print(f"готово: {artifact.name} → {artifact.unpack_to or artifact.target}")
    install_dolos()
    print(f"готово: Dolos {DOLOS_VERSION} → {DOLOS_BIN}")


if __name__ == "__main__":
    fetch_all()
    sys.exit(0)
