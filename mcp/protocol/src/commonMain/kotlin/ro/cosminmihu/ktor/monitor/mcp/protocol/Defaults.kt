package ro.cosminmihu.ktor.monitor.mcp.protocol

/**
 * Loopback address the companion listens on. It is IPv4 only, so apps must not use `localhost`,
 * which may resolve to `::1`.
 */
internal const val DEFAULT_HOST: String = "127.0.0.1"

/** Default WebSocket port of the companion. Plain constants, also compiled into the no-op library. */
internal const val DEFAULT_PORT: Int = 2991

/**
 * When the preferred port is busy (e.g. a companion of another project), the companion takes the next free
 * one, up to this many consecutive ports. Apps connect to every companion in this range.
 */
internal const val PORT_FALLBACK_COUNT: Int = 10
