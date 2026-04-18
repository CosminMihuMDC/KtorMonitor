package ro.cosminmihu.ktor.monitor.db

import app.cash.sqldelight.db.SqlDriver
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call
import ro.cosminmihu.ktor.monitor.db.sqldelight.LibraryDatabase

internal const val DATABASE_NAME = "ktor_monitor.db"

internal expect fun createDatabaseDriver(): SqlDriver

internal fun createDatabase(driver: SqlDriver): LibraryDatabase {
    return LibraryDatabase(
        driver = driver,
        callAdapter = Call.Adapter(
            requestHeadersAdapter = HeadersAdapter,
            responseHeadersAdapter = HeadersAdapter,
        )
    )
}