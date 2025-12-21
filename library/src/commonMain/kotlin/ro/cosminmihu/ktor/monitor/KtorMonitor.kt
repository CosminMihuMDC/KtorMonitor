package ro.cosminmihu.ktor.monitor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ro.cosminmihu.ktor.monitor.ui.main.MainRoute

/**
 * Ktor Monitor UI entry point.
 */
@Composable
public fun KtorMonitor(
    modifier: Modifier = Modifier,
    useKtorMonitorTheme: Boolean = true,
) {
    MainRoute(
        modifier = modifier,
        useLibraryTheme = useKtorMonitorTheme,
    )
}