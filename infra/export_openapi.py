import argparse


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://localhost:8080")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    raise SystemExit(
        "api/openapi.yaml is now the target API contract (spec-first). "
        "The current Java backend implements the previous API. "
        "Do not overwrite the contract from /v3/api-docs.yaml; "
        "restore implementation conformance checking when issue #40 is complete."
    )
if __name__ == "__main__":
    main()
