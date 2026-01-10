package ro.cosminmihu.ktor.monitor.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ro.cosminmihu.ktor.monitor.db.sqldelight.LibraryDatabase
import java.io.File

internal actual fun createDatabaseDriver(): SqlDriver {
    val filePath = File(System.getProperty("java.io.tmpdir"), DATABASE_NAME)
    return JdbcSqliteDriver("jdbc:sqlite:$filePath").also {
        LibraryDatabase.Schema.synchronous().create(it)
    }
}