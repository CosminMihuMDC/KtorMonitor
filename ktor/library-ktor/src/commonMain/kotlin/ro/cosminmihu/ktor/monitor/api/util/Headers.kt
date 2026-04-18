package ro.cosminmihu.ktor.monitor.api.util

import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import ro.cosminmihu.ktor.monitor.SanitizedHeader


internal fun HeadersBuilder.sanitizedHeaders(
    sanitizedHeaders: List<SanitizedHeader>,
): Map<String, List<String>> {
    val sortedHeaders = this.entries().toList().sortedBy { it.key }

    return sortedHeaders.associate { (key, values) ->
        val placeholder = sanitizedHeaders
            .firstOrNull { it.predicate(key) }
            ?.placeholder
            ?.let { listOf(it) }
        Pair(key, placeholder ?: values)
    }
}

internal fun Headers.sanitizedHeaders(
    sanitizedHeaders: List<SanitizedHeader>,
): Map<String, List<String>> {
    val sortedHeaders = this.entries().toList().sortedBy { it.key }

    return sortedHeaders.associate { (key, values) ->
        val placeholder = sanitizedHeaders
            .firstOrNull { it.predicate(key) }
            ?.placeholder
            ?.let { listOf(it) }
        Pair(key, placeholder ?: values)
    }
}

