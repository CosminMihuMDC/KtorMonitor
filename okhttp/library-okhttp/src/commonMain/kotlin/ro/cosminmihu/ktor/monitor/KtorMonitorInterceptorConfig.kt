package ro.cosminmihu.ktor.monitor

import okhttp3.Request
import kotlin.time.Duration

/**
 * DSL marker for [KtorMonitorInterceptorConfig].
 */
@DslMarker
public annotation class KtorMonitorInterceptorDsl

/**
 * Configuration for [KtorMonitorInterceptor].
 */
@KtorMonitorInterceptorDsl
public class KtorMonitorInterceptorConfig {

    internal val filters = mutableListOf<(Request) -> Boolean>()
    internal val sanitizedHeaders = mutableListOf<SanitizedHeader>()

    /**
     * Allows you to filter logs for calls matching a [predicate].
     */
    public fun filter(predicate: (Request) -> Boolean) {
        filters.add(predicate)
    }

    /**
     * Allows you to sanitize sensitive headers to avoid their values appearing in the logs.
     * In the example below, Authorization header value will be replaced with '***' when logging:
     * ```kotlin
     * sanitizeHeader { header -> header == "Authorization" }
     * ```
     */
    public fun sanitizeHeader(placeholder: String = "***", predicate: (String) -> Boolean) {
        sanitizedHeaders.add(SanitizedHeader(placeholder, predicate))
    }

    /**
     * Enable or disable the logging of requests and responses.
     * By default:
     * - android   - enabled for debug build type and disabled for release build type.
     * - desktop   - enabled.
     */
    @OptIn(InternalKtorMonitorApi::class)
    public var isActive: Boolean = InternalLibraryBridge.isDebug

    /**
     * Keep track of latest requests and responses into notification.
     * By default:
     * - android   - enabled. android.permission.POST_NOTIFICATIONS needs to be granted.
     * - desktop   - not supported.
     */
    public var showNotification: Boolean = true

    /**
     * The retention period for the logs.
     * By default, it is 1 hour.
     */
    public var retentionPeriod: Duration = RetentionPeriod.OneHour

    /**
     * The maximum length of the content that will be logged.
     * After this, body will be truncated.
     * By default, it is [ContentLength.Default].
     * Use [ContentLength.Full] to log the full content.
     */
    public var maxContentLength: Int = ContentLength.Default
}

/**
 * Configuration for a sanitized header.
 */
internal class SanitizedHeader(
    val placeholder: String,
    val predicate: (String) -> Boolean,
)
