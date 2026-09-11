package ro.cosminmihu.ktor.monitor.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call
import ro.cosminmihu.ktor.monitor.domain.model.ContentType
import ro.cosminmihu.ktor.monitor.domain.model.contentType
import ro.cosminmihu.ktor.monitor.domain.model.decodeBody

internal class ExportCallAsMarkdownUseCase {

    suspend operator fun invoke(call: Call): String = withContext(Dispatchers.Default) {
        val requestBody = call.requestBody?.decodeBody(call.requestHeaders)
        val responseBody = call.responseBody?.decodeBody(call.responseHeaders)

        buildString {
            appendLine("## ${call.method} ${call.url}")
            appendLine()

            appendLine("### Request")
            appendLine()
            call.requestHeaders.appendHeaderBlock(this)
            requestBody
                ?.takeIf { it.isNotBlank() }
                ?.let { appendBodyBlock(it, call.requestContentType, call.isRequestBodyTruncated == true) }

            appendLine("### Response")
            appendLine()
            if (call.responseCode == null) {
                appendLine("_No response received._")
                appendLine()
                call.error
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        appendLine("**Error:** $it")
                        appendLine()
                    }
            } else {
                appendLine("Status: `${call.responseCode}`")
                appendLine()
                call.responseHeaders.orEmpty().appendHeaderBlock(this)
                responseBody
                    ?.takeIf { it.isNotBlank() }
                    ?.let { appendBodyBlock(it, call.responseContentType, call.isResponseBodyTruncated == true) }
            }
        }
    }
}

private const val BODY_PREVIEW_LIMIT = 2_000

private fun Map<String, List<String>>.appendHeaderBlock(out: StringBuilder) {
    if (isEmpty()) return
    out.appendLine("```")
    entries.forEach { (key, values) ->
        out.appendLine("$key: ${values.joinToString(separator = "; ")}")
    }
    out.appendLine("```")
    out.appendLine()
}

private fun StringBuilder.appendBodyBlock(
    body: String,
    contentType: String?,
    isTruncatedAtCapture: Boolean,
) {
    val preview = body.toMarkdownBodyPreview(isTruncatedAtCapture)
    val language = contentType?.contentType?.fenceLanguage.orEmpty()
    appendLine("```$language")
    appendLine(preview)
    appendLine("```")
    appendLine()
}

private fun String.toMarkdownBodyPreview(isTruncatedAtCapture: Boolean): String {
    val clipped = take(BODY_PREVIEW_LIMIT)
    return when {
        isTruncatedAtCapture -> "$clipped (truncated)"
        length > BODY_PREVIEW_LIMIT -> "$clipped...(clipped for export)"
        else -> clipped
    }
}

/**
 * Maps a call's decoded [ContentType] to the Markdown fence language used to highlight its
 * body in the exported document. Only content types with a well-understood text structure are
 * mapped; everything else (binary, images, unknown types) falls back to an unlabeled fence.
 */
private val ContentType.fenceLanguage: String?
    get() = when (this) {
        ContentType.APPLICATION_JSON,
        ContentType.APPLICATION_HAL_JSON,
        ContentType.APPLICATION_PROBLEM_JSON,
        ContentType.APPLICATION_VND_API_JSON,
            -> "json"

        ContentType.TEXT_HTML,
            -> "html"

        ContentType.TEXT_XML,
        ContentType.APPLICATION_XML,
        ContentType.APPLICATION_XML_DTD,
        ContentType.APPLICATION_XAML,
        ContentType.APPLICATION_ATOM,
        ContentType.APPLICATION_RSS,
        ContentType.APPLICATION_SOAP,
        ContentType.APPLICATION_PROBLEM_XML,
            -> "xml"

        ContentType.APPLICATION_YAML,
        ContentType.APPLICATION_X_YAML,
        ContentType.TEXT_YAML,
        ContentType.TEXT_X_YAML,
            -> "yaml"

        ContentType.TEXT_CSS,
            -> "css"

        else -> null
    }
