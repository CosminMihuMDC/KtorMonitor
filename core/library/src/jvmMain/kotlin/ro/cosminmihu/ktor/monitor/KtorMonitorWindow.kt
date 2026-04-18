package ro.cosminmihu.ktor.monitor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.main.MainRoute
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_library_name

/**
 * Ktor Monitor [Window].
 *
 * ```kotlin
 * var showWindow by rememberSaveable { mutableStateOf(false) }
 *
 * KtorMonitorWindow(
 *      show = showWindow,
 *      useLibraryTheme = true,
 *      onCloseRequest = { showWindow = false },
 * )
 * ```
 */
@Composable
public fun KtorMonitorWindow(
    show: Boolean = true,
    useLibraryTheme: Boolean = true,
    onCloseRequest: () -> Unit = {},
) {
    if (!show) return

    Window(
        onCloseRequest = onCloseRequest,
        title = stringResource(Res.string.ktor_library_name),
    ) {
        MainRoute(
            modifier = Modifier.fillMaxSize(),
            useLibraryTheme = useLibraryTheme
        )
    }
}