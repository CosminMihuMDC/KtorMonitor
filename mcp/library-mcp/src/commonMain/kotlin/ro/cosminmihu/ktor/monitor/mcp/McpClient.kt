package ro.cosminmihu.ktor.monitor.mcp

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
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
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.TimeSource
import ro.cosminmihu.ktor.monitor.McpConnectionStatus
import ro.cosminmihu.ktor.monitor.mcp.protocol.APP_PATH
import ro.cosminmihu.ktor.monitor.mcp.protocol.Hello
import ro.cosminmihu.ktor.monitor.mcp.protocol.HelloAck
import ro.cosminmihu.ktor.monitor.mcp.protocol.Message
import ro.cosminmihu.ktor.monitor.mcp.protocol.Request
import ro.cosminmihu.ktor.monitor.mcp.protocol.Response
import ro.cosminmihu.ktor.monitor.mcp.protocol.decodeMessageOrNull
import ro.cosminmihu.ktor.monitor.mcp.protocol.encode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
    private val retryDelays: List<Duration> = listOf(1.seconds, 2.seconds, 5.seconds, 10.seconds),
) {

    suspend fun run() {
        val client = HttpClient { install(WebSockets) { maxFrameSize = MAX_FRAME_SIZE } }
        var attempt = 0
        try {
            while (currentCoroutineContext().isActive) {
                if (status.value != McpConnectionStatus.Rejected) status.value = McpConnectionStatus.Connecting
                var connectedAt: TimeSource.Monotonic.ValueTimeMark? = null
                try {
                    connect(client) { connectedAt = TimeSource.Monotonic.markNow() }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Throwable) {
                    // Companion not running or connection lost, retry.
                }
                if (status.value != McpConnectionStatus.Rejected) status.value = McpConnectionStatus.Connecting
                // Only reset the backoff when the connection was stable, so a companion that drops us right away is not hammered.
                if (connectedAt?.let { it.elapsedNow() >= STABLE_CONNECTION } == true) attempt = 0
                delay(retryDelays[minOf(attempt++, retryDelays.lastIndex)])
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
            val ack = withTimeoutOrNull(HELLO_TIMEOUT) { readMessage() } as? HelloAck ?: return@webSocket
            if (!ack.ok) {
                status.value = McpConnectionStatus.Rejected
                return@webSocket
            }
            status.value = McpConnectionStatus.Connected
            onConnected()

            val permits = Semaphore(MAX_CONCURRENT_REQUESTS)
            coroutineScope {
                for (frame in incoming) {
                    val request = (frame as? Frame.Text)?.readText()
                        ?.let(::decodeMessageOrNull) as? Request
                        ?: continue
                    launch {
                        val response = permits.withPermit { answer(request).encode() }
                        try {
                            send(Frame.Text(response))
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (_: Throwable) {
                            // The socket closed while the query ran; the loop above ends the connection.
                        }
                    }
                }
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.readMessage(): Message? {
        for (frame in incoming) {
            val text = (frame as? Frame.Text)?.readText() ?: continue
            return decodeMessageOrNull(text)
        }
        return null
    }

    private suspend fun answer(request: Request): Response = try {
        Response(request.id, result = query(request.method, request.params.toString()))
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Throwable) {
        Response(request.id, error = exception.message ?: exception::class.simpleName ?: "Error")
    }
}

private val HELLO_TIMEOUT = 10.seconds
private val STABLE_CONNECTION = 30.seconds
private const val MAX_CONCURRENT_REQUESTS = 4
private const val MAX_FRAME_SIZE = 16L * 1024 * 1024
