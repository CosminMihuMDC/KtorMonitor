package ro.cosminmihu.ktor.monitor.ui.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.koin.core.component.inject
import ro.cosminmihu.ktor.monitor.R
import ro.cosminmihu.ktor.monitor.di.LibraryKoinComponent
import ro.cosminmihu.ktor.monitor.ui.KtorMonitorActivity

private const val NOTIFICATION_CHANNEL_ID = "KtorMonitorNotificationChannel"
private const val NOTIFICATION_CHANNEL_NAME = "Ktor Monitor Notification Channel"
private const val NOTIFICATION_CHANNEL_DESCRIPTION = "Ktor Monitor Notification Channel"

internal actual class NotificationManager : LibraryKoinComponent {

    private val context: Context by inject()

    actual suspend fun clear() {
        val notificationManager = ContextCompat
            .getSystemService(context, NotificationManager::class.java)
            ?: return
        notificationManager.cancel(NOTIFICATION_ID)
    }

    actual suspend fun notify(messages: List<String>) {
        if (messages.isEmpty()) {
            clear()
            return
        }

        if (!isNotificationPermissionGranted(context)) {
            return
        }

        val notificationManager = ContextCompat
            .getSystemService(context, NotificationManager::class.java)
            ?: return

        createNotificationChannel(notificationManager)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setLocalOnly(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ktor_ic_notification)
            .setContentIntent(createPendingIntent())
            .addAction(createClearAction())
            .setStyle(NotificationCompat.InboxStyle().also { style ->
                messages.forEach { style.addLine(it) }
            })
            .setSound(null)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = NOTIFICATION_CHANNEL_DESCRIPTION
        notificationManager.createNotificationChannel(channel)
    }

    private fun createPendingIntent(): PendingIntent? {
        val intent = Intent(context, KtorMonitorActivity::class.java)

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createClearAction(): NotificationCompat.Action {
        val clearTitle = context.getString(R.string.ktor_clear)
        val clearTransactionsBroadcastIntent = Intent(context, KtorMonitorClearBroadcastReceiver::class.java)
        val pendingBroadcastIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                clearTransactionsBroadcastIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
            )
        return NotificationCompat.Action(
            R.drawable.ktor_ic_clear_all,
            clearTitle,
            pendingBroadcastIntent,
        )
    }

    private fun isNotificationPermissionGranted(context: Context): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        else -> {
            true
        }
    }
}