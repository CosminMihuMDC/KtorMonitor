# Contributing to KtorMonitor

Thank you for your interest in contributing to **KtorMonitor**! 🎉  
Whether you're fixing a bug, adding a feature, improving documentation, or just opening an issue — every contribution matters.

---

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Project Overview](#project-overview)
4. [Development Environment](#development-environment)
5. [Making Changes](#making-changes)
6. [Code Style](#code-style)
7. [Testing](#testing)
8. [Public API Guidelines](#public-api-guidelines)
9. [Submitting a Pull Request](#submitting-a-pull-request)
10. [Release Process](#release-process)
11. [Getting Help](#getting-help)

---

## Code of Conduct

This project follows the [Kotlin Foundation Code of Conduct](https://kotlinfoundation.org/guidelines/).  
Be respectful, constructive, and inclusive in all interactions.

---

## Getting Started

1. **Fork** the repository and **clone** your fork:
   ```bash
   git clone https://github.com/<your-username>/KtorMonitor.git
   cd KtorMonitor
   ```

2. Create a **feature branch** from `main`:
   ```bash
   git checkout -b feature/my-awesome-feature
   ```

3. Make your changes, write tests, and verify everything builds:
   ```bash
   ./gradlew check
   ```

4. Open a **Pull Request** against `main`.

---

## Project Overview

KtorMonitor is a **Kotlin Multiplatform** library that intercepts and visualizes HTTP traffic from Ktor Client and OkHttp, with a Compose Multiplatform UI for inspecting requests and responses.

### Targets

- Android, iOS (arm64 / simulator arm64)
- Desktop JVM (Windows / macOS / Linux)
- JS (browser), Wasm/JS (browser)

### Module Structure

| Module path                        | Purpose                                                          |
|------------------------------------|------------------------------------------------------------------|
| `core/library`                     | Shared core: DB (SQLDelight), Compose UI, Koin DI               |
| `core/library-no-op`               | API-compatible empty implementation for production builds        |
| `ktor/library-ktor`                | Ktor client `KtorMonitorLogging` plugin                          |
| `ktor/library-ktor-no-op`          | No-op mirror of `library-ktor`                                   |
| `okhttp/library-okhttp`            | `KtorMonitorInterceptor` for OkHttp (Android + JVM)              |
| `okhttp/library-okhttp-no-op`      | No-op mirror of `library-okhttp`                                 |
| `http4k/library-http4k`            | `KtorMonitorFilter` for http4k (Android + JVM)                   |
| `http4k/library-http4k-no-op`      | No-op mirror of `library-http4k`                                 |
| `sample/ktor`                      | Demo app for Ktor monitor (Android, iOS, JVM, JS, Wasm)          |
| `sample/okhttp`                    | Demo app for OkHttp monitor (Android + JVM)                      |
| `sample/http4k`                    | Demo app for http4k monitor (Android + JVM)                      |

---

## Development Environment

### Prerequisites

| Tool             | Minimum version                    |
|------------------|------------------------------------|
| JDK              | 11 (libraries), 21+ (publish / CI) |
| Android SDK      | API 24+                            |
| Xcode            | Required for iOS targets           |
| Kotlin           | 2.3.21                             |

### Building

Always use the Gradle wrapper:

```bash
# Full build (all targets)
./gradlew build

# All checks (lint + tests + ABI)
./gradlew check

# Fast feedback — JVM unit tests only
./gradlew jvmTest
```

### Running Samples

```bash
# Desktop (JVM)
./gradlew :sample:ktor:jvmApp:run
./gradlew :sample:okhttp:jvmApp:run
./gradlew :sample:http4k:jvmApp:run

# Web — JS dev server
./gradlew :sample:ktor:webApp:jsBrowserDevelopmentRun

# Web — Wasm dev server
./gradlew :sample:ktor:webApp:wasmJsBrowserDevelopmentRun

# Android debug APKs
./gradlew :sample:ktor:androidApp:assembleDebug
./gradlew :sample:okhttp:androidApp:assembleDebug
./gradlew :sample:http4k:androidApp:assembleDebug
```

For **iOS**, open `sample/ktor/iosApp/iosApp.xcodeproj` in Xcode and run from there.

---

## Making Changes

### Adding a Feature or Bug Fix

1. Read the relevant source and its tests before changing anything.
2. Keep changes **focused** — one logical change per PR.
3. For UI changes, review `ui/list`, `ui/main`, and `ui/detail` together — Compose state and event flow is shared via Koin ViewModels.
4. For new content types or formatters, update [`ContentType.kt`](core/library/src/commonMain/kotlin/ro/cosminmihu/ktor/monitor/domain/model/ContentType.kt) **and** the corresponding viewer under `ui/detail/formatter/`.
5. UI strings must be added to [`strings.xml`](core/library/src/commonMain/composeResources/values/strings.xml) and referenced via `stringResource(Res.string.<key>)`.

### Adding Dependencies

Do **not** introduce a dependency that is not already in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).  
Add it there first with a versioned alias, then reference `libs.<alias>` in the relevant `build.gradle.kts`.

### Database / SQLDelight

The schema lives in [`Call.sq`](core/library/src/commonMain/sqldelight/ro/cosminmihu/ktor/monitor/db/sqldelight/Call.sq).

- The project uses `generateAsync = true`; always call mutation queries from a `suspend` context.
- Add an `INDEX` whenever you use `ORDER BY` / `WHERE` on a non-primary-key column.

### Coroutines

- Use `withContext(Dispatchers.Default)` for CPU work, `Dispatchers.IO` for blocking I/O.
- **Never call `runBlocking`** from an interceptor — use `InternalLibraryBridge.coroutineScope().launch { }` for fire-and-forget DB writes.
- Avoid `GlobalScope`. Use the single library scope provided by Koin via `InternalLibraryBridge.coroutineScope()`.

### Multiplatform `expect`/`actual`

When adding a new platform target, provide `actual` declarations for:  
`ShareManager`, `ClipboardManager`, `NotificationManager`, `NotificationPermissionBanner`, `DatabaseDriverFactory`.

---

## Code Style

- **Kotlin official style**, 4-space indentation, trailing commas on multi-line argument lists.
- `explicitApi()` is enforced — every public symbol must declare `public` and have an explicit return type.
- All public symbols **must** carry KDoc. Include a fenced `kotlin` usage example when relevant.

### Compose Multiplatform

- `@Composable` functions use **PascalCase** and return `Unit`.
- First optional parameter must be `modifier: Modifier = Modifier`.
- Prefer **hoisted state**; UI state classes are immutable `data class`es.
- Use `collectAsStateWithLifecycle()` over `collectAsState()`.
- Provide `key = { it.id }` (and `contentType = ...` where items differ) on every `LazyColumn`/`LazyRow` `items(…)` block.
- Apply each `Modifier` only once (e.g., when wrapping `SelectionContainer(modifier = modifier)`, the inner layout uses a fresh `Modifier`).
- Cache heavyweight builders (e.g. `ImageLoader`) with `remember`.

### Accessibility

- Every actionable `Icon` / `IconButton` / `Image` must have a meaningful `contentDescription = stringResource(Res.string.…)`. Pure decorations use `null`.
- Touch targets must be ≥ 48 dp; rely on `LocalMinimumInteractiveComponentSize` instead of hardcoded small `size(…)` modifiers.
- Encode status (success / redirect / error) with **both** color and icon/text — never color alone.

---

## Testing

```bash
# Fast feedback — JVM unit tests
./gradlew jvmTest

# Full check (lint + all-target tests + ABI)
./gradlew check
```

- Add unit tests for new domain logic and use cases.
- UI composables can be tested with Compose testing APIs.
- Don't commit changes to `kotlin-js-store/`, `build/`, `*.iml`, or `local.properties`.

---

## Public API Guidelines

### `explicitApi()` & KDoc

Every public symbol must be annotated `public`, declare a return type, and carry KDoc.

### no-op Mirrors

Every published module has a matching `*-no-op` module (e.g., `library-ktor` → `library-ktor-no-op`).  
**Whenever you add or change a public symbol, mirror it in the corresponding no-op module** (same package, types, signatures, defaults).

### ABI Validation

The project uses [`binary-compatibility-validator`](https://github.com/Kotlin/binary-compatibility-validator).  
`apiValidation { publicPackages.add("ro.cosminmihu.ktor.monitor") }` keeps `db.*` and `ui.*` packages out of the public ABI.

```bash
# Verify no unintended ABI changes
./gradlew apiCheck

# Regenerate after intentional ABI changes; commit the updated *.api files
./gradlew apiDump
```

Always run `apiCheck` before pushing changes that touch public types.  
Run `apiDump` after intentional ABI changes and commit the regenerated `*/api/*.api` files together with the no-op mirror update.

### Internal API

The marker `@InternalKtorMonitorApi` (RequiresOptIn, level ERROR) gates cross-module internals exposed via `InternalLibraryBridge`.  
**Never** call `InternalLibraryBridge` from outside library modules.

---

## Submitting a Pull Request

1. Ensure **all checks pass** locally:
   ```bash
   ./gradlew check
   ```
2. If you changed any public API, regenerate and commit the ABI dump:
   ```bash
   ./gradlew apiDump
   ```
3. Update [`CHANGELOG.md`](CHANGELOG.md) with a short description under the appropriate version section.
4. Fill in the PR description:
   - **What** changed and **why**.
   - Link to the related issue (if any): `Closes #<issue-number>`.
   - Screenshots / recordings for UI changes.
5. Keep the PR **focused** — unrelated changes belong in a separate PR.
6. A maintainer will review your PR. Be prepared to make requested changes.

---

## Release Process *(maintainers only)*

1. Bump `version` in root [`build.gradle.kts`](build.gradle.kts) and update [`CHANGELOG.md`](CHANGELOG.md).
2. Run `./gradlew apiCheck && ./gradlew check` locally.
3. Tag the commit and create a GitHub Release; the `publish.yml` CI workflow will:
   - Run `apiCheck`.
   - Regenerate Dokka API docs.
   - Publish artifacts to Maven Central.

---

## Getting Help

- 💬 **Slack:** [#ktormonitor](https://kotlinlang.slack.com/archives/C0AB9GA32H0) on the Kotlin Slack workspace.
- 🐛 **Bug reports / feature requests:** [Open an issue](https://github.com/CosminMihuMDC/KtorMonitor/issues/new).
- 📖 **Documentation:** [cosminmihumdc.github.io/KtorMonitor](https://cosminmihumdc.github.io/KtorMonitor)
- 📦 **API reference:** [cosminmihumdc.github.io/KtorMonitor/api](https://cosminmihumdc.github.io/KtorMonitor/api)

---

*Thank you for helping make KtorMonitor better! 🙏*

