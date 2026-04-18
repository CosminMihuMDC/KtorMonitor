package ro.cosminmihu.ktor.monitor.core

internal expect class ShareManager() {

    internal suspend fun shareAsFile(content: String, name: String, title: String? = null)
}