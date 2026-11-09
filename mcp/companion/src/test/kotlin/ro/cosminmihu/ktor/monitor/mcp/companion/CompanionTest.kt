package ro.cosminmihu.ktor.monitor.mcp.companion

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_HOST
import ro.cosminmihu.ktor.monitor.mcp.protocol.APP_PATH
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import ro.cosminmihu.ktor.monitor.mcp.protocol.Auth
import ro.cosminmihu.ktor.monitor.mcp.protocol.Challenge
import ro.cosminmihu.ktor.monitor.mcp.protocol.Hello
import ro.cosminmihu.ktor.monitor.mcp.protocol.appProof
import ro.cosminmihu.ktor.monitor.mcp.protocol.companionProof
import ro.cosminmihu.ktor.monitor.mcp.protocol.hmacSha256
import ro.cosminmihu.ktor.monitor.mcp.protocol.newNonce
import ro.cosminmihu.ktor.monitor.mcp.protocol.HelloAck
import ro.cosminmihu.ktor.monitor.mcp.protocol.Request
import ro.cosminmihu.ktor.monitor.mcp.protocol.Response
import ro.cosminmihu.ktor.monitor.mcp.protocol.decodeMessage
import ro.cosminmihu.ktor.monitor.mcp.protocol.encode
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class CompanionTest {

    /** Plays the app side of the authenticated handshake. */
    private suspend fun DefaultClientWebSocketSession.handshake(token: String?, verifyCompanion: Boolean = true): HelloAck {
        val nonce = newNonce()
        send(Frame.Text(Hello(appId = "a1", appName = "Sample", platform = "Test", clientNonce = nonce).encode()))
        val challenge = decodeMessage((incoming.receive() as Frame.Text).readText()) as Challenge
        if (token != null && verifyCompanion) {
            assertEquals(companionProof(token, nonce, challenge.serverNonce), challenge.proof)
        }
        send(Frame.Text(Auth(token?.let { appProof(it, nonce, challenge.serverNonce) }).encode()))
        return decodeMessage((incoming.receive() as Frame.Text).readText()) as HelloAck
    }

    private fun freePort() = ServerSocket(0).use { it.localPort }

    private fun toolCall(name: String, arguments: String = "{}") =
        """{"jsonrpc":"2.0","id":7,"method":"tools/call","params":{"name":"$name","arguments":$arguments}}"""

    private fun JsonObject.toolText() =
        this["result"]!!.jsonObject["content"]!!.jsonArray[0].jsonObject["text"]!!.jsonPrimitive.content

    private fun JsonObject.isToolError() = this["result"]!!.jsonObject["isError"]!!.jsonPrimitive.boolean

    @Test
    fun initializeListsToolsAndIgnoresNotifications() = runBlocking {
        val mcp = McpServer(AppRegistry(), "test")

        val init = Json.parseToJsonElement(
            mcp.handle("""{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05"}}""")!!
        ).jsonObject
        assertEquals("2024-11-05", init["result"]!!.jsonObject["protocolVersion"]!!.jsonPrimitive.content)

        assertNull(mcp.handle("""{"jsonrpc":"2.0","method":"notifications/initialized"}"""))

        val tools = Json.parseToJsonElement(mcp.handle("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}""")!!).jsonObject
        val names = tools["result"]!!.jsonObject["tools"]!!.jsonArray.map { it.jsonObject["name"]!!.jsonPrimitive.content }
        assertEquals(listOf("list_apps", "list_calls", "get_call", "search_calls", "get_stats"), names)
        assertTrue(tools["result"]!!.jsonObject["tools"]!!.jsonArray.all {
            it.jsonObject["annotations"]!!.jsonObject["readOnlyHint"]!!.jsonPrimitive.boolean
        })
    }

    @Test
    fun invalidJsonReturnsParseError() = runBlocking {
        val mcp = McpServer(AppRegistry(), "test")
        val response = Json.parseToJsonElement(mcp.handle("{not json")!!).jsonObject
        assertEquals(-32700, response["error"]!!.jsonObject["code"]!!.jsonPrimitive.int)
    }

    @Test
    fun unknownToolReturnsInvalidParams() = runBlocking {
        val mcp = McpServer(AppRegistry(), "test")
        val response = Json.parseToJsonElement(mcp.handle(toolCall("nope"))!!).jsonObject
        assertEquals(-32602, response["error"]!!.jsonObject["code"]!!.jsonPrimitive.int)
    }

    @Test
    fun toolCallWithoutAppReturnsToolError() = runBlocking {
        val mcp = McpServer(AppRegistry(), "test")
        val response = Json.parseToJsonElement(mcp.handle(toolCall("get_stats"))!!).jsonObject
        assertTrue(response.isToolError())
        assertTrue(response.toolText().contains("No app is connected"))
    }

    @Test
    fun toolCallIsRoutedToTheConnectedApp() = runBlocking {
        val port = freePort()
        val registry = AppRegistry()
        val server = startWebSocketServer(registry, port, token = "secret")
        val client = HttpClient(CIO) { install(WebSockets) }
        try {
            val appJob = launch(kotlinx.coroutines.Dispatchers.IO) {
                client.webSocket(host = DEFAULT_HOST, port = port, path = APP_PATH) {
                    assertTrue(handshake("secret").ok)
                    for (frame in incoming) {
                        val request = decodeMessage((frame as Frame.Text).readText()) as Request
                        send(Frame.Text(Response(request.id, result = """{"echo":"${request.method}","limit":${request.params["limit"]}}""").encode()))
                    }
                }
            }
            while (registry.list().isEmpty()) delay(20.milliseconds)

            val mcp = McpServer(registry, "test")
            val apps = Json.parseToJsonElement(mcp.handle(toolCall("list_apps"))!!).jsonObject
            assertTrue(apps.toolText().contains("\"app_id\":\"a1\""))

            val response = Json.parseToJsonElement(mcp.handle(toolCall("list_calls", """{"limit":5,"app_id":"a1"}"""))!!).jsonObject
            assertFalse(response.isToolError())
            assertEquals("""{"echo":"list_calls","limit":5}""", response.toolText())

            appJob.cancel()
        } finally {
            client.close()
            server.stop(100, 500)
        }
    }

    @Test
    fun wrongTokenIsRejected() = runBlocking {
        val port = freePort()
        val registry = AppRegistry()
        val server = startWebSocketServer(registry, port, token = "secret")
        val client = HttpClient(CIO) { install(WebSockets) }
        try {
            var ack: HelloAck? = null
            client.webSocket(host = DEFAULT_HOST, port = port, path = APP_PATH) {
                ack = handshake("wrong", verifyCompanion = false)
            }
            assertFalse(ack!!.ok)
            assertTrue(registry.list().isEmpty())
        } finally {
            client.close()
            server.stop(100, 500)
        }
    }

    @Test
    fun browserOriginIsRejectedWithoutTokenButLocalOriginIsAccepted() = runBlocking {
        val port = freePort()
        val registry = AppRegistry()
        val server = startWebSocketServer(registry, port, token = null)
        val client = HttpClient(CIO) { install(WebSockets) }
        try {
            suspend fun handshake(origin: String): Boolean {
                var accepted = false
                client.webSocket(
                    host = DEFAULT_HOST,
                    port = port,
                    path = APP_PATH,
                    request = { header(HttpHeaders.Origin, origin) },
                ) {
                    accepted = runCatching { handshake(null).ok }.getOrDefault(false)
                }
                return accepted
            }
            assertFalse(handshake("https://evil.example.com"))
            assertTrue(handshake("http://localhost:8080"))
        } finally {
            client.close()
            server.stop(100, 500)
        }
    }

    @Test
    fun hmacMatchesTheJdkImplementation() {
        for (size in listOf(0, 1, 55, 56, 64, 65, 200)) {
            val key = ByteArray(size) { (it * 7).toByte() }
            val message = ByteArray(size * 3) { (it * 13).toByte() }
            val mac = javax.crypto.Mac.getInstance("HmacSHA256")
            mac.init(javax.crypto.spec.SecretKeySpec(key.copyOf(maxOf(size, 1)), "HmacSHA256"))
            if (size > 0) assertTrue(mac.doFinal(message).contentEquals(hmacSha256(key, message)), "size $size")
        }
    }

    @Test
    fun jsonRpcEdgeCasesReturnProperErrors() = runBlocking {
        val mcp = McpServer(AppRegistry(), "test")
        suspend fun code(line: String) = Json.parseToJsonElement(mcp.handle(line)!!).jsonObject["error"]!!
            .jsonObject["code"]!!.jsonPrimitive.int

        assertEquals(-32600, code("""[{"jsonrpc":"2.0","id":1,"method":"ping"}]"""))
        assertEquals(-32600, code("""{"jsonrpc":"2.0","id":{"a":1},"method":"ping"}"""))
        assertEquals(-32600, code("""{"jsonrpc":"2.0","id":1,"method":5}"""))
        assertEquals(-32602, code("""{"jsonrpc":"2.0","id":1,"method":"tools/list","params":[1]}"""))
        assertEquals(-32602, code("""{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":5}}"""))
        assertNull(mcp.handle("""{"jsonrpc":"2.0","id":1,"result":{}}"""))
    }

    @Test
    fun toolArgumentsAreValidatedBeforeReachingTheApp() = runBlocking {
        val mcp = McpServer(AppRegistry(), "test")
        suspend fun text(name: String, args: String): String {
            val response = Json.parseToJsonElement(mcp.handle(toolCall(name, args))!!).jsonObject
            assertTrue(response.isToolError(), "$name $args")
            return response.toolText()
        }
        assertTrue(text("get_stats", """{"app_id":5}""").contains("'app_id' must be a string"))
        assertTrue(text("list_calls", """{"limit":"abc"}""").contains("'limit' must be an integer"))
        assertTrue(text("list_calls", """{"limit":500}""").contains("between 1 and 200"))
        assertTrue(text("list_calls", """{"only_errors":"yes"}""").contains("must be a boolean"))
        assertTrue(text("list_calls", """{"bogus":1}""").contains("Unknown argument 'bogus'"))
        assertTrue(text("get_call", "{}").contains("'id' is required"))
    }
}
