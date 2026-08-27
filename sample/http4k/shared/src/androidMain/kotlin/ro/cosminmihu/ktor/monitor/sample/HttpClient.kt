package ro.cosminmihu.ktor.monitor.sample

import org.http4k.client.OkHttp
import org.http4k.core.HttpHandler
import org.http4k.core.then
import ro.cosminmihu.ktor.monitor.ContentLength
import ro.cosminmihu.ktor.monitor.KtorMonitorFilter
import ro.cosminmihu.ktor.monitor.RetentionPeriod

internal actual fun httpClient(): HttpHandler =
    KtorMonitorFilter {
        sanitizeHeader { header -> header == "Authorization" }
        filter { request -> !request.uri.host.contains("cosminmihu.ro") }
        showNotification = true
        retentionPeriod = RetentionPeriod.OneHour
        maxContentLength = ContentLength.Default
    }.then(OkHttp())

