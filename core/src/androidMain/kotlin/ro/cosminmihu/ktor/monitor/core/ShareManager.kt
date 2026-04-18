package ro.cosminmihu.ktor.monitor.core

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import ro.cosminmihu.ktor.monitor.di.LibraryKoinComponent
import java.io.File

internal actual class ShareManager : LibraryKoinComponent {

    private val context: Context by inject()

    internal actual suspend fun shareAsFile(
        content: String,
        name: String,
        title: String?,
    ) {
        val file = withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, SHARE_DIRECTORY).apply { mkdirs() }
            File(directory, name).apply { writeText(content) }
        }

        val authority = "${context.packageName}.$FILE_PROVIDER_SUFFIX"
        val shareUri = FileProvider.getUriForFile(context, authority, file)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE_TEXT
            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (title != null) {
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TITLE, title)
            }
        }

        val chooser = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        withContext(Dispatchers.Main) {
            context.startActivity(chooser)
        }
    }

    private companion object {
        private const val SHARE_DIRECTORY = "ktormonitor"
        private const val FILE_PROVIDER_SUFFIX = "ktorMonitor.share"
        private const val MIME_TYPE_TEXT = "text/plain"
    }
}