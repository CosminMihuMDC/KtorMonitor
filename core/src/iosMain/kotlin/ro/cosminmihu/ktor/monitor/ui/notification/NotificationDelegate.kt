package ro.cosminmihu.ktor.monitor.ui.notification

import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionAlert
import platform.UserNotifications.UNNotificationPresentationOptionBadge
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

internal class NotificationDelegate(
    private val appNotificationDelegate: UNUserNotificationCenterDelegateProtocol? = null
) : NSObject(), UNUserNotificationCenterDelegateProtocol {

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        val options = UNNotificationPresentationOptionAlert or
                UNNotificationPresentationOptionSound or
                UNNotificationPresentationOptionBadge

        appNotificationDelegate
            ?.userNotificationCenter(center, willPresentNotification, withCompletionHandler)
            ?: withCompletionHandler(options)
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        appNotificationDelegate
            ?.userNotificationCenter(center, didReceiveNotificationResponse, withCompletionHandler)
            ?: withCompletionHandler()
    }
}
