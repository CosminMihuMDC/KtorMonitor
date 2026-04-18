package ro.cosminmihu.ktor.monitor

import io.ktor.client.plugins.api.ClientPlugin
import ro.cosminmihu.ktor.monitor.api.LoggingPlugin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * A [Ktor](https://ktor.io/) client plugin that provides the capability to log HTTP calls.
 *
 * You can learn more from [KtorMonitor](https://github.com/CosminMihuMDC/KtorMonitor).
 *
 * ```kotlin
 * HttpClient {
 *    install(KtorMonitorLogging) {
 *       sanitizeHeader { header -> header == "Authorization" }
 *       filter { request -> !request.url.host.contains("cosminmihu.ro") }
 *       isActive = true
 *       showNotification = true
 *       retentionPeriod = RetentionPeriod.OneHour
 *       maxContentLength = ContentLength.Default
 *    }
 * }
 * ```
 */
public val KtorMonitorLogging: ClientPlugin<KtorMonitorLoggingConfig> = LoggingPlugin