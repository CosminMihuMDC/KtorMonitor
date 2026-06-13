package ro.cosminmihu.ktor.monitor.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call
import ro.cosminmihu.ktor.monitor.domain.model.decodeBody

internal class ExportCallAsTextUseCase {

    suspend operator fun invoke(call: Call): String = withContext(Dispatchers.Default) {
        val protocol = call.protocol ?: "HTTP/1.1"
        val requestBody = call.requestBody?.decodeBody(call.requestHeaders)
        val responseBody = call.responseBody?.decodeBody(call.responseHeaders)

        buildString {
            // Request line + headers
            appendLine("${call.method} ${call.url} $protocol")
            call.requestHeaders.appendHeaderBlock(this)
            appendLine()

            // Request body
            requestBody
                ?.takeIf { it.isNotBlank() }
                ?.let { appendLine(it + call.isRequestBodyTruncated.truncatedLabel()) }
            appendLine()

            // Response line + headers
            appendLine("$protocol ${call.responseCode?.toString() ?: "-"}")
            call.responseHeaders.orEmpty().appendHeaderBlock(this)
            appendLine()

            // Response body
            responseBody
                ?.takeIf { it.isNotBlank() }
                ?.let { appendLine(it + call.isResponseBodyTruncated.truncatedLabel()) }
            appendLine()
        }
    }
}

private fun Map<String, List<String>>.appendHeaderBlock(out: StringBuilder) {
    if (isEmpty()) return
    entries.joinTo(out, separator = "\n", postfix = "\n") { (key, values) ->
        "$key: ${values.joinToString(separator = "; ")}"
    }
}

private fun Boolean?.truncatedLabel() = if (this == true) " (truncated)" else ""