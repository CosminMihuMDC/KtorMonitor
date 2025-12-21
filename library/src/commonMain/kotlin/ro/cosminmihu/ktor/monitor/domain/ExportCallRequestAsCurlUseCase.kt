package ro.cosminmihu.ktor.monitor.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call
import ro.cosminmihu.ktor.monitor.domain.model.shellEscape

internal class ExportCallRequestAsCurlUseCase {

    suspend operator fun invoke(call: Call) = withContext(Dispatchers.Default) {
        buildString {
            val command = buildList {
                add("curl")
                add("-X \"${call.method}\"")
                call.requestHeaders.forEach { (key, values) ->
                    val value = values.joinToString(separator = "; ")
                    add("-H \"${("$key: $value").shellEscape()}\"")
                }
                call.requestBody?.takeIf { it.isNotEmpty() }?.decodeToString()?.let {
                    add("--data-binary \"${it.shellEscape()}\"")
                }
                add("\"${call.url.shellEscape()}\"")
            }

            append(command.joinToString(separator = " \\\n  "))
        }
    }
}