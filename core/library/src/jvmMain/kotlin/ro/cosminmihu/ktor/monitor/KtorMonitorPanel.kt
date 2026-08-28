package ro.cosminmihu.ktor.monitor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import java.awt.Dimension
import javax.swing.JLayeredPane

/**
 * A Swing [javax.swing.JPanel] that displays [KtorMonitor].
 *
 * ```kotlin
 * val frame = JFrame()
 * frame.add(KtorMonitorPanel)
 * frame.isVisible = true
 * ```
 */
public val KtorMonitorPanel: JLayeredPane = ComposePanel().apply {
    preferredSize = Dimension(200, 200)
    setContent {
        KtorMonitor(modifier = Modifier.fillMaxSize())
    }
}