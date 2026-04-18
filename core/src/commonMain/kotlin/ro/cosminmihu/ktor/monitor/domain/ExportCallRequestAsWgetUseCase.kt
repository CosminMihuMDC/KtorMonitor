package ro.cosminmihu.ktor.monitor.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call
import ro.cosminmihu.ktor.monitor.domain.model.shellEscape

internal class ExportCallRequestAsWgetUseCase {

    suspend operator fun invoke(call: Call) = withContext(Dispatchers.Default) {
        buildString {
            val command = buildList {
                add("wget")
                add("--method=\"${call.method}\"")
                call.requestHeaders.forEach { (key, values) ->
                    val value = values.joinToString(separator = "; ")
                    add("--header=\"${("$key: $value").shellEscape()}\"")
                }
                call.requestBody?.takeIf { it.isNotEmpty() }?.decodeToString()?.let {
                    add("--body-data=\"${it.shellEscape()}\"")
                }
                add("\"${call.url.shellEscape()}\"")
            }

            append(command.joinToString(separator = " \\\n  "))
        }
    }
}