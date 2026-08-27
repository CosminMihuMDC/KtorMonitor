# AGENTS.md — KtorMonitor

Operational guide for AI coding agents (and new contributors) working in this repository.

> Keep this file up to date when build commands, module layout, conventions or coding standards change.

---

## 1. Project overview

**KtorMonitor** is a Kotlin Multiplatform library that intercepts and visualizes HTTP traffic from
[Ktor Client](https://ktor.io/) and [OkHttp](https://square.github.io/okhttp/), with a Compose
Multiplatform UI for inspecting requests and responses.

- **Targets:** Android, iOS (arm64 / simulator arm64), Desktop JVM (Windows / macOS / Linux), JS (browser), Wasm/JS (browser).
- **Tech stack:** Kotlin **2.3.21**, Compose Multiplatform **1.11.0-beta01**, Ktor **3.4.3**, OkHttp **5.3.2**, http4k **6.15.1.0**, SQLDelight **2.3.2** (async driver, `sql.js` on web), Koin **4.2.1**, Coil **3.4.0**, kotlinx.atomicfu, kotlinx.coroutines, kotlinx.datetime, kotlinx.serialization.
- **Group / artifacts** (group `ro.cosminmihu.ktor`):
  - `ktor-monitor-core`            — shared core (UI + storage)
  - `ktor-monitor-core-no-op`      — ABI-equivalent no-op
  - `ktor-monitor-logging`         — Ktor client plugin
  - `ktor-monitor-logging-no-op`
  - `ktor-monitor-okhttp-interceptor`        — OkHttp interceptor (Android & JVM)
  - `ktor-monitor-okhttp-interceptor-no-op`
  - `ktor-monitor-http4k-filter`             — http4k Filter (Android & JVM)
  - `ktor-monitor-http4k-filter-no-op`
- **Distribution:** Maven Central, signed via `com.vanniktech.maven.publish`, ABI checked via `binary-compatibility-validator`, docs via Dokka + MkDocs Material (`docs/`).

## 2. Module structure

| Path                              | Plugin / target            | Purpose                                                                 |
|-----------------------------------|----------------------------|-------------------------------------------------------------------------|
| `core/library`                    | KMP library (all targets)  | DB (SQLDelight), Compose UI, Koin DI, notification & share managers.    |
| `core/library-no-op`              | KMP library                | API-compatible empty implementation for production builds.              |
| `ktor/library-ktor`               | KMP library                | Ktor client `KtorMonitorLogging` plugin; depends on `core/library`.     |
| `ktor/library-ktor-no-op`         | KMP library                | No-op mirror of `library-ktor`.                                         |
| `okhttp/library-okhttp`           | KMP (Android + JVM)        | `KtorMonitorInterceptor` for OkHttp; depends on `core/library`.         |
| `okhttp/library-okhttp-no-op`     | KMP (Android + JVM)        | No-op mirror of `library-okhttp`.                                       |
| `http4k/library-http4k`           | KMP (Android + JVM)        | `KtorMonitorFilter` for http4k; depends on `core/library`.              |
| `http4k/library-http4k-no-op`     | KMP (Android + JVM)        | No-op mirror of `library-http4k`.                                       |
| `sample/ktor/shared`              | KMP shared module          | Shared business/UI code for Ktor sample.                                 |
| `sample/ktor/androidApp`          | Android application        | Android app for Ktor sample.                                             |
| `sample/ktor/jvmApp`              | JVM desktop application    | Desktop app for Ktor sample.                                             |
| `sample/ktor/webApp`              | KMP web app module         | JS + Wasm browser targets for Ktor sample.                               |
| `sample/okhttp/shared`            | KMP shared module          | Shared business/UI code for OkHttp sample.                               |
| `sample/okhttp/androidApp`        | Android application        | Android app for OkHttp sample.                                           |
| `sample/okhttp/jvmApp`            | JVM desktop application    | Desktop app for OkHttp sample.                                           |
| `sample/http4k/shared`            | KMP shared module          | Shared business/UI code for http4k sample.                               |
| `sample/http4k/androidApp`        | Android application        | Android app for http4k sample.                                           |
| `sample/http4k/jvmApp`            | JVM desktop application    | Desktop app for http4k sample.                                           |
| `docs/`                           | MkDocs Material            | Documentation site, including generated Dokka API docs in `docs/api/`.  |

Type-safe project accessors are enabled (`enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`); reference modules as `projects.core.library`, `projects.ktor.libraryKtor`, `projects.http4k.libraryHttp4k`, etc.

### Source-set layout (core)

```
core/library/src/
  commonMain/   — shared Kotlin + Compose UI (ui/, db/, di/, domain/, core/)
  commonMain/sqldelight/  — SQLDelight schema (Call.sq → ro.cosminmihu.ktor.monitor.db.sqldelight)
  commonMain/composeResources/  — Compose Multiplatform resources (drawables, strings)
  androidMain/  — Activity, notification channel, clipboard, share, permission banner
  iosMain/      — UNUserNotificationCenter, UIPasteboard, UIActivityViewController
  jvmMain/      — Swing/AWT clipboard & share, KtorMonitorWindow / Panel / MenuItem
  jsMain/ wasmJsMain/ webMain/ — Web fallbacks; SQLDelight web-worker driver, sql.js webpack plugin
```

## 3. Build, test, run

A wrapper is included; always use `./gradlew`. JVM toolchain is **Java 11** for the libraries and the CI publish job uses **Java 21+**.

| Goal                                  | Command                                                               |
|---------------------------------------|-----------------------------------------------------------------------|
| Full build                            | `./gradlew build`                                                     |
| All checks (lint + tests + apiCheck)  | `./gradlew check`                                                     |
| JVM unit tests (run in CI)            | `./gradlew jvmTest`                                                   |
| Android sample APKs (debug + release) | `./gradlew :sample:ktor:androidApp:assembleDebug :sample:okhttp:androidApp:assembleDebug :sample:http4k:androidApp:assembleDebug` |
| Desktop sample run (JVM)              | `./gradlew :sample:ktor:jvmApp:run` / `:sample:okhttp:jvmApp:run` / `:sample:http4k:jvmApp:run` |
| Desktop installer (current OS)        | `./gradlew :sample:ktor:jvmApp:packageDmg` (mac) / `packageMsi` / `packageDeb` |
| Web (JS) dev server                   | `./gradlew :sample:ktor:webApp:jsBrowserDevelopmentRun`               |
| Wasm dev server                       | `./gradlew :sample:ktor:webApp:wasmJsBrowserDevelopmentRun`           |
| iOS                                   | Open `sample/ktor/iosApp/iosApp.xcodeproj` in Xcode and run.          |
| ABI check (binary-compatibility)      | `./gradlew apiCheck`                                                  |
| ABI dump (after intentional changes)  | `./gradlew apiDump`                                                   |
| API docs (Dokka HTML → docs/docs/api) | `./gradlew dokkaGenerate`                                             |
| Publish to Maven Central (CI only)    | `./gradlew publishAndReleaseToMavenCentral --no-configuration-cache`  |
| Documentation site (local)            | `pip install mkdocs-material && mkdocs serve --config-file docs/mkdocs.yml` |

CI workflows live in [`.github/workflows/`](.github/workflows): `build.yml` (PR/push) and `publish.yml` (release).

## 4. Conventions & code style

### Public API hygiene
- **`explicitApi()` is enforced** in every published library module — every public symbol must declare `public` and have a return type.
- All public symbols **must** carry KDoc. Show usage in a fenced `kotlin` block when relevant.
- The library uses an opt-in marker [`@InternalKtorMonitorApi`](core/library/src/commonMain/kotlin/ro/cosminmihu/ktor/monitor/InternalLibraryBridge.kt) (RequiresOptIn, level ERROR) to gate cross-module internals exposed via `InternalLibraryBridge`. **Never** call `InternalLibraryBridge` from outside library modules.
- `apiValidation { publicPackages.add("ro.cosminmihu.ktor.monitor") }` keeps the `db.*`, `ui.*` packages out of the public ABI. Run `./gradlew apiCheck` before pushing changes that touch public types; run `apiDump` after intentional ABI changes and commit the updated `*/api/*.api` files.
- The **no-op** modules must mirror the public ABI of their non-no-op counterpart (same package, types, signatures, defaults). When you add or change a public symbol, mirror it in the matching `*-no-op` module.

### Kotlin / Compose
- Kotlin official style, 4-space indentation, trailing commas on multi-line argument lists.
- Compose Multiplatform conventions:
  - `@Composable` functions are PascalCase, return `Unit`.
  - First optional parameter must be `modifier: Modifier = Modifier`.
  - Prefer hoisted state; UI state classes are immutable `data class`es.
  - Prefer `collectAsStateWithLifecycle()` (already used) over `collectAsState()` for ViewModel flows.
  - Provide `key = { it.id }` (and `contentType = ...` when items differ) on every `LazyColumn`/`LazyRow` `items(...)` block.
  - Apply each `Modifier` only once: when wrapping with `SelectionContainer(modifier = modifier)`, the inner layout must use a fresh `Modifier`.
  - `Modifier.semantics { contentDescription = ... }` — never write `Modifier.semantics { someString }` (no-op lambda).
  - Cache heavyweight builders (e.g. Coil `ImageLoader`) with `remember`.
- Coroutines:
  - Use `withContext(Dispatchers.Default)` for CPU work and `Dispatchers.IO` for blocking IO. Database mutations go through SQLDelight's async APIs (`generateAsync = true`).
  - **Never call `runBlocking` from an interceptor** — use `InternalLibraryBridge.coroutineScope().launch { ... }` for fire-and-forget DB writes.
  - Avoid `GlobalScope`. The single library scope is provided by Koin and exposed via `InternalLibraryBridge.coroutineScope()`.
- Multiplatform:
  - The project enables `-Xexpect-actual-classes` (transitional). New `expect class` declarations should remain `internal` whenever possible.
  - When adding a new platform target, add the matching `actual` declarations for `ShareManager`, `ClipboardManager`, `NotificationManager`, `NotificationPermissionBanner`, `DatabaseDriverFactory`.
- Logging: keep all UI strings in `core/library/src/commonMain/composeResources/values/strings.xml` and reference them with `stringResource(Res.string....)`.

### Data layer
- DB schema is defined in [`Call.sq`](core/library/src/commonMain/sqldelight/ro/cosminmihu/ktor/monitor/db/sqldelight/Call.sq). Headers are stored as JSON via [`HeadersAdapter`](core/library/src/commonMain/kotlin/ro/cosminmihu/ktor/monitor/db/HeadersAdapter.kt). Add an `INDEX` whenever you introduce an `ORDER BY` / `WHERE` on a non-primary-key column.
- Long-running export use cases (`ExportCall*UseCase`) must wrap CPU work in `withContext(Dispatchers.Default)`.

### Accessibility (a11y)
- Every actionable `Icon`/`IconButton`/`Image` must carry a meaningful `contentDescription = stringResource(Res.string.…)`. Pure decorations may use `null`.
- Group decoration + label inside an actionable element with `Modifier.semantics(mergeDescendants = true) { contentDescription = ... }`.
- Touch targets must be ≥ 48 dp; rely on `LocalMinimumInteractiveComponentSize` instead of hard-coding small `size(...)` modifiers on click targets.
- Encode status (success / redirect / error) with both color **and** icon/text, never color alone.

## 5. Working with the agent — guidelines

- Before changing public API, read the matching `*/api/*.api` files; after the change run `./gradlew apiDump` and commit the regenerated `.api` files **and** mirror the change in the corresponding `*-no-op` module.
- Before editing UI, prefer reading `ui/list`, `ui/main`, `ui/detail` together — Compose state and event flow is shared across them via Koin ViewModels.
- When adding strings, add them to `core/library/src/commonMain/composeResources/values/strings.xml` (and translations if present); the generated accessor is `Res.string.<key>`.
- When adding a new ContentType or formatter, update [`ContentType.kt`](core/library/src/commonMain/kotlin/ro/cosminmihu/ktor/monitor/domain/model/ContentType.kt) and the corresponding viewer under `ui/detail/formater/`. Avoid wildcard MIME entries that match every string via `String.contains`.
- Never introduce dependencies that are not already in [`gradle/libs.versions.toml`](gradle/libs.versions.toml); add them there first with a versioned alias.
- Use `./gradlew jvmTest` for fast feedback. Full `check` is required for ABI, lint and all-target test runs.
- Don't commit changes to `kotlin-js-store/`, `build/`, `*.iml`, or local `local.properties`.

## 6. Release process (maintainers)

1. Bump `version` in root [`build.gradle.kts`](build.gradle.kts) and update [`CHANGELOG.md`](CHANGELOG.md).
2. `./gradlew apiCheck && ./gradlew check` locally.
3. Tag and create a GitHub Release; the `publish.yml` workflow runs `apiCheck`, regenerates docs and publishes to Maven Central.

## 7. Known gotchas

- **OkHttp interceptor** runs on OkHttp's HTTP dispatcher thread. The DB writes are dispatched onto the library coroutine scope (do **not** call `runBlocking` here). Response bodies are read via `Response.peekBody(maxContentLength)` to avoid loading multi-megabyte payloads.
- **http4k Filter** runs synchronously on the caller's thread (http4k is a synchronous, functional framework). The DB writes are dispatched onto the library coroutine scope (do **not** call `runBlocking` here). Request/response bodies are read via `Body.payload.duplicate()` to avoid consuming the original `ByteBuffer`; streaming bodies are not supported in v1.
- **Ktor plugin** wires into `HttpSendPipeline.Monitoring`, `HttpReceivePipeline.State`, `HttpResponsePipeline.Receive`, and an additional `ResponseObserver`. When changing hook order, retest streaming (SSE) and WebSocket flows.
- **SQLDelight** uses `generateAsync = true`; always call mutation queries from a `suspend` context.
- **Web targets** require the `sql.js` wasm artifact to be copied via the webpack config in `core/library/webpack.config.d/sqljs-config.js` (and mirrored in `sample/ktor/webpack.config.d/`).
- **iOS** binaries link against `sqlite3` (`linkerOpts("-lsqlite3")`); keep this when adding new iOS targets.
- The Android `minSdk` is **24**; users below `26` must enable Core Library Desugaring (kotlinx-datetime / `java.time`).

