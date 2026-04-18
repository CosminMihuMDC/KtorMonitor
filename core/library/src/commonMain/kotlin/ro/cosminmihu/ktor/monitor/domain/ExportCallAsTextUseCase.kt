package ro.cosminmihu.ktor.monitor.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call

internal class ExportCallAsTextUseCase {

    suspend operator fun invoke(call: Call) = withContext(Dispatchers.Default) {
        buildString {
            val requestStartLine = buildString {
                append(call.method)
                append(' ')
                append(call.url)
                append(' ')
                append(call.protocol ?: "HTTP/1.1")
            }
            val requestHeaders = call.requestHeaders
                .entries
                .joinToString(separator = "\n") { (key, values) ->
                    "$key: ${values.joinToString(separator = "; ")}"
                }
            val requestBody = call.requestBody
                ?.decodeToString()
                ?.takeIf { it.isNotEmpty() }
                ?.let { it + call.isRequestBodyTruncated.truncatedLabel() }

            val responseProtocol = call.protocol ?: "HTTP/1.1"
            val responseStartLine = buildString {
                append(responseProtocol)
                append(' ')
                append(call.responseCode?.toString() ?: "-")
            }
            val responseHeaders = (call.responseHeaders ?: emptyMap())
                .entries
                .joinToString(separator = "\n") { (key, values) ->
                    "$key: ${
                        values.joinToString(
                            separator = "; "
                        )
                    }"
                }
            val responseBody = call.responseBody
                ?.decodeToString()
                ?.takeIf { it.isNotEmpty() }
                ?.let { it + call.isResponseBodyTruncated.truncatedLabel() }

            appendLine(requestStartLine)
            if (requestHeaders.isNotBlank()) {
                appendLine(requestHeaders)
            }
            appendLine()
            if (!requestBody.isNullOrBlank()) {
                appendLine(requestBody)
            }
            appendLine()
            appendLine(responseStartLine)
            if (responseHeaders.isNotBlank()) {
                appendLine(responseHeaders)
            }
            appendLine()
            if (!responseBody.isNullOrBlank()) {
                appendLine(responseBody)
            }
            appendLine()
        }
    }
}

private fun Boolean?.truncatedLabel() = if (this == true) " (truncated)" else ""