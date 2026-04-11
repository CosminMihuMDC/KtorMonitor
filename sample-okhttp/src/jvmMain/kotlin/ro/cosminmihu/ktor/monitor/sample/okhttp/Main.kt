package ro.cosminmihu.ktor.monitor.sample.okhttp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ro.cosminmihu.ktor.monitor.KtorMonitorWindow

fun main() = application {

    KtorMonitorWindow()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Ktor Monitor OkHttp Sample",
    ) {
        App()
    }
}

