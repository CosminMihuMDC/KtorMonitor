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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.TimeSource
import ro.cosminmihu.ktor.monitor.McpConnectionStatus
import ro.cosminmihu.ktor.monitor.mcp.protocol.APP_PATH
import ro.cosminmihu.ktor.monitor.mcp.protocol.Auth
import ro.cosminmihu.ktor.monitor.mcp.protocol.Cancel
import ro.cosminmihu.ktor.monitor.mcp.protocol.Challenge
import ro.cosminmihu.ktor.monitor.mcp.protocol.Hello
import ro.cosminmihu.ktor.monitor.mcp.protocol.HelloAck
import ro.cosminmihu.ktor.monitor.mcp.protocol.Message
import ro.cosminmihu.ktor.monitor.mcp.protocol.Request
import ro.cosminmihu.ktor.monitor.mcp.protocol.Response
import ro.cosminmihu.ktor.monitor.mcp.protocol.appProof
import ro.cosminmihu.ktor.monitor.mcp.protocol.companionProof
import ro.cosminmihu.ktor.monitor.mcp.protocol.constantTimeEquals
import ro.cosminmihu.ktor.monitor.mcp.protocol.decodeMessageOrNull
import ro.cosminmihu.ktor.monitor.mcp.protocol.newNonce
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
        val client = HttpClient { install(WebSockets) }
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
            val clientNonce = newNonce()
            send(
                Frame.Text(
                    Hello(
                        appId = settings.appId,
                        appName = settings.appName,
                        platform = settings.platform,
                        clientNonce = clientNonce,
                    ).encode()
                )
            )
            val challenge = when (val message = withTimeoutOrNull(HELLO_TIMEOUT) { readMessage() }) {
                is Challenge -> message
                is HelloAck -> {
                    // Either an explicit refusal (e.g. protocol version) or a companion without authentication.
                    status.value = McpConnectionStatus.Rejected
                    return@webSocket
                }

                else -> return@webSocket
            }
            val token = settings.token
            if (token != null &&
                !constantTimeEquals(challenge.proof, companionProof(token, clientNonce, challenge.serverNonce))
            ) {
                // The peer does not know the token: do not talk to it.
                status.value = McpConnectionStatus.Rejected
                return@webSocket
            }
            send(Frame.Text(Auth(token?.let { appProof(it, clientNonce, challenge.serverNonce) }).encode()))

            val ack = withTimeoutOrNull(HELLO_TIMEOUT) { readMessage() } as? HelloAck ?: return@webSocket
            if (!ack.ok) {
                status.value = McpConnectionStatus.Rejected
                return@webSocket
            }
            status.value = McpConnectionStatus.Connected
            onConnected()

            val permits = Semaphore(MAX_CONCURRENT_REQUESTS)
            val running = MutableStateFlow<Map<Long, Job>>(emptyMap())
            coroutineScope {
                for (frame in incoming) {
                    when (val message = (frame as? Frame.Text)?.takeIf { it.data.size <= MAX_FRAME_SIZE }?.readText()?.let(::decodeMessageOrNull)) {
                        is Cancel -> running.value[message.id]?.cancel()
                        is Request -> {
                            val job = launch {
                                val response = permits.withPermit { answer(message).encode() }
                                try {
                                    send(Frame.Text(response))
                                } catch (exception: CancellationException) {
                                    throw exception
                                } catch (_: Throwable) {
                                    // The socket closed while the query ran; the loop above ends the connection.
                                }
                            }
                            running.update { it + (message.id to job) }
                            job.invokeOnCompletion { running.update { it - message.id } }
                        }

                        else -> Unit
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
// Not enforced through the engine config: not every Ktor engine (e.g. OkHttp) supports `maxFrameSize`.
private const val MAX_FRAME_SIZE = 1024 * 1024
