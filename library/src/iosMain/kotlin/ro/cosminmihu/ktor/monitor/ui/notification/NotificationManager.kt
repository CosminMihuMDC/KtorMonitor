package ro.cosminmihu.ktor.monitor.ui.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationInterruptionLevel
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

internal actual class NotificationManager {

    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter().apply {
        setDelegate(NotificationDelegate())
    }

    actual suspend fun clear() {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(NOTIFICATION_ID.toString()))
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(listOf(NOTIFICATION_ID.toString()))
    }

    actual suspend fun notify(messages: List<String>) {
        if (messages.isEmpty()) {
            clear()
            return
        }

        if (!notificationCenter.isNotificationPermissionGranted()) {
            return
        }

        val body = messages.joinToString("\n") { it }
        val content = UNMutableNotificationContent().apply {
            setTitle(NOTIFICATION_TITLE)
            setBody(body)
            setSound(null)
            setInterruptionLevel(UNNotificationInterruptionLevel.UNNotificationInterruptionLevelActive)
        }

        val trigger = UNTimeIntervalNotificationTrigger
            .triggerWithTimeInterval(0.1, repeats = false)
        val request = UNNotificationRequest
            .requestWithIdentifier(NOTIFICATION_ID.toString(), content, trigger)

        notificationCenter.addNotificationRequest(request) { error ->
            error?.let {
                println("Error showing notification: $it")
            }
        }
    }
}

internal suspend fun UNUserNotificationCenter.isNotificationPermissionGranted(): Boolean =
    suspendCancellableCoroutine { continuation ->
        requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            continuation.resume(granted)
        }
    }