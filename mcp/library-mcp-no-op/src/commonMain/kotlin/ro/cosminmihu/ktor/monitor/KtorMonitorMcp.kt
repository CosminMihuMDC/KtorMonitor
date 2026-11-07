package ro.cosminmihu.ktor.monitor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
    public var host: String = "localhost"
    public var port: Int = 8765
    public var token: String? = null
    public var appName: String? = null
}

/**
 * No-op connection state.
 */
public enum class McpConnectionStatus {
    Stopped,
    Connecting,
    Connected,
}
