package ro.cosminmihu.ktor.monitor

import io.ktor.client.plugins.api.ClientPlugin
import ro.cosminmihu.ktor.monitor.api.LoggingPlugin

/**
 * No-op implementation.
 */
public val KtorMonitorLogging: ClientPlugin<KtorMonitorLoggingConfig> = LoggingPlugin
