package ro.cosminmihu.ktor.monitor.mcp.companion

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import ro.cosminmihu.ktor.monitor.mcp.protocol.AppInfo
import ro.cosminmihu.ktor.monitor.mcp.protocol.CallToolParams
import ro.cosminmihu.ktor.monitor.mcp.protocol.CallToolResult
import ro.cosminmihu.ktor.monitor.mcp.protocol.InitializeParams
import ro.cosminmihu.ktor.monitor.mcp.protocol.InitializeResult
import ro.cosminmihu.ktor.monitor.mcp.protocol.JsonRpcError
import ro.cosminmihu.ktor.monitor.mcp.protocol.JsonRpcRequest
import ro.cosminmihu.ktor.monitor.mcp.protocol.JsonRpcResponse
import ro.cosminmihu.ktor.monitor.mcp.protocol.ListToolsResult
import ro.cosminmihu.ktor.monitor.mcp.protocol.McpJson
import ro.cosminmihu.ktor.monitor.mcp.protocol.ServerInfo
import ro.cosminmihu.ktor.monitor.mcp.protocol.ToolAnnotations
import ro.cosminmihu.ktor.monitor.mcp.protocol.ToolDefinition
import ro.cosminmihu.ktor.monitor.mcp.protocol.ToolInputSchema
import ro.cosminmihu.ktor.monitor.mcp.protocol.ToolProperty

/** Minimal MCP server (tools only) speaking JSON-RPC 2.0, one JSON message per line. */
internal class McpServer(
    private val registry: AppRegistry,
    private val version: String,
) {

    /** Handles one incoming message. Returns the response line, or `null` for notifications. */
    suspend fun handle(line: String): String? {
        val request = try {
            McpJson.decodeFromString<JsonRpcRequest>(line)
        } catch (exception: SerializationException) {
            return error(JsonNull, PARSE_ERROR, "Parse error")
        } catch (exception: IllegalArgumentException) {
            return error(JsonNull, PARSE_ERROR, "Parse error")
        }
        val method = request.method ?: return null
        val id = request.id ?: return null
        val params = request.params ?: JsonObject(emptyMap())

        return try {
            val result = when (method) {
                "initialize" -> McpJson.encodeToJsonElement(initialize(McpJson.decodeFromJsonElement(params)))
                "ping" -> JsonObject(emptyMap())
                "tools/list" -> McpJson.encodeToJsonElement(ListToolsResult(tools))
                "tools/call" -> McpJson.encodeToJsonElement(callTool(McpJson.decodeFromJsonElement(params)))
                else -> return error(id, METHOD_NOT_FOUND, "Method not found: $method")
            }
            McpJson.encodeToString(JsonRpcResponse(id = id, result = result))
        } catch (exception: IllegalArgumentException) {
            error(id, INVALID_PARAMS, exception.message ?: "Invalid params")
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            log("Internal error handling '$method': $exception")
            error(id, INTERNAL_ERROR, "Internal error")
        }
    }

    private fun initialize(params: InitializeParams) = InitializeResult(
        protocolVersion = params.protocolVersion?.takeIf { it in SUPPORTED_VERSIONS } ?: SUPPORTED_VERSIONS.first(),
        serverInfo = ServerInfo(name = "ktor-monitor", version = version),
        instructions = "Query the HTTP calls captured by Ktor Monitor in the running app. " +
            "Start with get_stats or list_calls, then use get_call for the details of one call.",
    )

    private suspend fun callTool(params: CallToolParams): CallToolResult {
        val name = params.name ?: throw IllegalArgumentException("Missing tool name")
        if (tools.none { it.name == name }) {
            throw IllegalArgumentException("Unknown tool: $name")
        }
        val arguments = params.arguments ?: JsonObject(emptyMap())

        return try {
            val text = if (name == "list_apps") {
                val apps = registry.list().map { AppInfo(appId = it.appId, name = it.appName, platform = it.platform) }
                McpJson.encodeToString(ListSerializer(AppInfo.serializer()), apps)
            } else {
                val appId = arguments["app_id"]?.let {
                    (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content
                        ?: throw IllegalArgumentException("'app_id' must be a string")
                }
                registry.call(appId, name, JsonObject(arguments - "app_id"))
            }
            CallToolResult(text, isError = false)
        } catch (exception: ToolException) {
            CallToolResult(exception.message ?: "Tool failed", isError = true)
        }
    }

    private fun error(id: JsonElement, code: Int, message: String): String =
        McpJson.encodeToString(JsonRpcResponse(id = id, error = JsonRpcError(code, message)))

    private companion object {
        const val PARSE_ERROR = -32700
        const val METHOD_NOT_FOUND = -32601
        const val INVALID_PARAMS = -32602
        const val INTERNAL_ERROR = -32603

        val SUPPORTED_VERSIONS = listOf("2025-06-18", "2025-03-26", "2024-11-05")

        val titles = mapOf(
            "list_apps" to "List connected apps",
            "list_calls" to "List HTTP calls",
            "get_call" to "Get HTTP call",
            "search_calls" to "Search HTTP calls",
            "get_stats" to "HTTP calls statistics",
        )

        val tools: List<ToolDefinition> = listOf(
            tool("list_apps", "List the apps currently connected to the companion."),
            tool(
                "list_calls",
                "List captured HTTP calls, newest first. Returns summaries without headers or bodies.",
                mapOf(
                    "method" to ToolProperty("string", "HTTP method, e.g. GET."),
                    "url_contains" to ToolProperty("string", "Keep calls whose URL contains this text."),
                    "status_min" to ToolProperty("integer", "Minimum response status code."),
                    "status_max" to ToolProperty("integer", "Maximum response status code."),
                    "only_errors" to ToolProperty("boolean", "Only failed calls (exceptions or HTTP 4xx/5xx)."),
                    "limit" to ToolProperty("integer", "Page size, default 50, max 200."),
                    "offset" to ToolProperty("integer", "Start of the page, use next_offset from the previous page."),
                ),
            ),
            tool(
                "get_call",
                "Get one call with headers and bodies (sensitive headers are already masked by the app). " +
                    "WebSocket and SSE calls include their events.",
                mapOf(
                    "id" to ToolProperty("string", "Call id from list_calls or search_calls."),
                    "max_body_length" to ToolProperty("integer", "Maximum characters per body, default 32000."),
                ),
                required = listOf("id"),
            ),
            tool(
                "search_calls",
                "Search calls by text in the method, URL, status, headers and bodies. Returns summaries.",
                mapOf(
                    "query" to ToolProperty("string", "Text to find, case-insensitive."),
                    "in_bodies" to ToolProperty("boolean", "Also search headers and bodies, default true."),
                    "limit" to ToolProperty("integer", "Page size, default 50, max 200."),
                    "offset" to ToolProperty("integer", "Start of the page, use next_offset from the previous page."),
                ),
                required = listOf("query"),
            ),
            tool(
                "get_stats",
                "Counts by status class, method and host, error count and latency percentiles of all captured calls.",
            ),
        )

        fun tool(
            name: String,
            description: String,
            properties: Map<String, ToolProperty> = emptyMap(),
            required: List<String>? = null,
        ): ToolDefinition {
            val appIdProperty = ToolProperty("string", "Target app, optional when only one app is connected.")
            return ToolDefinition(
                name = name,
                description = description,
                inputSchema = ToolInputSchema(
                    properties = if (name == "list_apps") properties else properties + ("app_id" to appIdProperty),
                    required = required,
                ),
                annotations = ToolAnnotations(title = titles[name]),
            )
        }
    }
}
