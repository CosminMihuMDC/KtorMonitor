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
