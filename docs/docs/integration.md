Add the UI component to your application based on your targeted platform.

### Compose Multiplatform (Common)

* Use ```KtorMonitor``` Composable

```kotlin hl_lines="3"
@Composable
fun Composable() {
    KtorMonitor()
}
```

### Android

- If ```showNotifcation = true``` and **android.permission.POST_NOTIFICATIONS** is granted, the library will display a notification showing a summary of ongoing KTOR activity. Tapping on the notification launches the full ```KtorMonitor```.
- Apps can optionally use the ```KtorMonitor()``` Composable directly into own Composable code.
- For ***Android minSdk < 26***, [Core Library Desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) is required.

### iOS

* If ```showNotifcation = true``` and notification permission is granted, the library will display a notification showing a summary of ongoing KTOR activity.

* Use ```KtorMonitorViewController```

```kotlin hl_lines="1"
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

### Desktop (Compose)

* Use ```KtorMonitorWindow``` Composable

```kotlin hl_lines="4"
fun main() = application {

    var showKtorMonitor by rememberSaveable { mutableStateOf(false) }
    KtorMonitorWindow(
        onCloseRequest = { showKtorMonitor = false },
        show = showKtorMonitor
    )
}
```

* Use ```KtorMonitorWindow``` Composable with ```KtorMonitorMenuItem```

```kotlin hl_lines="7"
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

### Desktop (Swing)

* Use ```KtorMonitorPanel``` Swing Panel

```kotlin hl_lines="5"
fun main() = application {

    SwingUtilities.invokeLater {
        val frame = JFrame()
        frame.add(KtorMonitorPanel, BorderLayout.CENTER)
        frame.isVisible = true
    }

}
```

### Wasm / Js

* Web targets store the data with SQLite WASM and need a small Web Worker (`WebWorkerSQLiteDriver` does not ship one).
  Copy [`sqlite.worker.js`](https://github.com/CosminMihuMDC/KtorMonitor/blob/main/sample/ktor/webApp/webpack-resources/sqlite.worker.js)
  into your web project (e.g. `{project}/webpack-resources/sqlite.worker.js`) and alias it in webpack.
  The data is kept in memory, so no special HTTP headers are required.

```javascript
// {project}/webpack.config.d/sqlite.js
const path = require('path');

config.resolve = {
    modules: ['node_modules', path.resolve(__dirname, '../../node_modules')],
    fallback: {
        fs: false,
        path: false,
        crypto: false,
        os: false,
    },
    alias: {
        ...(config.resolve && config.resolve.alias),
        'ktor-monitor-sqlite-worker/sqlite.worker.js': path.resolve(__dirname, '../../../../webpack-resources/sqlite.worker.js'),
    },
};
```

```kotlin
ComposeViewport {
    App()
}
```