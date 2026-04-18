package ro.cosminmihu.ktor.monitor.sample.okhttp

import okhttp3.OkHttpClient
import ro.cosminmihu.ktor.monitor.ContentLength
import ro.cosminmihu.ktor.monitor.KtorMonitorInterceptor
import ro.cosminmihu.ktor.monitor.RetentionPeriod

internal fun httpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor(
            KtorMonitorInterceptor {
                sanitizeHeader { header -> header == "Authorization" }
                filter { request -> !request.url.host.contains("cosminmihu.ro") }
                showNotification = true
                retentionPeriod = RetentionPeriod.OneHour
                maxContentLength = ContentLength.Default
            }
        )
        .build()

