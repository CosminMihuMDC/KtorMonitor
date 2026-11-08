package ro.cosminmihu.ktor.monitor.mcp.companion

import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.withTimeoutOrNull
import ro.cosminmihu.ktor.monitor.mcp.protocol.APP_PATH
import ro.cosminmihu.ktor.monitor.mcp.protocol.Hello
import ro.cosminmihu.ktor.monitor.mcp.protocol.HelloAck
import ro.cosminmihu.ktor.monitor.mcp.protocol.PROTOCOL_VERSION
import ro.cosminmihu.ktor.monitor.mcp.protocol.Response
import ro.cosminmihu.ktor.monitor.mcp.protocol.decodeMessage
import ro.cosminmihu.ktor.monitor.mcp.protocol.encode
import kotlin.time.Duration.Companion.seconds

/** Listens on loopback only, so apps on other machines cannot connect. */
internal fun startWebSocketServer(
    registry: AppRegistry,
    port: Int,
    token: String?,
): EmbeddedServer<*, *> = embeddedServer(CIO, port = port, host = "127.0.0.1") {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 30.seconds
    }
    routing {
        webSocket(APP_PATH) { handleApp(registry, token) }
    }
}.start(wait = false)

private suspend fun DefaultWebSocketServerSession.handleApp(registry: AppRegistry, expectedToken: String?) {
    val hello = withTimeoutOrNull(10.seconds) { readHello() }
    if (hello == null) {
        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Expected hello"))
        return
    }
    val rejection = when {
        hello.protocolVersion != PROTOCOL_VERSION ->
            "Unsupported protocol version ${hello.protocolVersion}, the companion supports $PROTOCOL_VERSION. " +
                "Use the same version for the library and the companion."

        expectedToken != null && hello.token != expectedToken -> "Invalid token."
        else -> null
    }
    if (rejection != null) {
        send(Frame.Text(HelloAck(ok = false, error = rejection).encode()))
        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Rejected"))
        return
    }
    send(Frame.Text(HelloAck(ok = true).encode()))

    val connection = AppConnection(hello) { send(Frame.Text(it)) }
    registry.register(connection)
    log("App connected: ${hello.appName} (${hello.platform}, ${hello.appId})")
    try {
        for (frame in incoming) {
            val text = (frame as? Frame.Text)?.readText() ?: continue
            val message = runCatching { decodeMessage(text) }.getOrNull()
            if (message is Response) connection.complete(message)
        }
    } finally {
        registry.unregister(connection)
        connection.disconnect()
        log("App disconnected: ${hello.appName} (${hello.appId})")
    }
}

private suspend fun DefaultWebSocketServerSession.readHello(): Hello? {
    for (frame in incoming) {
        val text = (frame as? Frame.Text)?.readText() ?: continue
        return runCatching { decodeMessage(text) }.getOrNull() as? Hello
    }
    return null
}
