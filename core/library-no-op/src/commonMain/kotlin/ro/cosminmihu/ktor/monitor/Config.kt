package ro.cosminmihu.ktor.monitor

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * The retention period for the logs.
 */
public object RetentionPeriod {
    public val OneHour: Duration = 1.hours
    public val OneDay: Duration = 1.days
    public val OneWeek: Duration = OneDay * 7
    public val Forever: Duration = Duration.INFINITE
}

/**
 * The maximum length of the content that will be logged.
 * After this response body will be truncated.
 */
public object ContentLength {
    /**
     * The default value for the maximum length of the content that will be logged.
     */
    public const val Default: Int = 250_000

    /**
     * The content will not be truncated.
     */
    public const val Full: Int = Int.MAX_VALUE
}

