package ro.cosminmihu.ktor.monitor.core

internal expect class ClipboardManager() {

    internal suspend fun setText(text: String)
}