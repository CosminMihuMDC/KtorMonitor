[![Maven Central](https://img.shields.io/maven-central/v/ro.cosminmihu.ktor/ktor-monitor-logging?logo=apachemaven&label=Maven%20Central&link=https://search.maven.org/artifact/ro.cosminmihu.ktor/ktor-monitor-logging/)](https://search.maven.org/artifact/ro.cosminmihu.ktor/ktor-monitor-logging)
[![License](https://img.shields.io/github/license/CosminMihuMDC/KtorMonitor?label=License&logo=lintcode&logoColor=white&color=#3DA639)](https://github.com/CosminMihuMDC/KtorMonitor/blob/main/LICENSE)
![Platforms](https://img.shields.io/badge/Platforms-Android%20+%20iOS%20+%20JVM%20+%20Wasm-brightgreen?logo=bambulab&logoColor=white&color=8d69e0)
[![JetBrains Klibs](https://img.shields.io/badge/JetBrains-klibs-4284F3?logo=jetbrains&logoColor=white)](https://klibs.io/project/CosminMihuMDC/KtorMonitor)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-F05032.svg?logo=git&logoColor=white)](http://makeapullrequest.com)
[![GitHub stars](https://img.shields.io/github/stars/CosminMihuMDC/KtorMonitor)](https://github.com/CosminMihuMDC/KtorMonitor)
[![GitHub forks](https://img.shields.io/github/forks/CosminMihuMDC/KtorMonitor)](https://github.com/CosminMihuMDC/KtorMonitor/fork)

# <img src="./extra/ktor_ic_launcher.svg" width="35"/> KtorMonitor
Powerful tool to monitor [Ktor Client](https://ktor.io/) requests and responses, making it easier to debug and analyze network communication.

<img src="extra/readme/ktormonitor_android.gif" width="200"/>
&emsp;
<img src="extra/readme/ktomonitor_ios.gif" width="210"/>
&emsp;
<img src="extra/readme/ktormonitor_desktop.gif" width="430"/>
&emsp;
<img src="extra/readme/ktormonitor_wasm.gif" width="430"/>

By default, **```KtorMonitor```**:
- **android** -> is enabled for ```debug``` builds and disabled for ```release``` builds
- **ios** -> is enabled for ```debug``` builds and disabled for ```release``` builds
- **desktop** -> is enabled for all builds
- **wasm** -> is enabled for all builds

## Setup

### <img src="https://upload.wikimedia.org/wikipedia/commons/6/6b/Gradle_logo.svg" width="100"/>

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("ro.cosminmihu.ktor:ktor-monitor-logging:1.7.2")
        }
    }
}
```

In order to isolate KtorMonitor from release builds, use `ktor-monitor-logging-no-op` variant:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("ro.cosminmihu.ktor:ktor-monitor-logging-no-op:1.7.2")
        }
    }
}
```

### <img src="https://resources.jetbrains.com/storage/products/company/brand/logos/Ktor_icon.png" width="30"/> Install Ktor Client Plugin

```kotlin
HttpClient {
	
    install(KtorMonitorLogging) {  
        sanitizeHeader { header -> header == "Authorization" }  
        filter { request -> !request.url.host.contains("cosminmihu.ro") }  
        showNotification = true  
        retentionPeriod = RetentionPeriod.OneHour
        maxContentLength = ContentLength.Default
    }
    
}
```

- ```sanitizeHeader``` - sanitize sensitive headers to avoid their values appearing in the logs
- ```filter``` - filter logs for calls matching a predicate.
- ```showNotification``` - Keep track of latest requests and responses into notification. Default is **true**. Android and iOS only. Notifications permission needs to be granted.
- ```retentionPeriod``` - The retention period for the logs. Default is **1h**.
- ```maxContentLength``` - The maximum length of the content that will be logged. After this, body will be truncated. Default is **250_000**. To log the entire body use ```ContentLength.Full```.

## 🧩 Integration

Check out below how to integrate Ktor Monitor for different platforms.

<details>
<summary><b>Compose Multiplatform (all platforms)</b></summary>

* Use ```KtorMonitor``` Composable

```kotlin
@Composable
fun Composable() {
    KtorMonitor()
}
```
</details>

<details>
<summary><b>Android</b></summary>

- If ```showNotifcation = true``` and **android.permission.POST_NOTIFICATIONS** is granted, the library will display a notification showing a summary of ongoing KTOR activity. Tapping on the notification launches the full ```KtorMonitor```.
- Apps can optionally use the ```KtorMonitor()``` Composable directly into own Composable code.
</details>

<details>
<summary><b>Desktop Compose</b></summary>

* Use ```KtorMonitorWindow``` Composable

```kotlin
fun main() = application {

    var showKtorMonitor by rememberSaveable { mutableStateOf(false) }
    KtorMonitorWindow(
        onCloseRequest = { showKtorMonitor = false },
        show = showKtorMonitor
    )

}
```

* Use ```KtorMonitorWindow``` Composable with ```KtorMonitorMenuItem```

```kotlin
fun main() = application {

    var showKtorMonitor by rememberSaveable { mutableStateOf(false) }
    Tray(
        icon = painterResource(Res.drawable.ic_launcher),
        menu = {
            KtorMonitorMenuItem { showKtorMonitor = true }
        }
    )

    KtorMonitorWindow(
        show = showKtorMonitor,
        onCloseRequest = { showKtorMonitor = false }
    )

}
```
</details>

<details>
<summary><b>Desktop Swing</b></summary>

* Use ```KtorMonitorPanel``` Swing Panel

```kotlin
fun main() = application {

    SwingUtilities.invokeLater {
        val frame = JFrame()
        frame.add(KtorMonitorPanel, BorderLayout.CENTER)
        frame.isVisible = true
    }

}
```
</details>

<details>
<summary><b>iOS</b></summary>

* If ```showNotifcation = true``` and notification permission is granted, the library will display a notification showing a summary of ongoing KTOR activity.

* Use ```KtorMonitorViewController```

```kotlin
fun MainViewController() = KtorMonitorViewController()
```

```swift
struct KtorMonitorView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        KtorMonitorView()
                .ignoresSafeArea()
    }
}
```
</details>

<details>
<summary><b>Wasm</b></summary>

```kotlin
kotlin {
    sourceSets {
        wasmJsMain.dependencies {
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
        }
    }
}
```

```javascript
// {project}/webpack.config.d/sqljs.js
config.resolve = {
    fallback: {
        fs: false,
        path: false,
        crypto: false,
    }
};

const CopyWebpackPlugin = require('copy-webpack-plugin');
config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            '../../node_modules/sql.js/dist/sql-wasm.wasm'
        ]
    })
);
```

```kotlin
ComposeViewport(document.body!!) {
    App()
}
```
</details>

## ✍️ Feedback

Feel free to send feedback or [file an issue](https://github.com/CosminMihuMDC/KtorMonitor/issues/new).

## 🙌 Acknowledgments

[![Kotlin](https://img.shields.io/badge/2.1.20-white?logo=kotlin&logoColor=white&color=7F52FF)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/1.8.0-white?logo=jetpackcompose&logoColor=white&color=4284F3)](https://www.jetbrains.com/lp/compose-multiplatform)
[![Android](https://img.shields.io/badge/Android%2016-white?logo=android&logoColor=white&color=34A853)](https://developer.android.com/about/versions/15)
[![Ktor](https://img.shields.io/badge/3.1.3-white?logo=ktor&logoColor=white&color=087CFA)](https://ktor.io)
[![SQLDelight](https://img.shields.io/badge/2.1.0-white?logo=sqlite&logoColor=white&color=003B57)](https://sqldelight.github.io/sqldelight)

Documentation is available at [KtorMonitor Documentation](https://cosminmihumdc.github.io/KtorMonitor/html).
<br>
KtorMonitor is available also on JetBrains' [klibs.io](https://klibs.io/project/CosminMihuMDC/KtorMonitor).
<br>
Some parts of this project are reusing ideas that are originally coming from [Chucker](https://github.com/ChuckerTeam/chucker).

Thanks to ChuckerTeam for Chucker!
<br>
Thanks to JetBrains for Ktor and Kotlin!

## 💸 Sponsors
KtorMonitor is maintained and improved during nights, weekends and whenever team has free time. If you use KtorMonitor in your project, please consider sponsoring us.

You can sponsor us by clicking <span style="color:#bf3989">♥ Sponsor</span> Button.

## 🙏🏻 Credits

KtorMonitor is brought to you by these [contributors](https://github.com/CosminMihuMDC/KtorMonitor/graphs/contributors).