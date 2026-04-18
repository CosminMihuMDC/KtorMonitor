package ro.cosminmihu.ktor.monitor

import okhttp3.Request
import kotlin.time.Duration

/**
 * DSL marker for [KtorMonitorInterceptorConfig].
 */
@DslMarker
public annotation class KtorMonitorInterceptorDsl

/**
 * No-op implementation.
 */
@KtorMonitorInterceptorDsl
public class KtorMonitorInterceptorConfig {

    public fun filter(predicate: (Request) -> Boolean) {
        // Not implemented.
    }

    public fun sanitizeHeader(placeholder: String = "***", predicate: (String) -> Boolean) {
        // Not implemented.
    }

    public var isActive: Boolean = false

    public var showNotification: Boolean = false

    public var retentionPeriod: Duration = Duration.ZERO

    public var maxContentLength: Int = 0
}

