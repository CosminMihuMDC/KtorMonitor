package ro.cosminmihu.ktor.monitor.core

import android.content.ClipData
import android.content.Context
import androidx.core.content.getSystemService
import org.koin.core.component.inject
import ro.cosminmihu.ktor.monitor.di.LibraryKoinComponent

internal actual class ClipboardManager : LibraryKoinComponent {

    private val context: Context by inject()

    internal actual suspend fun setText(text: String) {
        context.getSystemService<android.content.ClipboardManager>()
            ?.setPrimaryClip(ClipData.newPlainText("copy", text))
    }
}