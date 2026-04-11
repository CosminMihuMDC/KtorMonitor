package ro.cosminmihu.ktor.monitor.sample.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.KtorMonitorMenuItem
import ro.cosminmihu.ktor.monitor.KtorMonitorWindow
import ro.cosminmihu.ktor.monitor.sample.App
import ro.cosminmihu.ktor.monitor.sample.resources.Res
import ro.cosminmihu.ktor.monitor.sample.resources.app_name
import ro.cosminmihu.ktor.monitor.sample.resources.ic_launcher

/**
 * Compose sample how to use [KtorMonitorWindow] w/o [KtorMonitorMenuItem].
 */
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

    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
    ) {
        App()
    }
}