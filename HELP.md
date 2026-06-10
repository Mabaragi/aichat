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
.\mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.example.aichat.cli.AichatCliApplication -- user create --email user@example.com --password password123 --nickname "마바라기"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.example.aichat.cli.AichatCliApplication -- user get 1
.\mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.example.aichat.cli.AichatCliApplication -- debate create --owner-id 1 --topic-title "부먹 vs 찍먹" --format PROS_AND_CONS --max-rounds 5 --max-turn-length 600 --participant "{\"characterId\":10,\"model\":\"FAST\"}" --participant "{\"characterId\":20,\"model\":\"QUALITY\"}"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.example.aichat.cli.AichatCliApplication -- debate next-turn --turn-index 3 --participant-count 2 --max-rounds 5
```

The durable CLI contract is documented in `docs/agent-vault/guides/character-cli.md`.
