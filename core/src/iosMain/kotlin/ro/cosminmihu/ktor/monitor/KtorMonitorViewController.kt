package ro.cosminmihu.ktor.monitor

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * [UIViewController] for [KtorMonitor].
 *
 * ```kotlin
 * fun MainViewController() = KtorMonitorViewController()
 * ```
 *
 *```swift
 * struct KtorMonitorView: UIViewControllerRepresentable {
 *     func makeUIViewController(context: Context) -> UIViewController {
 *         MainViewControllerKt.MainViewController()
 *     }
 *
 *     func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
 * }
 *
 * struct ContentView: View {
 *     var body: some View {
 *         KtorMonitorView()
 *                 .ignoresSafeArea()
 *     }
 * }
 *```
 */
public fun KtorMonitorViewController(): UIViewController = ComposeUIViewController { KtorMonitor() }