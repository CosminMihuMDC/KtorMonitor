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

* Web targets require a few additional webpack steps.

```kotlin hl_lines="4"
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
ComposeViewport {
    App()
}
```