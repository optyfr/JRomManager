# Plan: jrmserver Unit Test Coverage

## TL;DR
Add comprehensive JUnit 5 + Mockito + AssertJ unit tests across the `jrmserver` subproject (`jrm.server`, `jrm.server.shared`, `jrm.fullserver` packages) to increase coverage. Tests are organized in 6 phases from pure-logic (zero mocking) up to Jetty-client composite/integration tests. Source visibility is widened (private→package-private) where needed to enable direct testing. H2 in-memory is used for the DB/security layer. The existing `LaunchTest` classes are preserved.

## Context findings
- **Existing tests:** only `jrm.server.LauncherTest` and `jrm.fullserver.LauncherTest` (lifecycle smoke tests). No unit tests, no test resources dir.
- **Missing test deps:** `jrmserver` build.gradle lacked `assertj-core` and `mockito-core` (present in jrmcore/jrmcli/jrmstandalone/jrmfx). Has `jetty-client`, `jetty-http`, `awaitility`, `junit-jupiter`. **DONE (Phase 0).**
- **Test conventions** (`.github/instructions/test-patterns.instructions.md`): JUnit 5, AssertJ fluent assertions, `@DisplayName`, `@Nested`, AAA pattern, package-private test classes, no `System.out`, `@TempDir` for files, prefer real impls over mocks, manual stubs/minimal Mockito.
- **Static/global state needing isolation:** `WebSession.allSessions`/`terminate`, `LongPollingReqMgr.cmds`, `Login.cache`/`cachetime`, `ImageServlet.uri`/`isModule`, `AbstractServer.jettyserver`, `Server.sessions`/`httpPort`/`bind`.
- **WebSession** extends `jrm.security.Session` (jrmcore) — provides `getSessionId`, `getUser`, `getCurrProfile`, `getReport`, `setMsgs`, etc.
- **PathAbstractor** (`jrm.security`) — `%work`/`%shared`/`%presets` abstraction; `isWriteable` checks admin role; `getAbsolutePath` throws `SecurityException` on forged paths.
- **H2** is already an `implementation` dep (`com.h2database:h2:2.4.+`) → available at test runtime for in-memory DB tests.
- **jrmserver test task** already `dependsOn ':WebClient:build'` and sets `JRomManager.rootPath` system property → Jetty-client composite tests can reuse this.

## Steps

### Phase 0 — Test infrastructure (blocks all later phases) — IN PROGRESS
1. ✅ Add to `jrmserver` block in `build.gradle`: `assertj-core:3.27.7` and `mockito-core:latest.release`.
2. Create `jrmserver/src/test/resources/` directory (currently absent) with a `.gitkeep`.
3. Create shared test helper `jrmserver/src/test/java/jrm/server/shared/TestWebSessions.java`:
   - `static WebSession newAdminSession(String id)` — builds a real `WebSession` whose inherited `User` is admin with a real `Settings` backed by a temp workpath.
   - `static void resetStaticState()` — clears `WebSession.allSessions`, resets `WebSession.terminate`, `LongPollingReqMgr.cmds`, `Login.cache`/`cachetime` (call in `@AfterEach` of tests touching static state).
4. Create `jrmserver/src/test/java/jrm/server/shared/MockServlets.java` — helpers to build Mockito mocks of `HttpServletRequest`/`HttpServletResponse`/`HttpSession` wired to a `WebSession` attribute, with a `StringWriter`-backed response writer for JSON/XML body assertions.

### Phase 1 — Pure-logic unit tests (parallel-safe, no mocking) — parallel with Phase 2
1. `SQLUtilsTest` (`jrm.fullserver.db`) — minimal `SQLUtils` stub returning a mock `Connection` + fixed context; test every default method: `backquote`, `append`/`appendComma`/`prependComma`, `appendParam`, `makeCols` (3 overloads), `makeSet` (3 overloads), `notNull`, `getSQLValue`, `getDefaultValue` dispatch (CHAR/BOOL/INT), `getIterable`/`getReversedIterable`, `str`, `val`. Parameterize with `@CsvSource`.
2. `CryptCredentialTest` (`jrm.fullserver.security`) — pre-hashed vectors only: `hash("$2a$...")` returns input; `hash("$argon2id$...")` returns input; `check("wrong", knownHash)` → false; `check("correct", knownHash)` → true (precomputed BCrypt hash); no `hash(plaintext)` triggering Argon2.
3. `UserCredentialTest` — Lombok `@Data` bean: getters/setters, `equals`/`hashCode`/`toString`, both constructors.
4. `XMLRequestOperationSorterTest` (`jrm.server.shared.datasources`) — `Sorter` parsing: plain name (asc), `-name` (desc=true), `isDesc()`/`getName()`.
5. `WorkerTest` (`jrm.server.shared`) — `start()` spawns virtual thread; `isAlive()` true after start, false after `CountDownLatch`-controlled `Runnable` completes; `getProgress` settable.
6. `TempFileInputStreamTest` (`jrm.server.shared`) — `@TempDir`: `newInstance()` empty temp file auto-deleted on `close()`; `newInstance(InputStream)` copies bytes & deletes on close; `newInstance(in,len,close)` respects `close` flag; verify `length` matches.
7. `SSLReloadTest` (`jrm.fullserver.security`) — **visibility change:** `getDelayUntilTomorrowMidNight()` private→package-private. Assert delay is within `(0, 24h]`; `getInstance` returns a new instance for a mock `SslContextFactory`; `start()` schedules without throwing.

### Phase 2 — XML request/response tests (parallel with Phase 1)
1. `XMLRequestTest` (`jrm.server.shared.datasources`) — feed `ByteArrayInputStream` XML:
   - single `fetch` operation → `getOperation()` populated (operationType, operationId, startRow, endRow, sorters, data, oldValues).
   - `transaction` with multiple operations → `getTransaction().getOperations()` size.
   - XXE hardening: external entity/DOCTYPE ignored (assert no exception).
   - malformed XML → no exception thrown (logged), `getOperation()` null.
   - `Operation.addData`/`hasData`/`getData`/`getDatas` contract.
2. `XMLResponseTest` (`jrm.server.shared.datasources`) — define a `@Nested static class TestResponse extends XMLResponse` overriding `fetch` to write a fixed `<record>`; assert `processRequest()` returns a `TempFileInputStream` whose content (parsed via `javax.xml.stream.XMLInputFactory`) contains expected `<responses>`/`<response>`/`<status>` structure. Test `error`/`noError`/`success`/`failure`/`loginIncorrect`/`loginRequired`/`otherError` produce correct status codes. Test `fetchList`/`fetchArray`/`fetchStream` pagination with `startRow`/`endRow` slicing (use `Range`).

### Phase 3 — Servlet tests with mocked request/response (depends on Phase 0)
1. `AbstractSessionServletTest` (`jrm.server.handlers`) — `fillAndSendJSO`: mock `HttpSession` returning `WebSession` attribute; mock `WebSession.getUser().getSettings().asJSO()`; mock `ResourceBundle` (via `Messages`); capture `HttpServletResponse.getWriter()` `StringWriter`; assert JSON contains `session`, `msgs`, `settings` keys and `text/json` content type.
2. `SessionServletTest` (`jrm.server.handlers`) — `doPost` success path (delegates to `fillAndSendJSO`); exception path → `SC_INTERNAL_SERVER_ERROR`.
3. `fullserver/SessionServletTest` — `doPost` adds `authenticated:true` and `admin` from `ws.getUser().isAdmin()` (true/false cases).
4. `ActionServletTest` (`jrm.server.shared.handlers`) — **visibility changes:** `doInit`/`doLPR`/`encapsulate`/`sendResp` private→package-private.
   - `doPost`: content-length <0 → `SC_LENGTH_REQUIRED`; >max → `SC_REQUEST_ENTITY_TOO_LARGE`; wrong content-type → `SC_UNSUPPORTED_MEDIA_TYPE`; valid → `LongPollingReqMgr.process` invoked; unknown URI → `SC_NOT_IMPLEMENTED`.
   - `doGet`: `/actions/init` → `doInit` + `doLPR`; `/actions/lpr` → `doLPR`; unknown → `SC_NOT_IMPLEMENTED`.
   - `encapsulate`: 1 msg → single JSON; >1 → `Global.multiCMD` wrapper.
   - `doLPR`: `WebSession.isTerminate()` → `SC_GONE`; pre-populated `lprMsg` deque → drained & sent; empty → 20s poll (use `Awaitility` or pre-seed).
5. `DataSourceServletTest` (`jrm.server.shared.handlers`) — `processResponse` dispatch: for each of the 16 URIs assert the correct `XMLResponse` subclass is instantiated; unknown URI → null + `SC_NOT_IMPLEMENTED`. Mock `XMLRequest` construction by providing a minimal valid XML body via `req.getInputStream`.
6. `FullDataSourceServletTest` (`jrm.fullserver.handlers`) — `/datasources/admin` → `AdminXMLResponse`; other URIs delegate to `super.processResponse`.
7. `DownloadServletTest` (`jrm.server.shared.handlers`) — `doPost` with `/download/`:
   - regular file → `Content-Disposition` (UTF-8 filename), `SC_OK`, content-length, `Files.copy` to output.
   - directory → ZIP streamed (verify `ZipOutputStream` entries via captured output).
   - null `path` param → `SC_BAD_REQUEST`.
   - `PathAbstractor` forged path → `SecurityException` → `SC_INTERNAL_SERVER_ERROR`.
   Use `@TempDir` for real files.
8. `ImageServletTest` (`jrm.server.shared.handlers`) — **visibility change:** `resolveRequestedResourceUri` private→package-private.
   - path traversal: `../`, `\\`, `:`, `\0`, `//` → `URISyntaxException`/`SC_NOT_FOUND`.
   - valid resource → `SC_OK` with content-type/length (use a real test resource under `src/test/resources/resicons/`).
   - `If-Modified-Since` equal/after → `SC_NOT_MODIFIED`.
   - missing resource → `SC_NOT_FOUND`.
9. `UploadServletTest` (`jrm.server.shared.handlers`) — **visibility changes:** `sanitizeHeader`/`checkRequest`/`getXFileSize`/`doUpload` private→package-private.
   - `sanitizeHeader`: URL-decode, strip `..`/`\0`/control chars.
   - `checkRequest` status matrix: invalid path (6), non-writeable (8), non-existing dir (7), disk space (9), syntax (11), ok (0).
   - `doUpload`: success (3), IOException (20), size mismatch (21).
   - `Result` JSON serialization via `Gson`.
   - `doPost` `/upload/` with `init=1` → JSON `Result`; `doPut` → file written to `@TempDir`.

### Phase 4 — Actions tests (depends on Phase 0) — parallel with Phase 3
1. `GlobalActionsTest` (`jrm.server.shared.actions`) — mock `ActionsMgr` returning mock `WebSession`/`User`/`Settings`; capture `send(...)`:
   - `setProperty` sets boolean/string/other props, saves settings, sends `Global.updateProperty`.
   - `setMemory` sends `Global.setMemory` with formatted memory stats.
   - `gc` calls `setMemory` after `System.gc`.
   - `warn` sends `Global.warn`.
2. `ProgressActionsTest` — constructor sends `Open`; `setProgress`/`setInfos`/`canCancel`/`close` emit expected Gson JSON; `close` includes `errors` list; `doCancel` static sets cancel flag.
3. `ReportActionsTest` — `setFilter(jso, true)` lite vs `false`; clone `FilterOptions` EnumSet, add/remove per params, apply via mock `Report.getHandler().filter`, assert sent `Report.applyFilters`/`ReportLite.applyFilters` with full filter state.
4. `CatVerActionsTest` / `NPlayersActionsTest` — `loaded(Profile)` sends `CatVer.loaded`/`NPlayers.loaded` with relative path (or null); `load` sets profile property + saves (mock `Profile`).
5. `LongPollingReqMgrTest` (`jrm.server.shared.lpr`) — real `WebSession` + mock/capturing `ActionsMgr`:
   - `process(String)` routes `Global.setProperty` etc. (verify `lastAction` updated, side effects).
   - `send`/`sendOptional` add to `lprMsg` (sendOptional only if empty).
   - `setSession`/`unsetSession` register/deregister in static `cmds`; `saveAllSettings` iterates.
   - `isOpen()` always true.
   - Reset `cmds` in `@AfterEach`.
6. `ProfileActionsSmokeTest` / `BatchActionsSmokeTest` — `@Tag("smoke")`: for each worker-spawning action, mock `ActionsMgr`/`WebSession`/`Profile` minimally, invoke the action, assert `Worker.isAlive()` shortly after and that `end`/`updateResult` is eventually sent within `Awaitility` timeout (~10s). Do not validate deep jrmcore results.

### Phase 5 — Database layer tests (H2 in-memory) (depends on Phase 0) — parallel with Phase 3/4
1. `DBTest` (`jrm.fullserver.db`) — `getInstance(settings)` via `settings.getDBClass()`; `getInstance("jrm.fullserver.db.H2", settings)`; `getInstance(H2.class, settings)`; invalid class → `ClassNotFoundException`.
2. `H2Test` — **visibility changes:** `resolveName`/`getDBPath` private→package-private (H2 is package-private class, test in same package).
   - `shouldDropDB`: DB missing → true; source missing but DB exists → false; source/capture newer than DB → true; DB newer → false. Use `@TempDir` files with `Files.setLastModifiedTime`.
   - `resolveName`: appends/strips `.mv.db`.
   - `getDBPath`: `%w` substitution, `sys` extension → basePath.
   - `connectToDB`: in-memory H2 (`jdbc:h2:mem:`) returns non-null `Connection`; `drop=true` drops first; `ifexists` flag.
   - `dropDB`: deletes `<name>.*` files in `@TempDir`.
3. `SQLTest` — **visibility changes:** `convertBeanToMap`/`updateBeanFromMap`/`hasResult` keep protected (test via same-package subclass `TestSQL extends SQL`); `findArrayParam`/`convertArrayParams` private→package-private.
   - `createSchema`/`dropSchema` (synchronized) on in-memory H2.
   - `query`/`queryFirst`/`update`/`insert` round-trip on in-memory table.
   - `count`/`countTbl`/`getLongValue`/`getIntValue`/`getScalarValue`/`getColumnList`.
   - `convertBeanToMap`/`updateBeanFromMap` with a test bean.
   - array param conversion: `= ANY(?)` → `IN (?, ?...)`.
4. `LoginTest` (`jrm.fullserver.security`) — in-memory H2 via `DB.getInstance`:
   - constructor creates `USERS` table + default admin.
   - `login("admin","admin",...)` → `UserIdentity` with admin role; wrong password → null/failure.
   - `validate` true for cached identity; `logout` removes from cache; cache expiry after `cachetime` reset.
   - Reset `Login.cache`/`cachetime` in `@AfterEach`.
5. `AdminXMLResponseTest` (`jrm.fullserver.datasources`) — in-memory H2 + mock `WebSession`:
   - admin user → `fetch` lists users, `add` inserts (hashed password), `update` changes password/roles, `remove` deletes.
   - non-admin → `failure(CAN_T_DO_THAT)`.
   - Assert XML output via `processRequest()` `TempFileInputStream` content.

### Phase 6 — Server lifecycle + Jetty-client composite tests (depends on Phases 0-5)
1. `AbstractServerTest` (`jrm.server`) — `getWorkPath` (`jrommanager.dir`/`user.dir`), `getLogPath` (creates dir), `getPath` (jrt/file/jar URI schemes — use a `@TempDir` zip for `jar:`), `terminate`/`isStarted`/`isStopped` state machine. Use a minimal `AbstractServer` concrete subclass.
2. `WebSessionTest` (`jrm.server.shared`) — `close` removes from `allSessions` + sentinel to `lprMsg`; `closeAll` sets terminate; profile list caching (`newProfileList`/`putProfileList`/`getProfileList`/`getLastProfileListKey`/`removeProfileList`); NPE-on-`putProfileList`-before-`newProfileList` contract. Reset static state `@AfterEach`.
3. `SessionListenerTest` (`jrm.server`) — `sessionCreated` (multi=true → `WebSession(id,null,null)`; multi=false → `WebSession(id)`) stores attribute; `sessionDestroyed` calls `ws.close()`. Mock `HttpSessionEvent`/`HttpSession`.
4. `ServerCompositeTest` (`jrm.server`) — **Jetty client composite:** `Server.initialize` must support port 0 — add a package-private overload or expose the connector so the test reads `ServerConnector.getLocalPort()`; Jetty client targets that port. `Server.parseArgs(--client=WebClient/war, --debug)` + `Server.initialize()`; use `org.eclipse.jetty.client.HttpClient` to:
   - GET `/session` → 200 + JSON body with `session`/`msgs`/`settings`.
   - GET `/actions/init` → 200 JSON.
   - GET `/images/<nonexistent>` → 404.
   - POST `/datasources/<unknown>` → 204/501.
   - Use `Awaitility` to wait for server started; `Server.terminate()` in `@AfterAll`. Reuse `JRomManager.rootPath` system property already set by the test task.
5. `FullServerCompositeTest` (`jrm.fullserver`) — **Jetty client HTTPS composite:** `FullServer.parseArgs(--client, --cert=test certs, --debug)` + `initialize()` (port 0 for the SSL connector); `HttpClient` with `SslContextFactory.Client` (trust-all for test) GET `/session` over HTTPS → 200; `terminate()` in `@AfterAll`. (Guard with `Assumptions.assumeTrue(certsExist)`.)

## Verification
1. Run `gradle: build` (or `:jrmserver:test`) — all new tests pass, no regressions in existing `LaunchTest`.
2. Inspect JaCoCo HTML report at `jrmserver/build/reports/jacoco/test/html/index.html` — confirm coverage increase on target packages; target >60% line coverage on `jrm.server.shared`, `jrm.fullserver.db`, `jrm.fullserver.security`.
3. Run SonarQube MCP `toggle_automatic_analysis` (disable at start, re-enable at end) and `analyze_file_list` on all new/modified files — confirm no new critical/blocker issues; visibility changes don't introduce SonarQube complaints (package-private is acceptable).
4. Confirm static-state isolation: run tests with `--tests` repeated invocations; no flaky failures from `WebSession.allSessions`/`Login.cache`/`LongPollingReqMgr.cmds`.
5. Confirm Jetty-client composite tests bind to ephemeral ports (port 0 / `ServerConnector.getLocalPort`).

## Decisions
- **Visibility widening authorized** per user; prefer private→package-private (minimal, same-package test access) over protected, to avoid widening the public API.
- **Mockito + AssertJ** added to `jrmserver` (currently missing) to match sibling subprojects and the test-patterns instruction.
- **H2 in-memory** (`jdbc:h2:mem:`) for DB/Login/AdminXMLResponse tests — no TestContainers needed (H2 already on classpath); keeps tests fast/hermetic.
- **Jetty client** used for composite tests against a real `Server.initialize()` — reuses the existing `JRomManager.rootPath` system property and `:WebClient:build` dependency already wired in the test task.
- **Scope:** all classes in the attached `jrmserver/src/main/java/jrm` tree. Excluded: `package-info.java` files (no logic); `ServerSettings` (empty marker — trivial getter test only if needed).
- **Existing `LaunchTest`** preserved (not rewritten); new composite tests are additive.
- **Static-state reset** centralized in `TestWebSessions.resetStaticState()` called from `@AfterEach` of affected tests.

## Decisions resolved (from user)
1. **Port binding for composite tests — Option A (chosen):** Refactor `Server.initialize`/`FullServer.initialize` to accept an overrideable port (or expose the `ServerConnector` / use port 0 + `ServerConnector.getLocalPort` after start). Composite tests bind to port 0 and read the actual assigned port to avoid CI conflicts. This requires a small visibility/parameter change on `initialize` (e.g., add an overload `initialize(int httpPort)` or expose the connector). Prefer the minimal change: add a package-private setter or overload rather than altering the public `main` flow.
2. **Argon2 hashing cost in `CryptCredentialTest` — Option A (chosen):** Use pre-hashed idempotent vectors only (already-hashed input returned as-is; `check(plaintext, knownHash)` with known BCrypt/Argon2 vectors). No real Argon2 hashing in unit tests — keeps the suite fast. A single known Argon2 hash string is used as a verification vector (no re-hashing).
3. **Worker-spawning actions — Option B (chosen):** Smoke-only tests for `ProfileActions.imprt`/`load`/`scan`/`fix` and `Dat2Dir`/`Dir2Dat`/`TrntChk`/`Compressor` actions: assert a `Worker` is started and that `end`/`updateResult` is eventually sent (via `Awaitility` with a generous timeout). Synchronous methods (`loaded`/`imported`/`settings`/`clearResults`/`end`) get full unit tests. Deep jrmcore machinery (`Import`/`Scan`/`Fix`/`DirUpdater`/`TorrentChecker`/`Compressor`) is not exercised with real data in this iteration.

## Progress tracking
- [x] Phase 0.1 — build.gradle deps added
- [ ] Phase 0.2 — test resources dir
- [ ] Phase 0.3 — TestWebSessions helper
- [ ] Phase 0.4 — MockServlets helper
- [ ] Phase 1 — Pure-logic unit tests
- [ ] Phase 2 — XML request/response tests
- [ ] Phase 3 — Servlet tests
- [ ] Phase 4 — Actions tests
- [ ] Phase 5 — Database layer tests
- [ ] Phase 6 — Composite tests
- [ ] Verification — build + coverage + SonarQube