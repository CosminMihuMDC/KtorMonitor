package ro.cosminmihu.ktor.monitor.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ro.cosminmihu.ktor.monitor.KtorMonitor

/**
 * Compose sample how to use [KtorMonitor].
 */
@Composable
@Preview
fun App() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KtorMonitor()
    }

    LaunchedEffect(Unit) {
        samples()
    }
}

