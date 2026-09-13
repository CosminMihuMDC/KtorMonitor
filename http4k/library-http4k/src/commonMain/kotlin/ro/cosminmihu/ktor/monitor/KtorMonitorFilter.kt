package ro.cosminmihu.ktor.monitor

import kotlinx.coroutines.launch
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import ro.cosminmihu.ktor.monitor.domain.model.NetworkClient
import java.io.FilterInputStream
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * An [http4k](https://www.http4k.org/) [Filter] that provides the capability to log HTTP calls.
 *
 * You can learn more from [KtorMonitor](https://github.com/CosminMihuMDC/KtorMonitor).
 *
 * ```kotlin
 * KtorMonitorFilter {
 *     sanitizeHeader { header -> header == "Authorization" }
 *     isActive = true
 *     showNotification = true
 *     retentionPeriod = RetentionPeriod.OneHour
 *     maxContentLength = ContentLength.Default
 * }.then(JavaHttpClient())
 * ```
 */
public class KtorMonitorFilter(
    private val config: KtorMonitorFilterConfig = KtorMonitorFilterConfig(),
) : Filter {

    public constructor(block: KtorMonitorFilterConfig.() -> Unit) : this(
        KtorMonitorFilterConfig().apply(block)
    )

    init {
        @OptIn(InternalKtorMonitorApi::class)
        InternalLibraryBridge.setConfig(
            isActive = config.isActive,
            showNotification = config.showNotification,
            retentionPeriod = config.retentionPeriod,
            maxContentLength = config.maxContentLength,
            networkClient = NetworkClient.Http4k,
        )

        if (config.isActive && config.retentionPeriod.isPositive()) {
            @OptIn(InternalKtorMonitorApi::class)
            InternalLibraryBridge.startListening()
        }
    }

    @OptIn(InternalKtorMonitorApi::class, ExperimentalTime::class)
    override fun invoke(next: HttpHandler): HttpHandler = { request ->
        // Check if plugin is active.
        if (!config.isActive || !config.retentionPeriod.isPositive()) {
            next(request)
        } else {
            // Filter.
            val shouldLog = config.filters.isEmpty() || config.filters.any { it(request) }
            if (!shouldLog) {
                next(request)
            } else {
                // Generate call id.
                val id = callIdentifier

                // Log request.
                val requestTimestamp = Clock.System.now().toEpochMilliseconds()
                val method = request.method.name
                val url = request.uri.toString()
                val requestContentType = request.header("Content-Type")

                // Request body bytes — duplicate ByteBuffer to avoid consuming the original.
                val requestBodyBytes = try {
                    val buf = request.body.payload.duplicate()
                    val bytes = ByteArray(buf.remaining()).also { buf.get(it) }
                    if (config.maxContentLength != ContentLength.Full) {
                        bytes.take(config.maxContentLength).toByteArray()
                    } else bytes
                } catch (_: Throwable) {
                    null
                }
                val requestContentLength = request.body.length
                    ?: requestBodyBytes?.size?.toLong()
                    ?: 0L

                // Request headers.
                val requestHeaders = request.headers
                    .groupBy({ it.first }) { it.second ?: "" }
                    .mapValues { (key, values) ->
                        val sanitized = config.sanitizedHeaders.firstOrNull { it.predicate(key) }
                        if (sanitized != null) listOf(sanitized.placeholder) else values
                    }
                    .toSortedMap()

                InternalLibraryBridge.coroutineScope().launch {
                    try {
                        InternalLibraryBridge.saveRequest(
                            id = id,
                            method = method,
                            url = url,
                            requestTimestamp = requestTimestamp,
                            requestHeaders = requestHeaders,
                            requestContentType = requestContentType,
                            requestContentLength = requestContentLength,
                            requestBody = requestBodyBytes,
                            isRequestBodyTruncated = requestContentLength != 0L && requestContentLength > config.maxContentLength,
                        )
                    } catch (_: Throwable) {
                    }
                }

                // Proceed with request.
                val response = try {
                    next(request)
                } catch (cause: Throwable) {
                    InternalLibraryBridge.coroutineScope().launch {
                        try {
                            InternalLibraryBridge.saveRequestError(id = id, error = cause)
                        } catch (_: Throwable) {
                        }
                    }
                    throw cause
                }

                // Log response.
                val responseTimestamp = Clock.System.now().toEpochMilliseconds()
                val responseCode = response.status.code
                // http4k does not expose the HTTP protocol version; default to HTTP/1.1.
                val protocol = "HTTP/1.1"
                val responseContentType = response.header("Content-Type")

                // Response headers.
                val responseHeaders = response.headers
                    .groupBy({ it.first }) { it.second ?: "" }
                    .mapValues { (key, values) ->
                        val sanitized = config.sanitizedHeaders.firstOrNull { it.predicate(key) }
                        if (sanitized != null) listOf(sanitized.placeholder) else values
                    }
                    .toSortedMap()

                InternalLibraryBridge.coroutineScope().launch {
                    try {
                        InternalLibraryBridge.saveResponse(
                            id = id,
                            protocol = protocol,
                            requestTimestamp = requestTimestamp,
                            responseCode = responseCode,
                            responseTimestamp = responseTimestamp,
                            responseContentType = responseContentType,
                            responseHeaders = responseHeaders,
                        )
                    } catch (_: Throwable) {
                    }
                }

                // Server-Sent Events — the body never closes on its own, so eagerly reading
                // `payload` (which fully drains the stream) would block indefinitely. Instead,
                // wrap the stream so each chunk read by the consumer is mirrored into the DB,
                // letting the detail screen update live.
                val isServerSentEvents = responseContentType
                    ?.substringBefore(';')
                    ?.trim()
                    ?.equals(ContentType.TEXT_EVENT_STREAM.value, ignoreCase = true) == true

                if (isServerSentEvents) {
                    wrapForStreaming(response, id, config.maxContentLength)
                } else {
                    // Response body — duplicate ByteBuffer so the caller still receives a full body.
                    val responseBodyBytes = try {
                        val buf = response.body.payload.duplicate()
                        ByteArray(buf.remaining()).also { buf.get(it) }
                    } catch (_: Throwable) {
                        null
                    }
                    val responseContentLength = responseBodyBytes?.size?.toLong()
                        ?: response.body.length
                        ?: 0L

                    val truncatedBody = if (config.maxContentLength != ContentLength.Full) {
                        responseBodyBytes?.take(config.maxContentLength)?.toByteArray()
                    } else {
                        responseBodyBytes
                    }
                    val isResponseBodyTruncated = responseContentLength > config.maxContentLength

                    InternalLibraryBridge.coroutineScope().launch {
                        try {
                            InternalLibraryBridge.saveResponseBody(
                                id = id,
                                responseContentLength = responseContentLength,
                                responseBody = truncatedBody ?: ByteArray(0),
                                isResponseBodyTruncated = isResponseBodyTruncated,
                            )
                        } catch (_: Throwable) {
                        }
                    }

                    response
                }
            }
        }
    }
}

@OptIn(InternalKtorMonitorApi::class)
private fun wrapForStreaming(
    response: Response,
    id: String,
    maxContentLength: Int,
): Response {
    // Initialise the body row so the UI shows an empty (streaming) response immediately.
    InternalLibraryBridge.coroutineScope().launch {
        try {
            InternalLibraryBridge.saveResponseBody(
                id = id,
                responseContentLength = 0L,
                responseBody = ByteArray(0),
                isResponseBodyTruncated = false,
            )
        } catch (_: Throwable) {
        }
    }

    val unbounded = maxContentLength == ContentLength.Full
    val originalLength = response.body.length

    val tee = object : FilterInputStream(response.body.stream) {
        private var stored: Long = 0L
        private var truncated: Boolean = false

        override fun read(): Int {
            val byte = super.read()
            if (byte < 0) return byte
            append(byteArrayOf(byte.toByte()), 1L)
            return byte
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val read = super.read(b, off, len)
            if (read <= 0) return read
            append(b.copyOfRange(off, off + read), read.toLong())
            return read
        }

        private fun append(newBytes: ByteArray, read: Long) {
            val keep: ByteArray = when {
                unbounded -> newBytes
                stored >= maxContentLength -> {
                    truncated = true
                    ByteArray(0)
                }
                stored + newBytes.size > maxContentLength -> {
                    truncated = true
                    val remaining = (maxContentLength - stored).toInt()
                    newBytes.copyOf(min(remaining, newBytes.size))
                }
                else -> newBytes
            }

            if (keep.isNotEmpty()) {
                stored += keep.size
            }
            val isTruncated = truncated

            InternalLibraryBridge.coroutineScope().launch {
                try {
                    InternalLibraryBridge.appendResponseBody(
                        id = id,
                        chunk = keep,
                        deltaSize = read,
                        isResponseBodyTruncated = isTruncated,
                    )
                } catch (_: Throwable) {
                }
            }
        }
    }

    return response.body(tee, originalLength)
}

@OptIn(ExperimentalTime::class)
private val callIdentifier: String
    get() {
        val timestamp = Clock.System.now().toEpochMilliseconds().toString()
        val randomLong = Random.nextLong().toString()
        val raw = "$timestamp-$randomLong"
        val hash = abs(raw.hashCode()).toString(16)
        return "$raw-$hash"
    }

