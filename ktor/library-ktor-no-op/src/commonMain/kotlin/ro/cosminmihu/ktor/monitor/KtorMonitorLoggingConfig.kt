package ro.cosminmihu.ktor.monitor

import io.ktor.client.request.HttpRequestBuilder
import kotlin.time.Duration

/**
 * No-op implementation.
 */
public class KtorMonitorLoggingConfig {

    public fun filter(predicate: (HttpRequestBuilder) -> Boolean) {
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

