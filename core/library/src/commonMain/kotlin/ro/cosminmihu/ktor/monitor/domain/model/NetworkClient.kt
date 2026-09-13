package ro.cosminmihu.ktor.monitor.domain.model

/**
 * Identifies which HTTP client library is being monitored.
 *
 * This type is exposed only for cross-module communication via
 * [ro.cosminmihu.ktor.monitor.InternalLibraryBridge] and is **not** part of the public API.
 */
public enum class NetworkClient {
    Ktor,
    OkHttp,
    Http4k,
}