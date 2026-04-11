package ro.cosminmihu.ktor.monitor.domain

import ro.cosminmihu.ktor.monitor.db.LibraryDao
import ro.cosminmihu.ktor.monitor.ui.notification.NotificationManager

internal class DeleteCallsUseCase(
    private val dao: LibraryDao,
    private val notificationManager: NotificationManager,
) {

    suspend operator fun invoke() {
        dao.deleteCalls()
        notificationManager.clear()
    }
}