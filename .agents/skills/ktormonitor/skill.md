---
name: ktormonitor
version: 1.14.4
description: >
  KtorMonitor is a Kotlin Multiplatform library for real-time HTTP traffic monitoring.
  It supports Ktor Client, OkHttp, and http4k on Android, iOS, Desktop JVM, Wasm, and JS.
  Use this skill when a developer needs to integrate, configure, display, or troubleshoot
  HTTP monitoring in a Kotlin or Android project.
tags:
  - kotlin
  - android
  - ios
  - kotlin-multiplatform
  - ktor
  - okhttp
  - http4k
  - compose-multiplatform
  - networking
  - debugging
---

# KtorMonitor Skill

## What this skill does

When a developer asks to:
- Add HTTP traffic monitoring to a Kotlin / Android / KMP project
- Inspect Ktor Client, OkHttp, or http4k requests and responses
- Configure log retention, body size limits, or header sanitization
- Display the KtorMonitor UI on any platform
- Exclude monitoring code from production (release) builds
- Fix issues with KtorMonitor (notifications, body truncation, desugaring, etc.)

…follow the steps below in order, adapting to the developer's specific platform and HTTP client.

---

## Step 1 — Identify the HTTP client and platform

| HTTP client | Supported platforms |
|---|---|
| Ktor Client | Android, iOS, Desktop JVM, Wasm, JS |
| OkHttp | Android, Desktop JVM |
| http4k | Android, Desktop JVM |

---

## Step 2 — Add the dependency

### Ktor Client — Kotlin Multiplatform

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("ro.cosminmihu.ktor:ktor-monitor-logging:1.14.4")
        }
    }
}
```

**No-op for release builds** (KMP):

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("ro.cosminmihu.ktor:ktor-monitor-logging-no-op:1.14.4")
        }
    }
}
```

### Ktor Client — Android only

```kotlin
// build.gradle.kts (app module)
dependencies {
    debugImplementation("ro.cosminmihu.ktor:ktor-monitor-logging:1.14.4")
    releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-logging-no-op:1.14.4")
}
```

### OkHttp — Android & JVM

```kotlin
dependencies {
    debugImplementation("ro.cosminmihu.ktor:ktor-monitor-okhttp-interceptor:1.14.4")
    releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-okhttp-interceptor-no-op:1.14.4")
}
```

### http4k — Android & JVM

```kotlin
dependencies {
    debugImplementation("ro.cosminmihu.ktor:ktor-monitor-http4k-filter:1.14.4")
    releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-http4k-filter-no-op:1.14.4")
}
```

> **Android minSdk < 26**: add Core Library Desugaring:
> ```kotlin
> android {
>     compileOptions { isCoreLibraryDesugaringEnabled = true }
> }
> dependencies {
>     coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
> }
> ```

---

## Step 3 — Install the plugin / interceptor / filter

### Ktor Client Plugin

```kotlin
import ro.cosminmihu.ktor.monitor.KtorMonitorLogging
import ro.cosminmihu.ktor.monitor.RetentionPeriod
import ro.cosminmihu.ktor.monitor.ContentLength

val client = HttpClient {
    install(KtorMonitorLogging) {
        // Replace matching header values with "***" in the logs
        sanitizeHeader { header -> header == "Authorization" }

        // Return true to SKIP logging for a request
        filter { request -> !request.url.host.contains("example.com") }

        // Show OS notification summarising active requests (Android & iOS only)
        showNotification = true

        // How long captured calls are kept. Default: OneHour
        retentionPeriod = RetentionPeriod.OneHour

        // Max body bytes captured. Default: 250 000. Use Full to disable the limit.
        maxContentLength = ContentLength.Default
    }
}
```

### OkHttp Interceptor

> Always use `addNetworkInterceptor` — it sees actual network traffic including redirects.

```kotlin
import ro.cosminmihu.ktor.monitor.KtorMonitorInterceptor
import ro.cosminmihu.ktor.monitor.RetentionPeriod
import ro.cosminmihu.ktor.monitor.ContentLength

val client = OkHttpClient.Builder()
    .addNetworkInterceptor(
        KtorMonitorInterceptor {
            sanitizeHeader { header -> header == "Authorization" }
            filter { request -> !request.url.host.contains("example.com") }
            showNotification = true
            retentionPeriod = RetentionPeriod.OneHour
            maxContentLength = ContentLength.Default
        }
    )
    .build()
```

### http4k Filter

```kotlin
import ro.cosminmihu.ktor.monitor.KtorMonitorFilter
import ro.cosminmihu.ktor.monitor.RetentionPeriod
import ro.cosminmihu.ktor.monitor.ContentLength

val app = KtorMonitorFilter {
    sanitizeHeader { header -> header == "Authorization" }
    filter { request -> !request.uri.host.contains("example.com") }
    showNotification = true          // Android only for http4k
    retentionPeriod = RetentionPeriod.OneHour
    maxContentLength = ContentLength.Default
}.then(JavaHttpClient())
```

---

## Step 4 — Add the UI for the target platform

### Compose Multiplatform (all targets)

```kotlin
import ro.cosminmihu.ktor.monitor.KtorMonitor

@Composable
fun App() {
    KtorMonitor()
}
```

### Android

Embed `KtorMonitor()` in any Composable. When `showNotification = true` and
`android.permission.POST_NOTIFICATIONS` is granted, tapping the notification opens the
monitor automatically — no extra Activity setup needed.

### iOS

```kotlin
// Kotlin shared module
import ro.cosminmihu.ktor.monitor.KtorMonitorViewController

fun MainViewController() = KtorMonitorViewController()
```

```swift
// SwiftUI
struct KtorMonitorView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        KtorMonitorView().ignoresSafeArea()
    }
}
```

### Desktop — Compose (Window + optional system tray)

```kotlin
import ro.cosminmihu.ktor.monitor.KtorMonitorWindow
import ro.cosminmihu.ktor.monitor.KtorMonitorMenuItem

fun main() = application {
    var showKtorMonitor by rememberSaveable { mutableStateOf(false) }

    Tray(icon = painterResource(Res.drawable.ic_launcher), menu = {
        KtorMonitorMenuItem { showKtorMonitor = true }
    })

    KtorMonitorWindow(
        show = showKtorMonitor,
        onCloseRequest = { showKtorMonitor = false }
    )

    Window(onCloseRequest = ::exitApplication) { App() }
}
```

### Desktop — Swing

```kotlin
import ro.cosminmihu.ktor.monitor.KtorMonitorPanel

fun main() {
    SwingUtilities.invokeLater {
        val frame = JFrame("Ktor Monitor")
        frame.add(KtorMonitorPanel, BorderLayout.CENTER)
        frame.isVisible = true
    }
}
```

### Wasm / JS (browser)

```kotlin
// build.gradle.kts — add webpack helper
kotlin {
    sourceSets {
        webMain.dependencies {
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
        }
    }
}
```

```javascript
// {project}/webpack.config.d/sqljs.js
config.resolve = { fallback: { fs: false, path: false, crypto: false } };
const CopyWebpackPlugin = require('copy-webpack-plugin');
config.plugins.push(new CopyWebpackPlugin({
    patterns: ['../../node_modules/sql.js/dist/sql-wasm.wasm']
}));
```

```kotlin
// Main entry point
fun main() {
    ComposeViewport(document.body!!) { App() }
}
```

---

## Configuration reference

| Option | Type | Default | Description |
|---|---|---|---|
| `sanitizeHeader` | `(String) -> Boolean` | — | Matching header values are replaced with `***` |
| `filter` | `(request) -> Boolean` | — | Matching requests are **not** logged |
| `showNotification` | `Boolean` | `true` | OS notification (Android & iOS only; needs permission) |
| `retentionPeriod` | `RetentionPeriod` | `OneHour` | How long calls are stored locally |
| `maxContentLength` | `ContentLength` | `Default` (250 000 B) | Truncation limit; `Full` disables it |

### RetentionPeriod values

```
OneMinute · ThirtyMinutes · OneHour · TwoHours · OneDay · OneWeek
```

### ContentLength values

```
ContentLength.Default   // 250 000 bytes
ContentLength.Full      // no limit
```

---

## No-op artifact map (production safety)

| Debug | Release / No-op |
|---|---|
| `ktor-monitor-logging` | `ktor-monitor-logging-no-op` |
| `ktor-monitor-okhttp-interceptor` | `ktor-monitor-okhttp-interceptor-no-op` |
| `ktor-monitor-http4k-filter` | `ktor-monitor-http4k-filter-no-op` |
| `ktor-monitor-core` | `ktor-monitor-core-no-op` |

All no-op variants are ABI-identical to their counterparts — zero code changes needed when switching.

---

## Common questions & troubleshooting

### Body appears truncated in the UI
Set `maxContentLength = ContentLength.Full` to capture the entire body.

### No notification appears on Android
Grant `android.permission.POST_NOTIFICATIONS` at runtime and ensure `showNotification = true`.

### OkHttp — some requests are missing
Use `addNetworkInterceptor`, not `addInterceptor`. The network interceptor sees all traffic.

### http4k — streaming responses are not captured
Only materialized bodies are supported. Streaming bodies are skipped.

### Build fails on Android with `minSdk < 26`
Enable Core Library Desugaring (see Step 2 note above).

### WebSocket / SSE support?
Yes — KtorMonitorLogging captures WebSocket frames and Server-Sent Events automatically.

### Which body formats get rich preview?
JSON, XML, HTML, CSS, YAML, Markdown, Form Data, Multipart, Images (JPG / PNG / SVG / GIF / WEBP), Hex.

---

## Resources

| Resource | URL |
|---|---|
| Documentation | https://cosminmihumdc.github.io/KtorMonitor |
| API reference | https://cosminmihumdc.github.io/KtorMonitor/api |
| GitHub | https://github.com/CosminMihuMDC/KtorMonitor |
| Maven Central | https://search.maven.org/artifact/ro.cosminmihu.ktor/ktor-monitor-logging |
| Slack (#ktormonitor) | https://kotlinlang.slack.com/archives/C0AB9GA32H0 |
| klibs.io | https://klibs.io/project/CosminMihuMDC/KtorMonitor |
| Context7 | https://context7.com/cosminmihumdc/ktormonitor |
