package ro.cosminmihu.ktor.monitor

import okhttp3.Request
import kotlin.time.Duration

/**
 * No-op implementation.
 */
public class KtorMonitorInterceptorConfig() {
    public constructor(block: KtorMonitorInterceptorConfig.() -> Unit) : this() {
        apply(block)
    }

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

