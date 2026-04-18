package ro.cosminmihu.ktor.monitor.api.util

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.http.contentType
import ro.cosminmihu.ktor.monitor.ContentLength
import ro.cosminmihu.ktor.monitor.InternalKtorMonitorApi
import ro.cosminmihu.ktor.monitor.KtorMonitorBridge
import ro.cosminmihu.ktor.monitor.SanitizedHeader

@OptIn(InternalKtorMonitorApi::class)
internal suspend fun logResponseException(
    id: String,
    cause: Throwable,
) {
    KtorMonitorBridge.saveResponseError(
        id = id,
        error = cause,
    )
}

@OptIn(InternalKtorMonitorApi::class)
internal suspend fun logResponse(
    id: String,
    response: HttpResponse,
    sanitizedHeaders: List<SanitizedHeader>,
) {
    val headers = response.headers.sanitizedHeaders(sanitizedHeaders)

    // Save response.
    KtorMonitorBridge.saveResponse(
        id = id,
        protocol = response.version.toString(),
        requestTimestamp = response.requestTime.timestamp,
        responseCode = response.status.value,
        responseTimestamp = response.responseTime.timestamp,
        responseContentType = response.contentType()?.toString(),
        responseHeaders = headers,
    )
}

@OptIn(InternalKtorMonitorApi::class)
internal suspend fun logResponseBody(
    id: String,
    maxContentLength: Int,
    response: HttpResponse,
) {
    // Read content.
    val responseBody = response.readRawBytes()

    val body = when {
        maxContentLength != ContentLength.Full -> responseBody
            .take(maxContentLength)
            .toByteArray()

        else -> responseBody
    }

    // Save response body.
    KtorMonitorBridge.saveResponseBody(
        id = id,
        responseContentLength = responseBody.size.toLong(),
        responseBody = body,
        isResponseBodyTruncated = responseBody.size > maxContentLength,
    )
}

