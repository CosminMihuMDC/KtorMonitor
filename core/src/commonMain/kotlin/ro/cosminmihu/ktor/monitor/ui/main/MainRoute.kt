package ro.cosminmihu.ktor.monitor.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.KoinIsolatedContext
import ro.cosminmihu.ktor.monitor.di.LibraryKoinContext
import ro.cosminmihu.ktor.monitor.ui.theme.LibraryTheme

@Composable
internal fun MainRoute(
    modifier: Modifier = Modifier,
    useLibraryTheme: Boolean = true,
) {
    KoinIsolatedContext(
        context = LibraryKoinContext.koinApp
    ) {
        when {
            useLibraryTheme ->
                LibraryTheme {
                    MainScreen(modifier = modifier.fillMaxSize())
                }

            else -> MainScreen(modifier = modifier.fillMaxSize())
        }
    }
}
