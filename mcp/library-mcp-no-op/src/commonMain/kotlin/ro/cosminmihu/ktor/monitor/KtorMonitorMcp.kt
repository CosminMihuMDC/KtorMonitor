package ro.cosminmihu.ktor.monitor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_HOST
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_PORT

/**
 * No-op entry point.
 */
public object KtorMonitor {

    /**
     * No-op implementation of the MCP integration.
     */
    public object MCP {

        /**
         * Always [ConnectionStatus.Stopped].
         */
        public val status: StateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus.Stopped)

        /**
         * Does nothing.
         */
        public fun start(block: Config.() -> Unit = {}) {
        }

        /**
         * Does nothing.
         */
        public fun stop() {
        }

        /**
         * No-op configuration.
         */
        public class Config {

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
        public enum class ConnectionStatus {
            /** Never connected. */
            Stopped,

            /** Never used. */
            Connecting,

            /** Never used. */
            Connected,

            /** Never used. */
            Rejected,
        }
    }
}
