package ro.cosminmihu.ktor.monitor.domain

import app.cash.sqldelight.Query
import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.flow.map
import ro.cosminmihu.ktor.monitor.db.LibraryDao
import ro.cosminmihu.ktor.monitor.db.sqldelight.SelectCalls

internal class GetCallsUseCase(
    private val dao: LibraryDao,
) {

    operator fun invoke() = dao.getCalls()
        .map(Query<SelectCalls>::awaitAsList)
}