package ro.cosminmihu.ktor.monitor.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call

internal class ExportCallUrlUseCase {

    suspend operator fun invoke(call: Call) = withContext(Dispatchers.Default) {
        buildString {
            append(call.url)
        }
    }
}