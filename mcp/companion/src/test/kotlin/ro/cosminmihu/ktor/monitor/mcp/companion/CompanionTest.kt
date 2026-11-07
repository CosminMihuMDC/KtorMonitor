package ro.cosminmihu.ktor.monitor.mcp.companion

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ro.cosminmihu.ktor.monitor.mcp.APP_PATH
import ro.cosminmihu.ktor.monitor.mcp.Hello
import ro.cosminmihu.ktor.monitor.mcp.HelloAck
import ro.cosminmihu.ktor.monitor.mcp.Request
import ro.cosminmihu.ktor.monitor.mcp.Response
import ro.cosminmihu.ktor.monitor.mcp.decodeMessage
import ro.cosminmihu.ktor.monitor.mcp.encode
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CompanionTest {

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
                client.webSocket(host = "127.0.0.1", port = port, path = APP_PATH) {
                    send(Frame.Text(Hello(appId = "a1", appName = "Sample", platform = "Test", token = "secret").encode()))
                    assertTrue((decodeMessage((incoming.receive() as Frame.Text).readText()) as HelloAck).ok)
                    for (frame in incoming) {
                        val request = decodeMessage((frame as Frame.Text).readText()) as Request
                        send(Frame.Text(Response(request.id, result = """{"echo":"${request.method}","limit":${request.params["limit"]}}""").encode()))
                    }
                }
            }
            while (registry.list().isEmpty()) delay(20)

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
            client.webSocket(host = "127.0.0.1", port = port, path = APP_PATH) {
                send(Frame.Text(Hello(appId = "a1", appName = "Sample", platform = "Test", token = "wrong").encode()))
                ack = decodeMessage((incoming.receive() as Frame.Text).readText()) as HelloAck
            }
            assertFalse(ack!!.ok)
            assertTrue(registry.list().isEmpty())
        } finally {
            client.close()
            server.stop(100, 500)
        }
    }
}
