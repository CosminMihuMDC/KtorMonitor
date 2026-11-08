package ro.cosminmihu.ktor.monitor

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import ro.cosminmihu.ktor.monitor.mcp.McpClient
import ro.cosminmihu.ktor.monitor.mcp.McpSettings
import ro.cosminmihu.ktor.monitor.mcp.platformName
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_PORT

/**
 * Lets AI agents (Copilot, Claude, Cursor, ...) query the calls captured by Ktor Monitor, through
 * the [Model Context Protocol](https://modelcontextprotocol.io).
 *
 * The app connects **to** a small companion process running on the developer machine, which exposes the
 * MCP tools to the AI. This works the same on Android, iOS, desktop and web.
 *
 * Start the companion (`ktor-monitor-mcp-companion-<version>-all.jar`) from the AI client MCP config,
 * then start the connection in debug builds:
 * ```kotlin
 * KtorMonitorMcp.start {
 *     port = 8765
 *     token = "my-secret" // optional, must match the companion --token
 * }
 * ```
 *
 * The AI gets read-only access to the stored calls. Header values hidden with `sanitizeHeader` are
 * already masked in the stored data, so they are never exposed. Bodies are **not** sanitized.
 */
public object KtorMonitorMcp {

    private val job = MutableStateFlow<Job?>(null)
    private val mutableStatus = MutableStateFlow(McpConnectionStatus.Stopped)

    /**
     * The state of the connection to the companion.
     */
    public val status: StateFlow<McpConnectionStatus> = mutableStatus.asStateFlow()

    /**
     * Connect to the companion. Reconnects automatically until [stop] is called.
     * Calling it again restarts the connection with the new [block] settings.
     */
    @OptIn(InternalKtorMonitorApi::class)
    public fun start(block: KtorMonitorMcpConfig.() -> Unit = {}) {
        val config = KtorMonitorMcpConfig().apply(block)
        val settings = McpSettings(
            host = config.host,
            port = config.port,
            token = config.token,
            appId = InternalLibraryBridge.newCallId(),
            appName = config.appName ?: platformName(),
            platform = platformName(),
        )
        val client = McpClient(
            settings = settings,
            status = mutableStatus,
            query = InternalLibraryBridge::mcpQuery,
        )
        mutableStatus.value = McpConnectionStatus.Connecting
        val newJob = InternalLibraryBridge.coroutineScope().launch(start = CoroutineStart.LAZY) {
            launch { mutableStatus.collect { publish(it) } }
            client.run()
        }
        job.getAndUpdate { newJob }?.cancel()
        newJob.start()
    }

    /**
     * Disconnect from the companion.
     */
    public fun stop() {
        job.getAndUpdate { null }?.cancel()
        mutableStatus.value = McpConnectionStatus.Stopped
        publish(McpConnectionStatus.Stopped)
    }

    @OptIn(InternalKtorMonitorApi::class)
    private fun publish(status: McpConnectionStatus) {
        InternalLibraryBridge.setMcpStatus(
            when (status) {
                McpConnectionStatus.Stopped -> null
                McpConnectionStatus.Connecting -> false
                McpConnectionStatus.Connected -> true
            }
        )
    }
}

/**
 * Configuration for [KtorMonitorMcp.start].
 */
public class KtorMonitorMcpConfig {

    /**
     * The machine running the companion. By default `localhost`.
     *
     * - Android emulator and device: run `adb reverse tcp:8765 tcp:8765` and keep `localhost`
     *   (or use `10.0.2.2` on the emulator).
     * - iOS simulator, desktop, web: `localhost`.
     */
    public var host: String = "localhost"

    /**
     * The companion port. By default 8765.
     */
    public var port: Int = DEFAULT_PORT

    /**
     * Shared secret, must match the companion `--token`. By default none.
     */
    public var token: String? = null

    /**
     * The name shown by the `list_apps` tool. By default the platform name.
     */
    public var appName: String? = null
}

/**
 * The state of the connection to the MCP companion.
 */
public enum class McpConnectionStatus {
    /** [KtorMonitorMcp.start] was not called or [KtorMonitorMcp.stop] was called. */
    Stopped,

    /** Trying to reach the companion. */
    Connecting,

    /** Connected, AI agents can query the calls. */
    Connected,
}
