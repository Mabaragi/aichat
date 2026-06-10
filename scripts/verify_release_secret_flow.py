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
    generation_provider_output_position = require(content, "output -raw generation_provider")
    openai_parameter_output_position = require(content, "output -raw openai_api_key_parameter_name")
    gemini_parameter_output_position = require(content, "output -raw gemini_api_key_parameter_name")
    fetch_position = require(content, "aws ssm get-parameter")
    require(content, "--with-decryption")
    empty_check_position = require(content, 'if [ -z \\"$JWT_SECRET\\" ]')
    require(content, "GENERATION_PROVIDER='__GENERATION_PROVIDER__'")
    require(content, "OPENAI_API_KEY_PARAMETER_NAME='__OPENAI_API_KEY_PARAMETER_NAME__'")
    require(content, "GEMINI_API_KEY_PARAMETER_NAME='__GEMINI_API_KEY_PARAMETER_NAME__'")
    require(content, r'\"${docker_env[@]}\"')
    require(content, r'-e \"JWT_SECRET=$JWT_SECRET\"')
    require(content, r'-e \"GENERATION_PROVIDER=$GENERATION_PROVIDER\"')
    require(content, r'-e \"OPENAI_API_KEY=$OPENAI_API_KEY\"')
    require(content, r'-e \"GEMINI_API_KEY=$GEMINI_API_KEY\"')
    require(content, "unset JWT_SECRET")
    require(content, "unset JWT_SECRET OPENAI_API_KEY GEMINI_API_KEY")
    require(content, "rm -f /tmp/aichat-deploy.sh")
    require(content, "rm -f ssm-parameters.json")
    require(content, 'docker build -t "${BACKEND_IMAGE_URI}" backend')
    require(content, 'docker build -t "${FRONTEND_IMAGE_URI}" frontend')
    backend_pull_position = require(content, r'docker pull \"$BACKEND_IMAGE_URI\"')
    frontend_pull_position = require(content, r'docker pull \"$FRONTEND_IMAGE_URI\"')
    remove_position = require(content, r'docker rm -f \"$FRONTEND_CONTAINER_NAME\"')
    require(content, r'-p \"127.0.0.1:${BACKEND_PORT}:8080\"')
    require(content, r'-p \"${APP_PORT}:3000\"')
    require(content, r'-e BACKEND_BASE_URL=\"http://aichat:8080\"')
    require(content, r'-e AUTH_COOKIE_SECURE=\"false\"')
    openai_fetch_position = require(content, r'OPENAI_API_KEY=\"$(aws ssm get-parameter')
    gemini_fetch_position = require(content, r'GEMINI_API_KEY=\"$(aws ssm get-parameter')

    if output_position > fetch_position:
        raise SystemExit("Terraform parameter output must be captured before the deploy command is built.")
    if generation_provider_output_position > fetch_position:
        raise SystemExit("Generation provider output must be captured before the deploy command is built.")
    if openai_parameter_output_position > fetch_position:
        raise SystemExit("OpenAI API key parameter output must be captured before the deploy command is built.")
    if gemini_parameter_output_position > fetch_position:
        raise SystemExit("Gemini API key parameter output must be captured before the deploy command is built.")
    if fetch_position > remove_position:
        raise SystemExit("JWT secret must be fetched before the existing container is removed.")
    if openai_fetch_position > remove_position:
        raise SystemExit("OpenAI API key must be fetched before the existing container is removed.")
    if gemini_fetch_position > remove_position:
        raise SystemExit("Gemini API key must be fetched before the existing container is removed.")
    if empty_check_position > remove_position:
        raise SystemExit("JWT secret must be checked before the existing container is removed.")
    if backend_pull_position > remove_position or frontend_pull_position > remove_position:
        raise SystemExit("Both images must be pulled before existing containers are removed.")
    if "secrets.JWT_SECRET" in content or "secrets.OPENAI_API_KEY" in content or "secrets.GEMINI_API_KEY" in content:
        raise SystemExit("JWT secret must not be sourced from GitHub Secrets.")

    print("Release secret flow verified.")


if __name__ == "__main__":
    main()
