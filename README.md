# <img src="./extra/ktor_ic_launcher.svg" width="35"/> KtorMonitor
Powerful tools to log [Ktor Client](https://ktor.io/) requests and responses, making it easier to debug and analyze network communication.

By default, **```KtorMonitor```** is:
- **android** -> enabled for ```debug``` builds and disabled for ```release``` builds
- **ios** -> enabled for ```debug``` builds and disabled for ```release``` builds
- **desktop** -> enabled for all builds

## Setup

### <img src="https://upload.wikimedia.org/wikipedia/commons/6/6b/Gradle_logo.svg" width="100"/>

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("ro.cosminmihu.ktor:ktor-monitor-logging:1.0.1")
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
- ```showNotification``` - Keep track of latest requests and responses into notification. Default is **true**. Android only. **android.permission.POST_NOTIFICATIONS** needs to be granted.
- ```retentionPeriod``` - The retention period for the logs. Deault is **1h**.
- ```maxContentLength``` - The maximum length of the content that will be logged. After this, body will be truncated. Deafult is **250_000**. To log the entire body use ```ContentLength.Full```.

## 🧩 Integration

Check below how to interate library UI component in your app based on platform.

### Compose Multiplatform (all platforms)

* Use ```KtorMonitor``` Composable

```kotlin
@Composable
fun Composable() {
    KtorMonitor()
}
```

### Android

- If ```showNotifcation = true``` and **android.permission.POST_NOTIFICATIONS** is granted, the library will display a notification showing a summary of ongoing KTOR activity. Tapping on the notification launches the full ```KtorMonitor```.
- Apps can optionally use the ```KtorMonitor()``` Composable directly into own Composable code.

### Desktop Compose

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

### Desktop Swing

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

## ✍️ Feedback

Feel free to send feedback on [file an issue](https://github.com/CosminMihuMDC/KtorMonitor/issues).

## 🙌 Acknowledgments

Some parts of this project are reusing ideas that are originally coming
from [chucker](https://github.com/ChuckerTeam/chucker).

## 🙏🏻 Credits

KtorMonitor is brought to you by these [contributors](https://github.com/CosminMihuMDC/KtorMonitor/graphs/contributors).
