package ro.cosminmihu.ktor.monitor.ui.export

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call

internal object TransactionExporter {

    internal suspend fun exportUrl(call: Call) = withContext(Dispatchers.Default) {
        buildString {
            append(call.url)
        }
    }

    internal suspend fun exportAsText(call: Call) = withContext(Dispatchers.Default) {
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

    internal suspend fun exportRequestAsCurl(call: Call) = withContext(Dispatchers.Default) {
        buildString {
            val command = buildList {
                add("curl")
                add("-X \"${call.method}\"")
                call.requestHeaders.forEach { (key, values) ->
                    val value = values.joinToString(separator = "; ")
                    add("-H \"${shellEscape("$key: $value")}\"")
                }
                call.requestBody?.takeIf { it.isNotEmpty() }?.decodeToString()?.let {
                    add("--data-binary \"${shellEscape(it)}\"")
                }
                add("\"${shellEscape(call.url)}\"")
            }

            append(command.joinToString(separator = " \\\n  "))
        }
    }

    internal suspend fun exportRequestAsWget(call: Call) = withContext(Dispatchers.Default) {
        buildString {
            val command = buildList {
                add("wget")
                add("--method=\"${call.method}\"")
                call.requestHeaders.forEach { (key, values) ->
                    val value = values.joinToString(separator = "; ")
                    add("--header=\"${shellEscape("$key: $value")}\"")
                }
                call.requestBody?.takeIf { it.isNotEmpty() }?.decodeToString()?.let {
                    add("--body-data=\"${shellEscape(it)}\"")
                }
                add("\"${shellEscape(call.url)}\"")
            }

            append(command.joinToString(separator = " \\\n  "))
        }
    }
}

private fun shellEscape(value: String): String = value
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("$", "\\$")

private fun Boolean?.truncatedLabel() = if (this == true) " (truncated)" else ""
