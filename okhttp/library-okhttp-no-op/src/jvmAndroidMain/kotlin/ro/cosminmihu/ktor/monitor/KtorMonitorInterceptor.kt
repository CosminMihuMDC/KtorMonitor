package ro.cosminmihu.ktor.monitor

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * No-op implementation.
 */
public class KtorMonitorInterceptor : Interceptor {

    public constructor(block: KtorMonitorInterceptorConfig.() -> Unit)

    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}

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

