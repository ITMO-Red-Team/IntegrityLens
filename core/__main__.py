import argparse
import sys

from .mock import InvalidInput, run


def main():
    parser = argparse.ArgumentParser()
    sub = parser.add_subparsers(dest="command", required=True)
    command = sub.add_parser("run")
    command.add_argument("--input", required=True)
    command.add_argument("--output", required=True)
    args = parser.parse_args()
    try:
        run(args.input, args.output)
    except InvalidInput:
        print("Invalid core input", file=sys.stderr)
        return 2
    except Exception:
        print("Core tool failed", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
