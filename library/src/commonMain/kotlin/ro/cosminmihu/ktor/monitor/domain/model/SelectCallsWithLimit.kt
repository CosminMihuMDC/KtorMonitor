package ro.cosminmihu.ktor.monitor.domain.model

import io.ktor.http.URLBuilder
import io.ktor.http.isSecure
import io.ktor.http.isWebsocket
import ro.cosminmihu.ktor.monitor.db.sqldelight.SelectCalls
import ro.cosminmihu.ktor.monitor.db.sqldelight.SelectCallsWithLimit
import kotlin.time.Duration.Companion.milliseconds

internal val SelectCallsWithLimit.isInProgress
    get() = responseCode == null && error == null

internal val SelectCallsWithLimit.isError
    get() = !error.isNullOrBlank()

internal val SelectCallsWithLimit.encodedPathAndQuery
    get() = URLBuilder(url).build().encodedPathAndQuery


internal val SelectCalls.durationAsText
    get() = responseTimestamp?.minus(requestTimestamp)?.milliseconds?.toComponents { hours, minutes, seconds, nanoseconds ->
        val milliseconds = nanoseconds / 1_000_000
        when {
            hours > 0 -> "$hours:$minutes:$seconds $milliseconds ms"
            minutes > 0 -> "$minutes:$seconds $milliseconds ms"
            seconds > 0 -> "$seconds $milliseconds ms"
            milliseconds > 0 -> "$milliseconds ms"
            else -> null
        }
    }

internal val SelectCalls.requestTimeAsText
    get() = requestTimestamp.formatTime()

internal val SelectCalls.isSecure
    get() = URLBuilder(url).build().protocol.isSecure()

internal val SelectCalls.isWebsocket
    get() = URLBuilder(url).build().protocol.isWebsocket()

internal val SelectCalls.host
    get() = URLBuilder(url).build().host

internal val SelectCalls.encodedPathAndQuery
    get() = URLBuilder(url).build().encodedPathAndQuery
