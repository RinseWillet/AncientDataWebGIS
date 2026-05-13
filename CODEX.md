# CODEX.md

Use this file for Codex-style agents working in the backend repo.

## Base policy

- Follow `../ancientdataworkspace/AGENT-RULES.md`.
- Keep repo behavior aligned with `CLAUDE.md` in this directory.

## Backend context

- Stack: Java 21, Spring Boot 3.5.x, PostgreSQL/PostGIS, Flyway.
- Main code: `src/main/java/com/webgis/ancientdata`.
- Tests: `src/test`.

## Verification commands

```bash
./gradlew test --tests "*<ClassNameOrPattern>*"
./gradlew test
```

## Agent-specific behavior notes

- Preserve existing style and avoid unrelated refactoring.
- Add or update tests for behavior changes and bug fixes.
- Call out frontend/API impact when backend contract changes.
