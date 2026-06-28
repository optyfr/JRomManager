---
description: "Use when writing, editing, or generating JUnit 5 tests in Java source files — covers naming conventions, assertion style, fixture usage, and test organization patterns."
applyTo: "**/src/test/**/*.java"
---

# Test Conventions for JRomManager

These conventions apply to **all Java test source edits** in JRomManager. Follow them consistently to maintain a uniform testing style across modules (`jrmcore`, `jrmfx`, `jrmserver`, `jrmcli`, `jrmstandalone`).

## Framework & Assertions
- **JUnit 5** (`org.junit.jupiter.api.*`): use `@Test`, `@ParameterizedTest`, `@BeforeEach`, `@BeforeAll`, `@Nested`, `@TempDir`, `@DisplayName`.
- **AssertJ** fluent assertions (`org.assertj.core.api.Assertions.*`): prefer `assertThat(...)`, `assertThatThrownBy(...)`, `assertThatCode(...)`.
- Import assertions statically at the top of each test class.
- Avoid JUnit 4 (`org.junit.*`) and Hamcrest matchers — migrate any legacy usage when encountered.

## Test Structure
- Follow **Arrange → Act → Assert** (AAA) pattern. Use blank lines to separate the three phases when each block has multiple statements.
- One logical assertion per test; use multiple `assertThat` calls within the same test when verifying related properties.
- Prefer `@DisplayName` on every `@Test` and `@Nested` class — write a short, human-readable sentence describing intent.
- Use `@Nested` classes to group related tests by feature, scenario, or input variation.

## Naming
- Class names: `<ClassUnderTest>Test` (package-private, no `public` modifier).
- Method names: descriptive camelCase — prefer `parseTorrent_MissingInfoKey_ThrowsException()` or `shouldThrowWhenInfoKeyMissing()`.
- Avoid generic names like `test1`, `testMethod`, `myTest`.

## Test Data & Fixtures
- Place test resource files under `src/test/resources/<feature>/` (e.g., `src/test/resources/dats/`, `src/test/resources/torrents/`).
- Use `@TempDir Path tempDir` for any test that needs to create or write files.
- Load classpath resources with `Path.of("src/test/resources/...")` or `getClass().getResourceAsStream(...)`.
- Prefer **real-world fixtures** over synthetic data when validating parsers (actual DAT files, torrent files, etc.).
- Use `@ParameterizedTest` with `@ValueSource`, `@MethodSource`, or `@CsvSource` to validate the same logic across multiple inputs.

## Mocks & Dependencies
- Prefer real implementations over mocks when the dependency is lightweight and deterministic.
- When mocking is necessary, use a lightweight approach (manual stubs or minimal Mockito usage).
- Do not mock value objects, DTOs, or parsers — test them with real inputs.

## Constraints
- Tests are package-private by default; do not add `public` modifiers to test classes or methods unless required by a framework.
- Do not use `System.out.println` in tests — use assertions or `@DisplayName` for diagnostics.
- Do not catch and swallow exceptions in tests — let them propagate or use `assertThatThrownBy`.
- Keep tests independent — no shared mutable state between test methods.
- When a test requires a complex setup, extract it into a private helper method or a `@BeforeEach` block.
