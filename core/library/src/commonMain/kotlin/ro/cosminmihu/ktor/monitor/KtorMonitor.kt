package ro.cosminmihu.ktor.monitor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ro.cosminmihu.ktor.monitor.ui.main.MainUI

/**
 * Ktor Monitor UI entry point.
 */
@Composable
public fun KtorMonitor(
    modifier: Modifier = Modifier,
    useKtorMonitorTheme: Boolean = true,
) {
    MainUI(
        modifier = modifier,
        useLibraryTheme = useKtorMonitorTheme,
    )
}