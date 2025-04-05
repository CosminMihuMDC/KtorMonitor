@file:Suppress("MemberVisibilityCanBePrivate", "ConstPropertyName")

package ro.cosminmihu.ktor.monitor

import io.ktor.client.plugins.api.ClientPlugin
import ro.cosminmihu.ktor.monitor.api.LoggingPlugin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * No-op implementation.
 */
public val KtorMonitorLogging: ClientPlugin<KtorMonitorLoggingConfig> = LoggingPlugin

public object RetentionPeriod {
    public val OneHour: Duration = 1.hours
    public val OneDay: Duration = 1.days
    public val OneWeek: Duration = OneDay * 7
    public val Forever: Duration = Duration.INFINITE
}

public object ContentLength {
    public const val Default: Int = 250_000
    public const val Full: Int = Int.MAX_VALUE
}