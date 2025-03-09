package ro.cosminmihu.ktor.monitor

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.utils.io.KtorDsl
import kotlin.time.Duration

@KtorDsl
public class KtorMonitorLoggingConfig {
    public fun filter(predicate: (HttpRequestBuilder) -> Boolean) {
    }
    public fun sanitizeHeader(placeholder: String = "***", predicate: (String) -> Boolean) {
    }
    public var isActive: Boolean = false
    public var showNotification: Boolean = true
    public var retentionPeriod: Duration = Duration.ZERO
    public var maxContentLength: Int = ContentLength.Default
}
