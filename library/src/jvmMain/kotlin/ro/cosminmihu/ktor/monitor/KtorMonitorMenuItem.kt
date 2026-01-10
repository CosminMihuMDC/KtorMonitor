package ro.cosminmihu.ktor.monitor

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.MenuScope
import androidx.compose.ui.window.Tray
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_tray_name

/**
 * A [Tray] menu item for Ktor Monitor.
 *
 * ```kotlin
 * var showWindow by rememberSaveable { mutableStateOf(false) }
 *
 * Tray(
 *      icon = painterResource(Res.drawable.ic_launcher),
 *      menu = {
 *         KtorMonitorMenuItem { showWindow = true }
 *      }
 * )
 *
 * KtorMonitorWindow(
 *      show = showWindow,
 *      onCloseRequest = { showWindow = false },
 * )
 * ```
 */
@Composable
public fun MenuScope.KtorMonitorMenuItem(
    onClick: () -> Unit,
) {
    Item(
        text = stringResource(Res.string.ktor_tray_name),
        onClick = onClick,
    )
}