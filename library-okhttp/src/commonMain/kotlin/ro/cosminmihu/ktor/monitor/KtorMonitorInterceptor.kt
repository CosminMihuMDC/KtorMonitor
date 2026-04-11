package ro.cosminmihu.ktor.monitor

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * An [OkHttp](https://square.github.io/okhttp/) [Interceptor] that provides the capability to log HTTP calls.
 *
 * You can learn more from [KtorMonitor](https://github.com/CosminMihuMDC/KtorMonitor).
 *
 * ```kotlin
 * OkHttpClient.Builder()
 *     .addInterceptor(
 *         KtorMonitorInterceptor {
 *             sanitizeHeader { header -> header == "Authorization" }
 *             isActive = true
 *             showNotification = true
 *             retentionPeriod = RetentionPeriod.OneHour
 *             maxContentLength = ContentLength.Default
 *         }
 *     )
 *     .build()
 * ```
 */
public class KtorMonitorInterceptor(
    private val config: KtorMonitorInterceptorConfig = KtorMonitorInterceptorConfig(),
) : Interceptor {

    public constructor(block: KtorMonitorInterceptorConfig.() -> Unit) : this(KtorMonitorInterceptorConfig().apply(block))

    init {
        @OptIn(InternalKtorMonitorApi::class)
        KtorMonitorBridge.setConfig(
            isActive = config.isActive,
            showNotification = config.showNotification,
            retentionPeriod = config.retentionPeriod,
            maxContentLength = config.maxContentLength,
        )

        if (config.isActive && config.retentionPeriod.isPositive()) {
            @OptIn(InternalKtorMonitorApi::class)
            KtorMonitorBridge.startListening()
        }
    }

    @OptIn(InternalKtorMonitorApi::class, ExperimentalTime::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Check if plugin is active.
        if (!config.isActive) return chain.proceed(request)
        // Check if retention period is zero.
        if (!config.retentionPeriod.isPositive()) return chain.proceed(request)

        // Filter.
        val shouldLog = config.filters.isEmpty() || config.filters.any { it(request) }
        if (!shouldLog) return chain.proceed(request)

        // Generate call id.
        val id = callIdentifier

        // Log request.
        val requestTimestamp = Clock.System.now().toEpochMilliseconds()
        val method = request.method
        val url = request.url.toString()
        val requestContentType = request.body?.contentType()?.toString()
        val requestContentLength = request.body?.contentLength() ?: 0L

        // Headers.
        val requestHeaders = request.headers.toMultimap()
            .mapValues { (key, values) ->
                val sanitized = config.sanitizedHeaders.firstOrNull { it.predicate(key) }
                if (sanitized != null) listOf(sanitized.placeholder) else values
            }
            .toSortedMap()

        // Request body.
        val requestBody = try {
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            val bytes = buffer.readByteArray()
            if (config.maxContentLength != ContentLength.Full) {
                bytes.take(config.maxContentLength).toByteArray()
            } else bytes
        } catch (_: Throwable) {
            null
        }

        runBlocking {
            try {
                KtorMonitorBridge.saveRequest(
                    id = id,
                    method = method,
                    url = url,
                    requestTimestamp = requestTimestamp,
                    requestHeaders = requestHeaders,
                    requestContentType = requestContentType,
                    requestContentLength = requestContentLength,
                    requestBody = requestBody,
                    isRequestBodyTruncated = requestContentLength != 0L && requestContentLength > config.maxContentLength,
                )
            } catch (_: Throwable) {
            }
        }

        // Proceed with request.
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (cause: Throwable) {
            runBlocking {
                KtorMonitorBridge.saveRequestError(id = id, error = cause)
            }
            throw cause
        }

        // Log response.
        val responseTimestamp = Clock.System.now().toEpochMilliseconds()
        val responseCode = response.code
        val protocol = response.protocol.toString()
        val responseContentType = response.body?.contentType()?.toString()

        // Response headers.
        val responseHeaders = response.headers.toMultimap()
            .mapValues { (key, values) ->
                val sanitized = config.sanitizedHeaders.firstOrNull { it.predicate(key) }
                if (sanitized != null) listOf(sanitized.placeholder) else values
            }
            .toSortedMap()

        // Response body.
        val responseBodySource = response.body?.source()
        responseBodySource?.request(Long.MAX_VALUE)
        val responseBodyBytes = responseBodySource?.buffer?.clone()?.readByteArray()
        val responseContentLength = responseBodyBytes?.size?.toLong()

        val truncatedBody = if (responseBodyBytes != null && config.maxContentLength != ContentLength.Full) {
            responseBodyBytes.take(config.maxContentLength).toByteArray()
        } else {
            responseBodyBytes
        }

        runBlocking {
            try {
                KtorMonitorBridge.saveResponse(
                    id = id,
                    protocol = protocol,
                    requestTimestamp = requestTimestamp,
                    responseCode = responseCode,
                    responseTimestamp = responseTimestamp,
                    responseContentType = responseContentType,
                    responseHeaders = responseHeaders,
                )
                KtorMonitorBridge.saveResponseBody(
                    id = id,
                    responseContentLength = responseContentLength,
                    responseBody = truncatedBody,
                    isResponseBodyTruncated = (responseContentLength ?: 0) > config.maxContentLength,
                )
            } catch (_: Throwable) {
            }
        }

        return response
    }
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
