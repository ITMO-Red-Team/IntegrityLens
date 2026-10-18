"""Запуск контейнеров.

Монтирования задаются через `--mount type=bind`: в `-v` путь Windows с
двоеточием после буквы диска разбирается неоднозначно.
"""

from __future__ import annotations

import shutil
import subprocess
from dataclasses import dataclass
from pathlib import Path


class DockerError(RuntimeError):
    """Docker недоступен или контейнер завершился с ошибкой."""


@dataclass(frozen=True)
class Mount:
    source: Path
    target: str
    read_only: bool = True

    def to_arg(self) -> str:
        parts = ["type=bind", f"source={self.source.resolve()}", f"target={self.target}"]
        if self.read_only:
            parts.append("readonly")
        return ",".join(parts)


def docker() -> str:
    exe = shutil.which("docker")
    if exe is None:
        raise DockerError("docker не найден в PATH: инструменты запускаются в контейнерах")
    return exe


def daemon_version() -> str:
    result = subprocess.run(
        [docker(), "version", "--format", "{{.Server.Version}}"],
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        raise DockerError(f"демон Docker не отвечает: {result.stderr.strip()}")
    return result.stdout.strip()


def image_exists(image: str) -> bool:
    return (
        subprocess.run([docker(), "image", "inspect", image], capture_output=True).returncode
        == 0
    )


def build_images(services: list[str], compose_file: Path) -> None:
    cmd = [docker(), "compose", "-f", str(compose_file), "build", *services]
    if subprocess.run(cmd).returncode != 0:
        raise DockerError(f"сборка не удалась: {' '.join(cmd)}")


def run(
    image: str,
    args: list[str],
    mounts: list[Mount] = (),
    entrypoint: str | None = None,
    timeout: float | None = None,
    check: bool = True,
) -> subprocess.CompletedProcess[str]:
    """Один запуск контейнера; stdout и stderr возвращаются целиком."""
    cmd = [docker(), "run", "--rm"]
    for mount in mounts:
        cmd.extend(["--mount", mount.to_arg()])
    if entrypoint is not None:
        cmd.extend(["--entrypoint", entrypoint])
    cmd.append(image)
    cmd.extend(args)
    result = subprocess.run(
        cmd,
        capture_output=True,
        text=True,
        timeout=timeout,
        encoding="utf-8",
        errors="replace",
    )
    if check and result.returncode != 0:
        raise DockerError(
            f"{image} вернул {result.returncode}\n"
            f"команда: {' '.join(cmd)}\n"
            f"stderr: {result.stderr.strip()[-2000:]}"
        )
    return result
