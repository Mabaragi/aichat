from pathlib import Path


WORKFLOW_PATH = Path(".github/workflows/ci.yml")


def require(content: str, text: str) -> int:
    position = content.find(text)
    if position < 0:
        raise SystemExit(f"Missing OpenAPI CI contract: {text}")
    return position


def main() -> None:
    content = WORKFLOW_PATH.read_text(encoding="utf-8")

    require(content, '- "docs/api/**"')
    export_position = require(content, "-Dtest=OpenApiContractExportTest")
    verify_position = require(content, "python scripts/verify_openapi_contract.py")
    artifact_position = require(content, "actions/upload-artifact@v4")
    base_check_position = require(
        content,
        'git cat-file -e "origin/${GITHUB_BASE_REF}:docs/api/openapi.json"',
    )
    breaking_position = require(content, "oasdiff/oasdiff-action/breaking@v0.0.57")
    require(content, "steps.openapi-base.outputs.exists == 'true'")
    require(content, "fail-on: ERR")

    if export_position > verify_position:
        raise SystemExit("OpenAPI contract must be exported before drift verification.")
    if verify_position > artifact_position:
        raise SystemExit("OpenAPI contract must be verified before artifact upload.")
    if base_check_position > breaking_position:
        raise SystemExit("The base contract must be checked before oasdiff runs.")

    print("OpenAPI CI flow verified.")


if __name__ == "__main__":
    main()
