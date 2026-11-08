package ro.cosminmihu.ktor.monitor.mcp.protocol

/**
 * Loopback address the companion listens on. It is IPv4 only, so apps must not use `localhost`,
 * which may resolve to `::1`.
 */
internal const val DEFAULT_HOST: String = "127.0.0.1"

/** Default WebSocket port of the companion. Plain constants, also compiled into the no-op library. */
internal const val DEFAULT_PORT: Int = 8765
