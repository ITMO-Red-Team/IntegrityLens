import argparse
import urllib.request
from pathlib import Path


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://localhost:8080")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    with urllib.request.urlopen(args.base.rstrip("/") + "/v3/api-docs.yaml", timeout=30) as response:
        document = response.read()
    destination = Path(__file__).resolve().parents[1] / "api" / "openapi.yaml"
    if args.check:
        if not destination.exists() or destination.read_bytes() != document:
            raise SystemExit("api/openapi.yaml is stale; run python3 infra/export_openapi.py")
    else:
        destination.write_bytes(document)
        print(destination)


if __name__ == "__main__":
    main()
