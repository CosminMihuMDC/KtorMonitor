package ro.cosminmihu.ktor.monitor.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ro.cosminmihu.ktor.monitor.db.sqldelight.LibraryDatabase
import ro.cosminmihu.ktor.monitor.di.LibraryKoinContext

internal actual fun createDatabaseDriver(): SqlDriver {
    return AndroidSqliteDriver(
        schema = LibraryDatabase.Schema.synchronous(),
        context = LibraryKoinContext.koin.get(),
        name = DATABASE_NAME
    )
}
