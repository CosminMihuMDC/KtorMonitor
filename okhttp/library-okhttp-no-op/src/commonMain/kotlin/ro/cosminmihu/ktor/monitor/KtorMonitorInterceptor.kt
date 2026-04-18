package ro.cosminmihu.ktor.monitor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * No-op implementation.
 */
public class KtorMonitorInterceptor() : Interceptor {

    public constructor(block: KtorMonitorInterceptorConfig.() -> Unit) : this()

    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}
