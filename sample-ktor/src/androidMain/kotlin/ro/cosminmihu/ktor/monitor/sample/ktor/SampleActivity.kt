package ro.cosminmihu.ktor.monitor.sample.ktor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * Compose sample how to use [ro.cosminmihu.ktor.monitor.KtorMonitor].
 */
class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App()
        }
    }
}