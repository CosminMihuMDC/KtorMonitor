package ro.cosminmihu.ktor.monitor.domain.model

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call
import ro.cosminmihu.ktor.monitor.db.sqldelight.SelectCalls
import kotlin.time.Duration.Companion.milliseconds

internal val Call.isInProgress
    get() = responseCode == null && error == null

internal val Call.isRedirect
    get() = responseCode?.toInt() in 300 until 400

internal val Call.isError
    get() = !error.isNullOrBlank()

internal val Call.requestTimeAsText
    get() = requestTimestamp.formatTime()

internal val Call.responseTimeAsText
    get() = responseTimestamp?.formatTime()

internal val Call.responseDateTimeAsText
    get() = responseTimestamp?.formatDateTimeTime()

internal val Call.requestDateTimeAsText
    get() = requestTimestamp.formatDateTimeTime()

internal val Call.isSecure
    get() = ParsedUrl.parse(url).isSecure

internal val Call.host
    get() = ParsedUrl.parse(url).host

internal val Call.encodedPathAndQuery
    get() = ParsedUrl.parse(url).encodedPathAndQuery

internal val Call.durationAsText
    get() = responseTimestamp?.minus(requestTimestamp)?.milliseconds?.toComponents { hours, minutes, seconds, nanoseconds ->
        val milliseconds = nanoseconds / 1_000_000
        when {
            hours > 0 -> "$hours:$minutes:$seconds $milliseconds ms"
            minutes > 0 -> "$minutes:$seconds $milliseconds ms"
            seconds > 0 -> "$seconds $milliseconds ms"
            milliseconds > 0 -> "$milliseconds ms"
            else -> null
        }
    }

internal val Call.totalSizeAsText: String?
    get() {
        responseContentLength ?: return null
        return (requestContentLength + responseContentLength).sizeAsText()
    }

internal val Call.isHttpError
    get() = when (responseCode) {
        null -> false
        in 300 until 400 -> false
        in 200 until 300 -> false
        in 100 until 200 -> false
        else -> true
    }

internal val SelectCalls.isError
    get() = when {
        !error.isNullOrEmpty() -> true
        responseCode == null -> false
        responseCode in 300 until 400 -> false
        responseCode in 200 until 300 -> false
        responseCode in 100 until 200 -> false
        else -> true
    }

internal inline fun <T> ifNotTruncated(
    isTruncated: Boolean?,
    fallback: T? = null,
    value: () -> T?,
): T? = if (isTruncated == true) fallback else value()

internal fun Map<String, List<String>>.supportsStreamTransport(): Boolean {
    val isSse = containsHeaderValue(
        name = HttpHeaders.ContentType,
        valuePart = ContentType.Text.EventStream.toString(),
    )
    val isWebSocket = containsHeaderValue(name = HttpHeaders.Upgrade, valuePart = "websocket") ||
        keys.any { it.equals(HttpHeaders.SecWebSocketKey, ignoreCase = true) }
    return isSse || isWebSocket
}

private fun Map<String, List<String>>.containsHeaderValue(
    name: String,
    valuePart: String,
): Boolean = entries
    .firstOrNull { it.key.equals(name, ignoreCase = true) }
    ?.value
    ?.any { it.contains(valuePart, ignoreCase = true) } == true

