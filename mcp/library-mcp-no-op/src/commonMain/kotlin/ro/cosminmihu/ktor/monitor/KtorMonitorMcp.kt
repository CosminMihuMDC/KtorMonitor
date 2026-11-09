package ro.cosminmihu.ktor.monitor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_HOST
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_PORT

/**
 * No-op implementation of `KtorMonitorMcp`.
 */
public object KtorMonitorMcp {

    /**
     * Always [McpConnectionStatus.Stopped].
     */
    public val status: StateFlow<McpConnectionStatus> = MutableStateFlow(McpConnectionStatus.Stopped)

    /**
     * Does nothing.
     */
    public fun start(block: KtorMonitorMcpConfig.() -> Unit = {}) {
    }

    /**
     * Does nothing.
     */
    public fun stop() {
    }
}

/**
 * No-op configuration.
 */
public class KtorMonitorMcpConfig {

    /** Ignored. */
    public var host: String = DEFAULT_HOST

    /** Ignored. */
    public var port: Int = DEFAULT_PORT

    /** Ignored. */
    public var token: String? = null

    /** Ignored. */
    public var appName: String? = null
}

/**
 * No-op connection state.
 */
public enum class McpConnectionStatus {
    /** Never connected. */
    Stopped,

    /** Never used. */
    Connecting,

    /** Never used. */
    Connected,

    /** Never used. */
    Rejected,
}
