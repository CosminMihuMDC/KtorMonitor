package ro.cosminmihu.ktor.monitor.core

import android.content.ClipData
import android.content.Context
import androidx.core.content.getSystemService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal actual class ClipboardManager : KoinComponent {

    private val context: Context by inject()

    internal actual suspend fun setText(text: String) {
        context.getSystemService<android.content.ClipboardManager>()
            ?.setPrimaryClip(ClipData.newPlainText("copy", text))
    }
}