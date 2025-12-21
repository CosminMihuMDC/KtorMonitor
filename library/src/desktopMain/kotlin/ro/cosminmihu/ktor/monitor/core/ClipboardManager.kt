package ro.cosminmihu.ktor.monitor.core

internal actual class ClipboardManager {

    internal actual suspend fun setText(text: String) {
        val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
        val stringSelection = java.awt.datatransfer.StringSelection(text)
        clipboard.setContents(stringSelection, null)
    }
}