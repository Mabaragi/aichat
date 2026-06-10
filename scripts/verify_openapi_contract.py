import argparse
import json
from pathlib import Path


def load_contract(path: Path) -> bytes:
    if not path.is_file():
        raise SystemExit(f"OpenAPI contract does not exist: {path}")

    content = path.read_bytes()
    try:
        document = json.loads(content)
    except json.JSONDecodeError as error:
        raise SystemExit(f"Invalid OpenAPI JSON at {path}: {error}") from error

    if not isinstance(document, dict) or not str(document.get("openapi", "")).startswith("3."):
        raise SystemExit(f"Expected an OpenAPI 3.x document: {path}")
    return content


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Verify that a generated OpenAPI contract matches the committed contract."
    )
    parser.add_argument("committed", type=Path)
    parser.add_argument("generated", type=Path)
    args = parser.parse_args()

    committed = load_contract(args.committed)
    generated = load_contract(args.generated)

    if committed != generated:
        raise SystemExit(
            "OpenAPI contract drift detected. Run the documented export command and commit "
            f"{args.committed} with the backend API change."
        )

    print(f"OpenAPI contract verified: {args.committed}")


if __name__ == "__main__":
    main()
