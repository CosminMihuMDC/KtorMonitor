package ro.cosminmihu.ktor.monitor.mcp

import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ro.cosminmihu.ktor.monitor.McpConnectionStatus
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_HOST
import ro.cosminmihu.ktor.monitor.mcp.protocol.APP_PATH
import ro.cosminmihu.ktor.monitor.mcp.protocol.Hello
import ro.cosminmihu.ktor.monitor.mcp.protocol.HelloAck
import ro.cosminmihu.ktor.monitor.mcp.protocol.Request
import ro.cosminmihu.ktor.monitor.mcp.protocol.Response
import ro.cosminmihu.ktor.monitor.mcp.protocol.decodeMessage
import ro.cosminmihu.ktor.monitor.mcp.protocol.encode
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class McpClientTest {

    private fun settings(port: Int) = McpSettings(
        host = DEFAULT_HOST,
        port = port,
        token = "secret",
        appId = "app-1",
        appName = "Sample",
        platform = "Test",
    )

    @Test
    fun answersRequestsWithTheQueryResultAndReportsErrors() = runBlocking {
        val port = ServerSocket(0).use { it.localPort }
        val hello = CompletableDeferred<Hello>()
        val ok = CompletableDeferred<Response>()
        val failed = CompletableDeferred<Response>()

        val server = embeddedServer(CIO, port = port, host = DEFAULT_HOST) {
            install(WebSockets)
            routing {
                webSocket(APP_PATH) {
                    hello.complete(decodeMessage((incoming.receive() as Frame.Text).readText()) as Hello)
                    send(Frame.Text(HelloAck(ok = true).encode()))
                    send(Frame.Text(Request(1, "list_calls", buildJsonObject { put("limit", 5) }).encode()))
                    send(Frame.Text(Request(2, "boom", buildJsonObject {}).encode()))
                    for (frame in incoming) {
                        val response = decodeMessage((frame as Frame.Text).readText()) as Response
                        if (response.id == 1L) ok.complete(response) else failed.complete(response)
                    }
                }
            }
        }.start(wait = false)

        val status = MutableStateFlow(McpConnectionStatus.Stopped)
        val job = launch(Dispatchers.IO) {
            McpClient(
                settings = settings(port),
                status = status,
                query = { method, params ->
                    if (method == "boom") error("failed query")
                    """{"method":"$method","params":$params}"""
                },
            ).run()
        }
        try {
            withTimeout(10.seconds) {
                assertEquals("app-1", hello.await().appId)
                assertEquals("secret", hello.await().token)
                assertEquals("""{"method":"list_calls","params":{"limit":5}}""", ok.await().result)
                assertEquals("failed query", failed.await().error)
                assertEquals(McpConnectionStatus.Connected, status.first { it != McpConnectionStatus.Connecting })
            }
        } finally {
            job.cancel()
            job.join()
            server.stop(100, 500)
        }
    }

    @Test
    fun keepsRetryingUntilTheCompanionStarts() = runBlocking {
        val port = ServerSocket(0).use { it.localPort }
        val status = MutableStateFlow(McpConnectionStatus.Stopped)
        val job = launch(Dispatchers.IO) {
            McpClient(settings(port), status, { _, _ -> "{}" }, retryDelays = listOf(50.milliseconds)).run()
        }
        try {
            withTimeout(5.seconds) { status.first { it == McpConnectionStatus.Connecting } }

            val connected = CompletableDeferred<Unit>()
            val server = embeddedServer(CIO, port = port, host = DEFAULT_HOST) {
                install(WebSockets)
                routing {
                    webSocket(APP_PATH) {
                        incoming.receive()
                        send(Frame.Text(HelloAck(ok = true).encode()))
                        connected.complete(Unit)
                        for (frame in incoming) Unit
                    }
                }
            }.start(wait = false)
            try {
                withTimeout(10.seconds) { connected.await() }
                assertTrue(status.first { it == McpConnectionStatus.Connected } == McpConnectionStatus.Connected)
            } finally {
                server.stop(100, 500)
            }
        } finally {
            job.cancel()
            job.join()
        }
    }
}
