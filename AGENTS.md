# Repository Guidelines

## Project Structure & Module Organization
- Modules: `smartcs-web-client` (DTOs/API contracts), `smartcs-web-domain` (domain model & services), `smartcs-web-infrastructure` (persistence, Redis, LangChain4j, MyBatis‑Plus), `smartcs-web-app` (application services, `*CmdExe`/`*QryExe`), `smartcs-web-adapter` (REST/WebSocket/SSE controllers), `start` (Spring Boot entrypoint, config).
- Code root: `com.leyue.smartcs`. Config lives in `start/src/main/resources` (`application.yaml`, `bootstrap.yaml`).
- Tests: per‑module under `src/test/java` (e.g., `smartcs-web-infrastructure/src/test/java`).

## Build, Test, and Development Commands
- Build all: `mvn clean install -DskipTests`
- Compile quickly: `mvn -q -DskipTests compile`
- Run tests: `mvn test` (single module: `mvn -pl smartcs-web-infrastructure test`; single test: `mvn -Dtest=QueryTransformerPipelineTest test`).
- Run locally: `cd start && mvn spring-boot:run`
- Example env: `export OPENAI_API_KEY=...` (optional `OPENAI_BASE_URL=...`).

## Coding Style & Naming Conventions
- Java 17; 4‑space indentation; no tabs; UTF‑8.
- Packages: `com.leyue.smartcs.<bounded_context>`; keep DDD boundaries (client/domain/infrastructure/app/adapter).
- Naming: `PascalCase` classes; `camelCase` methods/fields; `UPPER_SNAKE_CASE` constants.
- Patterns: controllers `*Controller`; services `*ServiceImpl`; executors `*CmdExe`/`*QryExe`; mappers with MapStruct.
- Use Lombok for boilerplate and MapStruct for converters; avoid wildcard imports.

## Testing Guidelines
- Frameworks: JUnit 5 and Spring Boot Test.
- Location: `src/test/java`; name tests `*Test.java` (e.g., `SecretCryptoServiceTest`).
- Keep unit tests fast and isolated; mock infrastructure. Run a focused test via `mvn -Dtest=ClassName test`.

## Commit & Pull Request Guidelines
- Commits: follow Conventional Commits — `feat(scope): summary` (CN/EN acceptable). Example: `feat(rag): 优化查询转换流程`.
- Branches: `feat/...`, `fix/...`, `refactor/...`.
- PRs: include clear description, linked issues, test coverage notes, and API change examples (curl/screenshots). CI must pass: Maven build/tests and “RAG Quality Gate” workflow.

## Security & Configuration Tips
- Never commit secrets. Derive from `start/src/main/resources/application-secrets-template.yaml`; load via `--spring.config.additional-location` or environment variables.
- Ensure local services (MySQL, Redis, Kafka/Nacos if enabled) match `start/src/main/resources/application.yaml`.

