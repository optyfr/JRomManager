# Java Modernization Plan: JRomManager

- **Generated**: 2026-07-05
- **Scope**: Modernization review (NOT a version bump — project already on Java 25 LTS)
- **Gradle wrapper**: 9.6.1 (Java 25 compatible ✓)
- **Lombok**: 1.18.46 (Java 25 compatible ✓)

## Context

The project is already on the latest Java LTS (25) via root `build.gradle`:

```groovy
java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
subprojects {
    java { sourceCompatibility = JavaVersion.VERSION_25; targetCompatibility = JavaVersion.VERSION_25 }
    tasks.withType(JavaCompile).configureEach {
        // options.compilerArgs += ['-Xlint:deprecation']   // <-- COMMENTED OUT
        options.release = (project.name == 'WebClient') ? 17 : java.targetCompatibility.majorVersion.toInteger()
    }
}
```

Therefore "upgrade Java further" means **modernizing the source to leverage Java 25 language features and APIs** and **cleaning up deprecated/legacy usage**, not bumping the bytecode target.

---

## 1. Top Findings (Assessment Summary)

### ✅ Already modern / low priority
- **No `finalize()`, no `StringBuffer`, no `Vector`/`Stack`/`Hashtable` types, no `SecurityManager` remnants, no `sun.misc`/internal imports.** The codebase is clean of the worst legacy patterns.
- **Virtual threads are already adopted** in `jrmcore/src/main/java/jrm/misc/MultiThreadingVirtual.java` via `Executors.newThreadPerTaskExecutor(Thread.ofVirtual()...)`.
- **Pattern matching for `instanceof`** is already widely used (119 occurrences across 30 files) — the codebase is fluent in this.
- **Switch expressions** are already used (104 `switch` occurrences; many are expression-form).
- **Lombok 1.18.46** is compatible with Java 25.

### ⚠️ Modernization opportunities (prioritized below)
1. **Build config**: `sourceCompatibility`/`targetCompatibility` is the deprecated approach. Migrate to Gradle **toolchains**. (High value, low risk.)
2. **Deprecation lint disabled**: `-Xlint:deprecation` is commented out. Re-enable and address warnings. (Medium value, surfaces latent issues.)
3. **Jetty deprecation/removal**: Three `@SuppressWarnings("removal")` sites in `jrmserver` — `ConnectionLimit` and `GzipHandler` are deprecated-for-removal in Jetty 12. (Medium value.)
4. **Jetty thread pool not using virtual threads**: `FullServer.createThreadPool()` returns a plain `QueuedThreadPool`. Jetty 12 supports virtual-thread executors — a high-value server-scaling opportunity. (High value, medium risk.)
5. **Legacy `java.util.Date`**: 7 occurrences of `new Date()` in `jrmserver` (the `WebSession.lastAction` field and 6 action handlers). Should be `java.time.Instant`. (Medium value, low risk.)
6. **No records / no sealed types anywhere**: Several immutable `@RequiredArgsConstructor` value classes are textbook record candidates. (Medium value, low risk.)
7. **Manual `new Thread(...)` in FX/standalone UIs**: 7 sites in `jrmfx`/`jrmstandalone` use raw `new Thread(task)` for background work. Some are unavoidable (JavaFX offloading), but shutdown hooks and long tasks could use virtual threads. (Low-medium value.)
8. **Reflection `setAccessible` in production**: One production site in `EntityBase.getParent`-style reflection (`field.setAccessible(true) // NOSONAR`). Compiles but is a latent strong-encapsulation risk on JDK 25+. (Low value, flag for awareness.)

### 🚫 Deferred / Skip
- **WebClient Java 17 pin**: KEEP. WebClient uses GWT 2.13.0 + SmartGWT 14.1. GWT's compiler is the binding constraint and does not yet support Java 21+ source. Re-evaluating the pin would require migrating off GWT — out of scope for a modernization pass. The current `options.release = 17` override is correct.
- **Bulk record conversion of `@Data` classes**: Most `@Data` classes (e.g. `ProfileNFOStats`) are **mutable** and use **custom serialization** (`ObjectInputStream`/`ObjectStreamField`) — they are **not** record candidates. Only convert genuinely immutable value classes (see Step 6).
- **Replacing Lombok wholesale**: Lombok is pervasive and consistent. Do not remove. Only opportunistically replace where a native record is strictly better.

---

## 2. Recommended Plan (Prioritized, Incremental)

> **Philosophy**: Each step is independently shippable and leaves the build green. Build/config first (enables the rest), then safe mechanical refactors, then feature adoption. No step depends on a later one being reverted.

### Phase A — Build Configuration (enables everything else)

#### Step 1: Migrate to Gradle Java toolchains
- **Rationale**: `sourceCompatibility`/`targetCompatibility` is the legacy approach and decouples the *build JDK* from the *target JDK*. Toolchains make the target JDK explicit and reproducible, and are the Gradle-recommended way since 7.3.
- **Changes**:
  - In root `build.gradle` (root `java {}` block ~lines 84-87 and the `subprojects { java {} }` block ~lines 127-130): replace
    ```groovy
    java {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
    ```
    with
    ```groovy
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }
    ```
    in both the root and `subprojects` blocks.
  - Remove the now-redundant `options.release = ...` override for non-WebClient modules in `tasks.withType(JavaCompile)` (toolchains already pin the release). **Keep** the WebClient-specific `options.release = 17` so the GWT module still compiles to Java 17 bytecode while the toolchain JDK is 25. Alternatively (cleaner), give WebClient its own toolchain pin to 17 — but this conflicts with GWT requiring a 17-classpath, so **keep the `options.release = 17` override for WebClient** and document why.
  - In `WebClient/build.gradle` (~lines 36-39): leave `sourceCompatibility = 17 / targetCompatibility = 17` as-is (GWT module, isolated) OR migrate to toolchain 17 for consistency. **Recommendation**: migrate to `toolchain { languageVersion = JavaLanguageVersion.of(17) }` for consistency, since WebClient has its own `java {}` block already.
- **Verification**: `./gradlew clean compileJava compileTestJava -q` on root and each module. Confirm `java -version` of produced `.class` files = 25 (17 for WebClient) via `javap -v`.
- **Risk**: Low. Toolchains auto-provision/download the JDK if missing (may need `foojay-resolver-convention` plugin — add `id 'org.gradle.toolchains.foojay-resolver-convention' version '0.9.0'` to `settings.gradle` if not present).

#### Step 2: Re-enable deprecation lint and triage warnings
- **Rationale**: The commented-out `// options.compilerArgs += ['-Xlint:deprecation']` hides latent issues (esp. Jetty deprecations). Surfacing them is the prerequisite to fixing them.
- **Changes**:
  - Uncomment the line in the `subprojects` `tasks.withType(JavaCompile)` block:
    ```groovy
    options.compilerArgs += ['-Xlint:deprecation']
    ```
  - Run a full compile and **capture the warning list** (do not fail the build yet — consider adding `-Werror` only after Step 5 clears the Jetty deprecations).
- **Verification**: `./gradlew clean compileJava -q 2>&1 | grep -i deprec` — capture and categorize all warnings into a tracking list.
- **Output**: A categorized warning inventory feeding Steps 5 and 6.

### Phase B — Safe Mechanical Refactors (low risk, high readability)

#### Step 3: Replace `java.util.Date` with `java.time.Instant` in jrmserver
- **Rationale**: `Date` is legacy, mutable, and poorly designed. `Instant` is immutable and thread-safe. The `WebSession.lastAction` field already lives in a server with concurrent access — `Instant` is strictly safer.
- **Changes** (7 sites):
  - `jrmserver/src/main/java/jrm/server/shared/WebSession.java:120` — change field type `Date lastAction = new Date()` → `Instant lastAction = Instant.now()`.
  - `jrmserver/.../actions/ActionsMgr.java:94`, `Dat2DirActions.java:169`, `Dir2DatActions.java:150`, `ProfileActions.java:168,281,424,497`, `TrntChkActions.java:160` — change `new Date()` → `Instant.now()`.
  - Update any serialization/transmission of `lastAction` (JSON via Gson). `Instant` serializes to ISO-8601 by default with Gson — verify the WebClient JavaScript side tolerates the new format (it currently reads `new Date()` on the client at `RemoteFileChooser.java:610,688`, but those are *client-side* JS `Date`s, unrelated to the server field). **Verify** no Java client code parses the server's `lastAction` as epoch-millis.
- **Verification**: `./gradlew :jrmserver:clean :jrmserver:test -q` + manual smoke test of session expiry if a test exists.
- **Risk**: Low-medium. The only real risk is the JSON wire format change for `lastAction`. Mitigation: if Gson is configured to serialize `Instant` as epoch-millis to preserve the wire format, register a `TypeAdapter` — but ISO-8601 is preferable if the client tolerates it.

#### Step 4: Modernize `Worker extends Thread` in jrmserver
- **Rationale**: Extending `Thread` is a long-deprecated pattern (effective Java item: "prefer executors/tasks to threads"). `Worker` only carries a `progress` field set once at start.
- **Changes**:
  - `jrmserver/src/main/java/jrm/server/shared/Worker.java` — convert to a small holder or, better, evaluate whether the `progress` field can be passed directly to the `ProgressReporter` instead of stashing it on a `Thread` subclass. If callers rely on `Worker` being a `Thread` (e.g., `Thread.currentThread()` casts), introduce a `ThreadLocal<ProgressActions>` or pass `ProgressActions` explicitly.
  - **Audit call sites first** (`grep -r "Worker" jrmserver/src`) — this step may be larger than it appears. If the coupling is deep, defer to a follow-up and just add a `@Deprecated` javadoc note for now.
- **Verification**: `./gradlew :jrmserver:clean :jrmserver:test -q`.
- **Risk**: Medium. Coupling audit required before committing to a rewrite.

### Phase C — Feature Adoption (higher value, evaluate per-module)

#### Step 5: Address Jetty deprecation/removal warnings (from Step 2)
- **Rationale**: Three `@SuppressWarnings("removal")` sites suppress real Jetty 12 deprecations: `ConnectionLimit` (in `Server.java:295` and `FullServer.java:726`) and `GzipHandler` (`AbstractServer.java:122`). These APIs are slated for removal in future Jetty versions.
- **Changes**:
  - **`GzipHandler`** (`org.eclipse.jetty.ee9.servlet.GzipHandler`): In Jetty 12 the ee9 `GzipHandler` is deprecated in favor of the core `org.eclipse.jetty.server.handler.gzip.GzipHandler` (configured on the connector/handler without ee9). Migrate `AbstractServer.gzipHandler()` to the core GzipHandler.
  - **`ConnectionLimit`** (`org.eclipse.jetty.server.ConnectionLimit`): deprecated for removal; replacement is `org.eclipse.jetty.server.limit.ConnectionLimit` (note the `limit` sub-package) or configuring via `ServerConnector` idle timeout / a `ConnectionLimit` bean on the new package. Check the exact Jetty 12.x replacement at the version in use (`jetty-*:12.+`).
  - Remove the `@SuppressWarnings("removal")` once migrated.
- **Verification**: `./gradlew :jrmserver:clean :jrmserver:test -q` + confirm no remaining `removal` warnings.
- **Risk**: Medium. API replacement exactness depends on the resolved Jetty 12.x patch version. **Pin the Jetty version** (currently `12.+` floats) before doing this so the replacement API is stable.

#### Step 6: Adopt records for immutable value classes
- **Rationale**: Several `@RequiredArgsConstructor` classes with all-`final` fields and no behavior are textbook records. Native records remove Lombok overhead, are immutable by construction, and get `equals`/`hashCode`/`toString` for free.
- **Candidates** (verify each has no mutable state, no inheritance need, and no problematic serialization before converting):
  - `jrmserver/.../actions/ActionsMgr.java` — inner `UpdateResult`, `UpdateResult.Params`, `SingleCmd` (all `final` fields, JSON DTOs — **excellent** record candidates).
  - `jrmcore/.../profile/report/EntryNote.java` — `abstract` class extending `Note` with `serialVersionUID`. **Skip** — records can't extend classes and this is serializable; keep as-is.
  - `jrmcore/.../security/User.java` — immutable fields but has logic (`getRoles()` etc.) and is a domain entity. **Borderline** — could be a record if it's purely a carrier, but it has behavior. Evaluate, likely **skip**.
- **Changes**: Convert the `ActionsMgr` inner DTOs (`UpdateResult`, `Params`, `SingleCmd`) to records. These are JSON request/response envelopes — ideal.
- **Verification**: `./gradlew :jrmserver:clean :jrmserver:test -q`; verify Gson serializes records correctly (Gson 2.10+ supports records natively — check the resolved `gson:latest.release` version is ≥ 2.10).
- **Risk**: Low for the DTOs. The `entrySet`-style DTOs have no serialization concerns.

#### Step 7: Enable Jetty virtual threads for request handling
- **Rationale**: The project already uses virtual threads for *compute* (`MultiThreadingVirtual`), but the Jetty request-handling pool is still a platform-thread `QueuedThreadPool`. Jetty 12 natively supports virtual threads for request handling, which dramatically improves concurrency for I/O-bound servlet work (the server handles file uploads/downloads, DB queries, profile scans — all I/O-bound). This is the single highest-value server modernization.
- **Changes**:
  - `jrmserver/.../fullserver/FullServer.java` `createThreadPool()` (~line 745):
    ```java
    private static QueuedThreadPool createThreadPool() {
        final int max = maxThreads > 0 ? maxThreads : (connLimit * 4);
        final int min = minThreads > 0 ? minThreads : (connLimit / 4);
        final var pool = new QueuedThreadPool(max, min);
        pool.setVirtualThreadsExecutor(
            Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("jetty-vt-", 0).factory()));
        return pool;
    }
    ```
    This keeps platform threads for blocking-accept but runs request handling on virtual threads. Optionally set `pool.setDetailedDump(false)`.
  - Consider also making this **configurable** (a `virtualThreads` setting in server config) so users on unusual workloads can opt out.
- **Verification**: Load test (the `jrmserver` test suite uses `jetty-client` + `awaitility` — extend with a concurrent-request test). Confirm no `synchronized`-on-virtual-thread pinning issues in the request path (see `FullServer.java:932,949` and `Login.java:250` `synchronized` blocks — `synchronized` pins virtual threads; consider `ReentrantLock` for hot paths if profiling shows pinning).
- **Risk**: Medium-high. Virtual threads + `synchronized` can pin. Mitigation: audit the `synchronized` blocks in the request path (3 in `FullServer`, 1 in `Login`); replace hot ones with `java.util.concurrent.locks.ReentrantLock`. JDK 21+ has reduced pinning for `synchronized` on intrinsically short operations, but DB/login caches held under `synchronized` while doing I/O are the danger zone.

#### Step 8: Convert eligible `switch` statements to switch pattern matching
- **Rationale**: 104 `switch` sites; several dispatch on type (`instanceof` chains) or enum-with-default that read more clearly as pattern-matching switch (Java 21+). This is a **readability** improvement, not correctness.
- **Candidates** (cherry-pick, do not bulk-convert):
  - `jrmcore/.../profile/fix/actions/AddEntry.java:93` — `switch (entry.getParent().getType())` over enum.
  - `jrmfx/.../ui/progress/ProgressController.java:93` — `if (n instanceof HBox w)` could be part of a pattern switch if there are sibling branches.
  - `jrmstandalone/.../report/ReportTreeCellRenderer.java:55` — `if (value instanceof ReportNode rn)` chain → pattern switch.
  - `jrmcli/.../JRomManagerCLI.java:906` — `else if (name instanceof EnumWithDefault n)` chain → pattern switch.
- **Changes**: Convert 3-5 high-value `instanceof`-chain sites to pattern-matching `switch`. Leave enum switches that are already expression-form alone unless they benefit from null-handling.
- **Verification**: `./gradlew clean test -q` per affected module.
- **Risk**: Low. Pure refactor; covered by existing tests.

#### Step 9: Replace FX/standalone raw `new Thread(...)` with virtual threads where appropriate
- **Rationale**: 7 sites use `new Thread(task)` for background work. JavaFX background tasks that touch the scene graph **must** run on platform threads via `Platform.runLater`, so the worker thread itself can be a virtual thread (it's just waiting on I/O most of the time).
- **Candidates**:
  - Shutdown hooks (`new Thread(() -> ...)` in `JRomManager.java:73`, `MainFrame.java:79`, `AbstractServer.java:182`, `jrmstandalone/.../JRomManager.java:89`, `MainFrame.java:84`) — low value, leave (shutdown hooks are fine as platform threads).
  - Long tasks (`BatchToolsPanelController.java:225`, `Dir2DatController.java:171`, `ProfilePanelController.java:634`, `ProgressTaskRunner.java:31`) — convert `new Thread(task)` → `Thread.startVirtualThread(task)` **only where the task is I/O-bound** (compression, DAT import, dir scan). CPU-bound tasks (zip compression) should stay on platform threads.
- **Changes**: Convert the 3-4 I/O-bound task launches to `Thread.startVirtualThread(...)`. Keep shutdown hooks and CPU-bound compression tasks on platform threads.
- **Verification**: `./gradlew :jrmfx:clean :jrmfx:test -q` + manual FX smoke test of each converted task.
- **Risk**: Low-medium. Virtual threads are cheap; the main risk is if a task relies on `Thread` identity (it doesn't — these use `Task`/`Runnable`).

### Phase D — Awareness / No Action

#### Step 10: Document residual risks (no code change)
- **`EntityBase.java:147` `field.setAccessible(true) // NOSONAR`**: Production reflection into fields. On JDK 25+ with strong encapsulation, this works only for non-JDK fields (which is the case here — it's reflecting on `EntityBase`'s own domain fields). **No action needed**, but document in `summary` that this is intentional domain reflection, not JDK-internal reflection.
- **`ProfilePanelController.java:278` `columnToFitMethod.setAccessible(true)`**: Reflects into a JavaFX internal API (`TableColumnHeader`) to call `columnToFit`. This is the risky one — JavaFX internals may be strongly encapsulated. **Flag**: if this breaks on a future JavaFX version, replace with a manual column-fit calculation. No action now (it's working).
- **Test-file `setAccessible` calls** (5 files): test-only reflection into private constructors/fields — acceptable, no action.

---

## 3. Suggested Execution Order

| Order | Step | Phase | Effort | Risk | Value |
|-------|------|-------|--------|------|-------|
| 1 | Step 1 — Toolchains | A | S | Low | Med |
| 2 | Step 2 — Re-enable deprecation lint | A | S | Low | Med |
| 3 | Step 3 — `Date` → `Instant` | B | S | Low-Med | Med |
| 4 | Step 6 — Records for DTOs | C | S | Low | Med |
| 5 | Step 8 — Pattern-matching switch (cherry-pick) | C | S | Low | Low-Med |
| 6 | Step 9 — Virtual threads for FX I/O tasks | C | M | Low-Med | Low-Med |
| 7 | Step 5 — Jetty deprecation fixes | C | M | Med | Med |
| 8 | Step 7 — Jetty virtual threads | C | M | Med-High | **High** |
| 9 | Step 4 — `Worker extends Thread` | B | M-L | Med | Low-Med |
| 10 | Step 10 — Document residual risks | D | S | None | Low |

Legend: Effort S/M/L; Risk Low/Med/High; Value Low/Med/**High**.

---

## 4. Blockers / Questions for the User

1. **WebClient Java 17 pin** — Recommendation is to **keep** it (GWT 2.13 constraint). Confirm you agree, or do you want to explore migrating WebClient off GWT (large, separate effort)?
2. **Virtual threads in Jetty (Step 7)** — This is the highest-value change but requires auditing `synchronized` blocks in the request path to avoid pinning. Are you willing to adopt virtual threads for request handling, and can you provide/permit a load test to validate?
3. **`Worker extends Thread` (Step 4)** — The coupling depth is unknown without a call-site audit. OK to do the audit first and report back before deciding whether to rewrite or just deprecate?
4. **JSON wire format for `lastAction` (Step 3)** — Converting `Date` → `Instant` changes the Gson serialization (epoch-millis → ISO-8601 string). Is there any external client (beyond the in-tree WebClient) that consumes `lastAction`? If yes, we should add a Gson `TypeAdapter` to preserve the format.
5. **Jetty version pinning (Step 5)** — `jetty-*:12.+` floats. Should we pin to a specific 12.x patch before doing the deprecation-API migration, so the replacement APIs are stable?
6. **Scope confirmation** — Do you want me to **execute** this plan (starting with Phase A), or is this assessment-only for now? (The original request said "Do NOT make code changes yet" — I've treated this as assessment-only and will await your go-ahead.)
---

## 5. Execution Summary (2026-07-05)

All user answers: (1) keep WebClient Java 17 pin, (2) adopt Jetty virtual threads, (3) audit then rewrite Worker, (4) no external client for `lastAction`, (5) pin Jetty version, (6) execute starting Phase A.

### Completed

| Step | Status | Summary |
|------|--------|---------|
| Step 1 â€” Toolchains | âœ… Done | Root + subprojects `java {}` migrated from `sourceCompatibility`/`targetCompatibility` to `toolchain { languageVersion = JavaLanguageVersion.of(25) }`. WebClient keeps its own toolchain pin to 17 + `options.release = 17` override (GWT constraint). `foojay-resolver-convention` was already in `settings.gradle`. |
| Step 2 â€” Deprecation lint | âœ… Done | Uncommented `options.compilerArgs += ['-Xlint:deprecation']` in `subprojects` JavaCompile block. All modules compile with **zero unsuppressed deprecation warnings**. |
| Step 3 â€” `Date` â†’ `Instant` | âœ… Done | `WebSession.lastAction` field type changed `Date` â†’ `Instant`. 8 write sites across `ActionsMgr`, `Dat2DirActions`, `Dir2DatActions`, `ProfileActions` (Ã—4), `TrntChkActions` updated `new Date()` â†’ `Instant.now()`. `ImageServlet.dateParse` left as `Date` (HTTP RFC 1123 header parsing â€” correct for protocol context). No external clients consume `lastAction`. |
| Step 6 â€” Records | âœ… Done | `ActionsMgr.UpdateResult`, `UpdateResult.Params`, `SingleCmd` converted from `@RequiredArgsConstructor` classes to `record`s. Unused `lombok.RequiredArgsConstructor` import removed. Gson serializes records natively (â‰¥2.10). All jrmserver tests pass. |
| Step 8 â€” Pattern switch | âœ… Done | `ReportTreeCellRenderer` (jrmstandalone): two `if/else-if instanceof` chains converted to pattern-matching `switch` â€” the value-extraction chain (3 branches) and the `setIcon` chain (12+ branches with `when` guard for `SubjectSet`/leaf interaction). Unnamed pattern `_` used for unused type witnesses. |
| Step 9 â€” FX virtual threads | âœ… Done | 7 I/O-bound `new Thread(task)+setDaemon(true)+start()` sites in jrmfx converted to `Thread.startVirtualThread(task)`: `BatchToolsPanelController` (Dir2Dat, Torrent tasks), `Dir2DatController`, `ProfilePanelController` (ImportDat, UpdateFromMame), `ProgressTaskRunner`, `MainFrame` (Export). CPU-bound `startCompression` and all shutdown hooks left as platform threads. |
| Step 5 â€” Jetty pin + deprecations | âœ… Partial | All Jetty deps pinned from `12.+` to `12.1.10`. `ConnectionLimit` (deprecated forRemoval) â†’ `NetworkConnectionLimit` in both `Server.java` and `FullServer.java` (same constructor signature). `@SuppressWarnings("removal")` removed for ConnectionLimit. **GzipHandler migration deferred** (see below). |
| Step 7 â€” Jetty virtual threads | âœ… Done | `FullServer.createThreadPool()` now calls `pool.setVirtualThreadsExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual()...))`. `Login.login()` `synchronized(cache)` â†’ `ReentrantLock` (avoids virtual thread pinning during DB I/O in auth). `FullServer.windowsStart/Stop` `synchronized(Server.class)` left as-is (not in request path). `Server.java` (simple HTTP server) left with default pool. |
| Step 4 â€” Worker rewrite | âœ… Done | `Worker extends Thread` â†’ `Worker` composing a virtual thread. API preserved (`start()`, `isAlive()`, `getProgress`). All 8 call sites unchanged. Workers now run on virtual threads (I/O-bound profile scans, DAT imports, torrent checks). |
| Step 10 â€” Document risks | âœ… Done | This section. |

### Residual Risks / Follow-ups

1. **`GzipHandler` migration (deferred)**: `org.eclipse.jetty.server.handler.gzip.GzipHandler` is `@Deprecated(since="12.1.1", forRemoval=true)`. The replacement is `org.eclipse.jetty.compression.server.CompressionHandler` (requires adding `jetty-compression-server` + `jetty-compression-gzip` dependencies). The `CompressionHandler` API is different (uses `putCompression(GzipCompression)` + `CompressionConfig` per-path-spec, not `setIncludedMethods`/`setIncludedMimeTypes`/etc.). The `@SuppressWarnings("removal")` on `AbstractServer.gzipHandler()` remains. **Action**: migrate before upgrading to Jetty 13. Since Jetty is pinned to 12.1.10, this is not urgent.

2. **`Server.java` (simple HTTP server)**: Does not use virtual threads (uses Jetty default `QueuedThreadPool`). If this server sees production use, apply the same `setVirtualThreadsExecutor` treatment as `FullServer`.

3. **`Login.validate()` unsynchronized cache access**: `validate()` reads `cache` (a `HashMap`) without holding `cacheLock`. This is a pre-existing race condition (not introduced by this work). Consider switching `cache` to `ConcurrentHashMap` in a follow-up.

4. **`ProgressActions` `synchronized` methods**: `setInfos()`, `extendInfos()`, `cleanup()` are `synchronized`. These are called by worker threads (now virtual threads). `synchronized` on virtual threads can pin, but these methods are quick (no I/O) so pinning is brief. Low risk. Consider `ReentrantLock` if profiling shows contention.

5. **`configureondemand=false`**: The pre-existing `de.esoco.gwt` + `WarPlugin` + configure-on-demand incompatibility with Gradle 9.6.1 was causing build failures. `gradle.properties` changed `org.gradle.configureondemand` from `true` to `false` to fix this. This is a net improvement (the build now works reliably) but loses the configure-on-demand performance optimization. Consider fixing the WebClient `de.esoco.gwt` plugin or upgrading it, then re-enabling configure-on-demand.

6. **Reflection `setAccessible`** (unchanged, awareness only):
   - `EntityBase.java` `field.setAccessible(true)`: domain reflection, safe.
   - `ProfilePanelController.java` `columnToFitMethod.setAccessible(true)`: JavaFX internal API reflection. Works now but may break on future JavaFX versions. No action.

7. **No load test performed**: The virtual thread adoption (Steps 7, 9, and Worker rewrite) is verified by unit tests but not load-tested. Recommend a concurrent-request load test against the server to validate throughput and confirm no pinning issues in production.