package ro.cosminmihu.ktor.monitor.mcp.companion

import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.URLParserException
import io.ktor.server.application.install
import io.ktor.server.request.ApplicationRequest
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
import ro.cosminmihu.ktor.monitor.mcp.protocol.Auth
import ro.cosminmihu.ktor.monitor.mcp.protocol.Challenge
import ro.cosminmihu.ktor.monitor.mcp.protocol.Hello
import ro.cosminmihu.ktor.monitor.mcp.protocol.appProof
import ro.cosminmihu.ktor.monitor.mcp.protocol.companionProof
import ro.cosminmihu.ktor.monitor.mcp.protocol.constantTimeEquals
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_HOST
import ro.cosminmihu.ktor.monitor.mcp.protocol.HelloAck
import ro.cosminmihu.ktor.monitor.mcp.protocol.PROTOCOL_VERSION
import ro.cosminmihu.ktor.monitor.mcp.protocol.Response
import ro.cosminmihu.ktor.monitor.mcp.protocol.decodeMessageOrNull
import ro.cosminmihu.ktor.monitor.mcp.protocol.encode
import java.security.SecureRandom
import kotlin.time.Duration.Companion.seconds

/** Listens on loopback only, so apps on other machines cannot connect. */
internal fun startWebSocketServer(
    registry: AppRegistry,
    port: Int,
    token: String?,
): EmbeddedServer<*, *> = embeddedServer(CIO, port = port, host = DEFAULT_HOST) {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 30.seconds
    }
    routing {
        webSocket(APP_PATH) { handleApp(registry, token) }
    }
}.start(wait = false)

private suspend fun DefaultWebSocketServerSession.handleApp(registry: AppRegistry, expectedToken: String?) {
    if (expectedToken == null && !call.request.hasLocalOrigin()) {
        // Without a token, a web page opened in the developer browser could otherwise pose as an app.
        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Forbidden origin"))
        return
    }
    val hello = withTimeoutOrNull(10.seconds) { readHello() }
    if (hello == null) {
        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Expected hello"))
        return
    }
    if (hello.protocolVersion != PROTOCOL_VERSION) {
        reject(
            "Unsupported protocol version ${hello.protocolVersion}, the companion supports $PROTOCOL_VERSION. " +
                "Use the same version for the library and the companion."
        )
        return
    }
    val serverNonce = secureNonce()
    send(
        Frame.Text(
            Challenge(
                serverNonce = serverNonce,
                proof = expectedToken?.let { companionProof(it, hello.clientNonce, serverNonce) },
            ).encode()
        )
    )
    val auth = withTimeoutOrNull(10.seconds) { readAuth() }
    if (auth == null) {
        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Expected auth"))
        return
    }
    if (expectedToken != null &&
        !constantTimeEquals(auth.proof, appProof(expectedToken, hello.clientNonce, serverNonce))
    ) {
        reject("Invalid token.")
        return
    }
    send(Frame.Text(HelloAck(ok = true).encode()))

    val connection = AppConnection(hello) { send(Frame.Text(it)) }
    registry.register(connection)
    log("App connected: ${hello.appName} (${hello.platform}, ${hello.appId})")
    try {
        for (frame in incoming) {
            val text = (frame as? Frame.Text)?.readText() ?: continue
            val message = decodeMessageOrNull(text)
            if (message is Response) connection.complete(message)
        }
    } finally {
        registry.unregister(connection)
        connection.disconnect()
        log("App disconnected: ${hello.appName} (${hello.appId})")
    }
}

/** Native clients send no `Origin`; browsers must come from a loopback page. */
private fun ApplicationRequest.hasLocalOrigin(): Boolean {
    val origin = headers[HttpHeaders.Origin] ?: return true
    val host = try {
        Url(origin).host
    } catch (_: URLParserException) {
        return false
    }
    return host == "localhost" || host == DEFAULT_HOST || host == "[::1]" || host == "::1"
}

private suspend fun DefaultWebSocketServerSession.reject(error: String) {
    send(Frame.Text(HelloAck(ok = false, error = error).encode()))
    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Rejected"))
}

private fun secureNonce(): String = ByteArray(16).also { SecureRandom().nextBytes(it) }
    .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

private suspend fun DefaultWebSocketServerSession.readHello(): Hello? {
    for (frame in incoming) {
        val text = (frame as? Frame.Text)?.readText() ?: continue
        return decodeMessageOrNull(text) as? Hello
    }
    return null
}

private suspend fun DefaultWebSocketServerSession.readAuth(): Auth? {
    for (frame in incoming) {
        val text = (frame as? Frame.Text)?.readText() ?: continue
        return decodeMessageOrNull(text) as? Auth
    }
    return null
}
