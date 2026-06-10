from pathlib import Path


WORKFLOW_PATH = Path(".github/workflows/release.yml")


def require(content: str, text: str) -> int:
    position = content.find(text)
    if position < 0:
        raise SystemExit(f"Missing release secret-flow contract: {text}")
    return position


def main() -> None:
    content = WORKFLOW_PATH.read_text(encoding="utf-8")

    output_position = require(content, "output -raw jwt_secret_parameter_name")
    fetch_position = require(content, "aws ssm get-parameter")
    require(content, "--with-decryption")
    empty_check_position = require(content, 'if [ -z \\"$JWT_SECRET\\" ]')
    require(content, "-e JWT_SECRET=\\\"$JWT_SECRET\\\"")
    require(content, "unset JWT_SECRET")
    require(content, "rm -f /tmp/aichat-deploy.sh")
    require(content, "rm -f ssm-parameters.json")
    remove_position = require(content, 'docker rm -f \\"$CONTAINER_NAME\\"')

    if output_position > fetch_position:
        raise SystemExit("Terraform parameter output must be captured before the deploy command is built.")
    if fetch_position > remove_position:
        raise SystemExit("JWT secret must be fetched before the existing container is removed.")
    if empty_check_position > remove_position:
        raise SystemExit("JWT secret must be checked before the existing container is removed.")
    if "secrets.JWT_SECRET" in content:
        raise SystemExit("JWT secret must not be sourced from GitHub Secrets.")

    print("Release JWT secret flow verified.")


if __name__ == "__main__":
    main()
