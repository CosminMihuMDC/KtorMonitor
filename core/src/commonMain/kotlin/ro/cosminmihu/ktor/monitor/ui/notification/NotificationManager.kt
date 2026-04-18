package ro.cosminmihu.ktor.monitor.ui.notification

internal expect class NotificationManager() {

    suspend fun notify(messages: List<String>)

    suspend fun clear()
}