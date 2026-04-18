package ro.cosminmihu.ktor.monitor.domain

import app.cash.sqldelight.Query
import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ro.cosminmihu.ktor.monitor.db.LibraryDao
import ro.cosminmihu.ktor.monitor.db.sqldelight.SelectCallsWithLimit
import ro.cosminmihu.ktor.monitor.domain.model.encodedPathAndQuery
import ro.cosminmihu.ktor.monitor.domain.model.isError
import ro.cosminmihu.ktor.monitor.domain.model.isInProgress
import ro.cosminmihu.ktor.monitor.ui.notification.NotificationManager

private const val LAST_CALLS = 5L

internal class ListenByRecentCallsUseCase(
    private val dao: LibraryDao,
    private val coroutineScope: CoroutineScope,
    private val notificationManager: NotificationManager,
    private val setupUseCase: ConfigUseCase,
    private val retentionUseCase: RetentionUseCase,
) {

    operator fun invoke() {
        coroutineScope.launch {
            dao.getCalls(LAST_CALLS)
                .map(Query<SelectCallsWithLimit>::awaitAsList)
                .map {
                    it.map {
                        buildString {
                            append(
                                when {
                                    it.isInProgress -> "⏳"
                                    it.isError -> "❌"
                                    else -> it.responseCode
                                }
                            )
                            append(" ")
                            append(it.method)
                            append(" ")
                            append(it.encodedPathAndQuery)
                        }
                    }
                }
                .distinctUntilChanged()
                .collectLatest {
                    // Show notification
                    if (setupUseCase.isShowNotification()) {
                        notificationManager.notify(it)
                    }

                    // Delete old calls.
                    retentionUseCase()
                }
        }
    }
}