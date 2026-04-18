package ro.cosminmihu.ktor.monitor.domain.model

/**
 * Lightweight URL parser to avoid depending on Ktor HTTP.
 */
internal class ParsedUrl private constructor(
    val protocol: String,
    val host: String,
    val encodedPathAndQuery: String,
) {
    val isSecure: Boolean
        get() = protocol == "https" || protocol == "wss"

    val isWebsocket: Boolean
        get() = protocol == "ws" || protocol == "wss"

    companion object {
        fun parse(url: String): ParsedUrl {
            // Extract protocol
            val protocolEnd = url.indexOf("://")
            val protocol = if (protocolEnd > 0) url.substring(0, protocolEnd).lowercase() else ""
            val rest = if (protocolEnd > 0) url.substring(protocolEnd + 3) else url

            // Extract host (before first / or end of string)
            val pathStart = rest.indexOf('/')
            val host = if (pathStart >= 0) rest.substring(0, pathStart) else rest

            // Extract path and query
            val encodedPathAndQuery = if (pathStart >= 0) rest.substring(pathStart) else "/"

            return ParsedUrl(
                protocol = protocol,
                host = host,
                encodedPathAndQuery = encodedPathAndQuery,
            )
        }
    }
}

