package ro.cosminmihu.ktor.monitor.core

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

internal actual class ShareManager {

    internal actual suspend fun shareAsFile(
        content: String,
        name: String,
        title: String?,
    ) {
        val dialog = FileDialog(
            null as Frame?,
            title,
            FileDialog.SAVE
        )

        dialog.file = name
        dialog.isVisible = true

        val directory = dialog.directory ?: return
        val file = dialog.file ?: return

        val output = File(directory, file)
        output.writeText(content)
    }
}