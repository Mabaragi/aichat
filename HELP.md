# Aichat CLI

This project now has two entrypoints:

- `com.example.aichat.AichatApplication` for the REST API
- `com.example.aichat.cli.AichatCliApplication` for the terminal CLI

## CLI usage

Run the CLI entrypoint with Spring Boot's main-class override:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.example.aichat.cli.AichatCliApplication -- --help
```

Useful examples:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.example.aichat.cli.AichatCliApplication -- character create --owner-id 1 --name "합리주의 미식가"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.example.aichat.cli.AichatCliApplication -- character list --owner-id 1
.\mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.example.aichat.cli.AichatCliApplication -- character get 1
```

The durable CLI contract is documented in `docs/agent-vault/guides/character-cli.md`.
