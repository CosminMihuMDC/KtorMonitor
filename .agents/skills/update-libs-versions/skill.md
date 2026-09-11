---
name: update-libs-versions
description: Update dependency versions in gradle/libs.versions.toml by querying Maven repositories.
version: 1.0.0
tags:
  - gradle
  - version-catalog
  - maven
  - dependencies
---

# Update `libs.versions.toml` from Maven metadata

Use this skill when a developer asks to quickly refresh dependency versions in `gradle/libs.versions.toml`.

## Goal

Update values from `[versions]` that are referenced by `[libraries]` entries, using live Maven repository metadata.

## Constraints

- Edit only `gradle/libs.versions.toml`.
- Keep existing aliases and formatting style intact.
- Do not change `android-*Sdk` numeric SDK values.
- Prefer stable releases (`alpha`, `beta`, `rc`, `m`, `eap`, `dev`, `snapshot` are pre-release).
- If current value is pre-release, allow pre-release updates in the same train.

## Repositories to query (in this order)

1. `https://dl.google.com/android/maven2`
2. `https://repo1.maven.org/maven2`
3. `https://maven.pkg.jetbrains.space/public/p/compose/dev`

For a dependency `group:artifact`, query:

`{repo}/{group as path}/{artifact}/maven-metadata.xml`

Example:

`io/ktor/ktor-client-core/maven-metadata.xml`

## Fast update algorithm

1. Parse `gradle/libs.versions.toml`.
2. Build a map: `version.ref -> representative module(s)` from `[libraries]`.
3. Skip version refs that are not used by `[libraries]`.
4. For each version ref, query repository metadata for candidate modules in parallel.
5. Pick the highest matching version:
   - Prefer stable versions unless the current version is pre-release.
   - Ignore invalid/non-semver tags when a valid stable exists.
6. Update only changed `[versions]` values.
7. Print a compact report: alias, old, new, source repository.

## Documentation sync (required)

After updating `gradle/libs.versions.toml`, also update version badges in the Acknowledgments sections to keep docs consistent.

Files to update:

- `README.md` -> `## 🙌 Acknowledgments`
- `docs/docs/contributing.md` -> `## 🙌 Acknowledgments`

Update badge versions for stack items that are shown there (for example: Kotlin, Compose Multiplatform, Ktor, OkHttp, http4k, SQLDelight, Android API when applicable).

Rule: docs must reflect the final versions from `gradle/libs.versions.toml` (or platform source of truth for Android API level).

## Practical notes

- Use HTTP timeouts and continue on per-artifact failures.
- Cache successful metadata responses per `group:artifact` during the run.
- If an alias maps to many modules, one successful module metadata response is enough.

## Validation

After update, run:

```bash
./gradlew clean build
```

If build fails due to incompatible upgrades, keep the alias unchanged and report it.

## Optional commit format (only when explicitly requested)

If the developer asks for a commit, use a commit message that includes one line per updated dependency in this format:

- `name: old version -> new version`

Example:

- `kotlin: 2.4.10 -> 2.4.20`
- `ktor: 3.5.1 -> 3.5.2`
- `okhttp: 5.4.0 -> 5.5.0`

## Expected output format

- `Updated: ktor 3.5.1 -> 3.5.2 (repo1.maven.org)`
- `Unchanged: sqldelight 2.3.2`
- `Skipped: android-compileSdk (non-Maven platform value)`