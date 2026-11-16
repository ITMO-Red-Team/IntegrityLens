import argparse
import gc
import sys

from .contract import InvalidInput
from .engine import run


def main():
    parser = argparse.ArgumentParser()
    sub = parser.add_subparsers(dest="command", required=True)
    command = sub.add_parser("run")
    command.add_argument("--input", required=True)
    command.add_argument("--output", required=True)
    args = parser.parse_args()
    # Процесс живёт один запуск, циклов в деревьях нет: сборка мусора только мешает.
    gc.disable()
    try:
        run(args.input, args.output)
    except InvalidInput:
        print("Invalid core input", file=sys.stderr)
        return 2
    except Exception as error:
        print(f"Core tool failed: {type(error).__name__}: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
