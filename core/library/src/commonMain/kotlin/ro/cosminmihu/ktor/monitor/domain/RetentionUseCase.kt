package ro.cosminmihu.ktor.monitor.domain

import ro.cosminmihu.ktor.monitor.RetentionPeriod
import ro.cosminmihu.ktor.monitor.db.LibraryDao
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class RetentionUseCase(
    private val configUseCase: ConfigUseCase,
    private val dao: LibraryDao,
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke() {
        val retentionPeriod = configUseCase.getRetentionPeriod()
        if (retentionPeriod == RetentionPeriod.Forever) return

        val threshold = Clock.System.now().minus(retentionPeriod).toEpochMilliseconds()
        dao.deleteCallsBefore(threshold)
    }
}