package ro.cosminmihu.ktor.monitor.mcp

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ro.cosminmihu.ktor.monitor.McpConnectionStatus
import ro.cosminmihu.ktor.monitor.mcp.protocol.APP_PATH
import ro.cosminmihu.ktor.monitor.mcp.protocol.Hello
import ro.cosminmihu.ktor.monitor.mcp.protocol.HelloAck
import ro.cosminmihu.ktor.monitor.mcp.protocol.Message
import ro.cosminmihu.ktor.monitor.mcp.protocol.Request
import ro.cosminmihu.ktor.monitor.mcp.protocol.Response
import ro.cosminmihu.ktor.monitor.mcp.protocol.decodeMessage
import ro.cosminmihu.ktor.monitor.mcp.protocol.encode

internal class McpSettings(
    val host: String,
    val port: Int,
    val token: String?,
    val appId: String,
    val appName: String,
    val platform: String,
)

/** WebSocket client that answers the companion requests with the stored calls. */
internal class McpClient(
    private val settings: McpSettings,
    private val status: MutableStateFlow<McpConnectionStatus>,
    private val query: suspend (method: String, paramsJson: String) -> String,
    private val retryDelaysMillis: List<Long> = listOf(1_000, 2_000, 5_000, 10_000),
) {

    suspend fun run() {
        val client = HttpClient { install(WebSockets) }
        var attempt = 0
        try {
            while (currentCoroutineContext().isActive) {
                status.value = McpConnectionStatus.Connecting
                try {
                    connect(client) { attempt = 0 }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Throwable) {
                    // Companion not running or connection lost, retry.
                }
                status.value = McpConnectionStatus.Connecting
                delay(retryDelaysMillis[minOf(attempt++, retryDelaysMillis.lastIndex)])
            }
        } finally {
            client.close()
        }
    }

    private suspend fun connect(client: HttpClient, onConnected: () -> Unit) {
        client.webSocket(host = settings.host, port = settings.port, path = APP_PATH) {
            send(
                Frame.Text(
                    Hello(
                        appId = settings.appId,
                        appName = settings.appName,
                        platform = settings.platform,
                        token = settings.token,
                    ).encode()
                )
            )
            val ack = readMessage() as? HelloAck ?: return@webSocket
            if (!ack.ok) return@webSocket
            status.value = McpConnectionStatus.Connected
            onConnected()

            coroutineScope {
                for (frame in incoming) {
                    val request = (frame as? Frame.Text)?.readText()
                        ?.let { runCatching { decodeMessage(it) }.getOrNull() } as? Request
                        ?: continue
                    launch {
                        send(Frame.Text(answer(request).encode()))
                    }
                }
            }
        }
    }

    private suspend fun io.ktor.client.plugins.websocket.DefaultClientWebSocketSession.readMessage(): Message? {
        for (frame in incoming) {
            val text = (frame as? Frame.Text)?.readText() ?: continue
            return runCatching { decodeMessage(text) }.getOrNull()
        }
        return null
    }

    private suspend fun answer(request: Request): Response = try {
        Response(request.id, result = query(request.method, request.params.toString()))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Response(request.id, error = e.message ?: e::class.simpleName ?: "Error")
    }
}
