package ro.cosminmihu.ktor.monitor.db

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Worker
import ro.cosminmihu.ktor.monitor.db.sqldelight.LibraryDatabase

internal actual fun createDatabaseDriver(): SqlDriver {
    return WebWorkerDriver(jsWorker()).also {
        GlobalScope.launch(Dispatchers.Default) { // TODO remove coroutines.
            LibraryDatabase.Schema.awaitCreate(it)
        }
    }
}

private fun jsWorker(): Worker =
    js("""new Worker(new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")
