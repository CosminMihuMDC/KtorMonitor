package ro.cosminmihu.ktor.monitor.mcp.companion

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** Minimal MCP server (tools only) speaking JSON-RPC 2.0, one JSON message per line. */
internal class McpServer(
    private val registry: AppRegistry,
    private val version: String,
) {

    /** Handles one incoming message. Returns the response line, or `null` for notifications. */
    suspend fun handle(line: String): String? {
        val message = try {
            Json.parseToJsonElement(line).jsonObject
        } catch (_: Exception) {
            return error(JsonNull, PARSE_ERROR, "Parse error").toString()
        }
        val method = message["method"]?.jsonPrimitive?.contentOrNull ?: return null
        val id = message["id"] ?: return null
        val params = message["params"] as? JsonObject ?: JsonObject(emptyMap())

        return try {
            val result = when (method) {
                "initialize" -> initialize(params)
                "ping" -> JsonObject(emptyMap())
                "tools/list" -> buildJsonObject { put("tools", tools) }
                "tools/call" -> callTool(params)
                else -> return error(id, METHOD_NOT_FOUND, "Method not found: $method").toString()
            }
            buildJsonObject {
                put("jsonrpc", "2.0")
                put("id", id)
                put("result", result)
            }.toString()
        } catch (e: IllegalArgumentException) {
            error(id, INVALID_PARAMS, e.message ?: "Invalid params").toString()
        }
    }

    private fun initialize(params: JsonObject): JsonElement {
        val requested = params["protocolVersion"]?.jsonPrimitive?.contentOrNull
        return buildJsonObject {
            put("protocolVersion", if (requested in SUPPORTED_VERSIONS) requested else SUPPORTED_VERSIONS.first())
            putJsonObject("capabilities") { putJsonObject("tools") {} }
            putJsonObject("serverInfo") {
                put("name", "ktor-monitor")
                put("version", version)
            }
            put(
                "instructions",
                "Query the HTTP calls captured by Ktor Monitor in the running app. " +
                    "Start with get_stats or list_calls, then use get_call for the details of one call.",
            )
        }
    }

    private suspend fun callTool(params: JsonObject): JsonElement {
        val name = params["name"]?.jsonPrimitive?.contentOrNull
            ?: throw IllegalArgumentException("Missing tool name")
        if (tools.none { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull == name }) {
            throw IllegalArgumentException("Unknown tool: $name")
        }
        val arguments = params["arguments"] as? JsonObject ?: JsonObject(emptyMap())

        return try {
            val text = if (name == "list_apps") {
                buildJsonArray {
                    registry.list().forEach {
                        add(buildJsonObject {
                            put("app_id", it.appId)
                            put("name", it.appName)
                            put("platform", it.platform)
                        })
                    }
                }.toString()
            } else {
                val appId = arguments["app_id"]?.jsonPrimitive?.contentOrNull
                registry.call(appId, name, JsonObject(arguments - "app_id"))
            }
            toolResult(text, isError = false)
        } catch (e: ToolException) {
            toolResult(e.message ?: "Tool failed", isError = true)
        }
    }

    private fun toolResult(text: String, isError: Boolean) = buildJsonObject {
        putJsonArray("content") {
            add(buildJsonObject {
                put("type", "text")
                put("text", text)
            })
        }
        put("isError", isError)
    }

    private fun error(id: JsonElement, code: Int, message: String) = buildJsonObject {
        put("jsonrpc", "2.0")
        put("id", id)
        putJsonObject("error") {
            put("code", code)
            put("message", message)
        }
    }

    private companion object {
        const val PARSE_ERROR = -32700
        const val METHOD_NOT_FOUND = -32601
        const val INVALID_PARAMS = -32602

        val SUPPORTED_VERSIONS = listOf("2025-06-18", "2025-03-26", "2024-11-05")

        val tools: JsonArray = buildJsonArray {
            add(tool("list_apps", "List the apps currently connected to the companion.", emptyMap()))
            add(
                tool(
                    "list_calls",
                    "List captured HTTP calls, newest first. Returns summaries without headers or bodies.",
                    mapOf(
                        "method" to prop("string", "HTTP method, e.g. GET."),
                        "url_contains" to prop("string", "Keep calls whose URL contains this text."),
                        "status_min" to prop("integer", "Minimum response status code."),
                        "status_max" to prop("integer", "Maximum response status code."),
                        "only_errors" to prop("boolean", "Only failed calls (exceptions or HTTP 4xx/5xx)."),
                        "limit" to prop("integer", "Page size, default 50, max 200."),
                        "offset" to prop("integer", "Start of the page, use next_offset from the previous page."),
                    ),
                )
            )
            add(
                tool(
                    "get_call",
                    "Get one call with headers and bodies (sensitive headers are already masked by the app). " +
                        "WebSocket and SSE calls include their events.",
                    mapOf(
                        "id" to prop("string", "Call id from list_calls or search_calls."),
                        "max_body_length" to prop("integer", "Maximum characters per body, default 32000."),
                    ),
                    required = listOf("id"),
                )
            )
            add(
                tool(
                    "search_calls",
                    "Search calls by text in the method, URL, status, headers and bodies. Returns summaries.",
                    mapOf(
                        "query" to prop("string", "Text to find, case-insensitive."),
                        "in_bodies" to prop("boolean", "Also search headers and bodies, default true."),
                        "limit" to prop("integer", "Page size, default 50, max 200."),
                        "offset" to prop("integer", "Start of the page, use next_offset from the previous page."),
                    ),
                    required = listOf("query"),
                )
            )
            add(
                tool(
                    "get_stats",
                    "Counts by status class, method and host, error count and latency percentiles of all captured calls.",
                    emptyMap(),
                )
            )
        }

        fun prop(type: String, description: String) = buildJsonObject {
            put("type", type)
            put("description", description)
        }

        fun tool(
            name: String,
            description: String,
            properties: Map<String, JsonObject>,
            required: List<String> = emptyList(),
        ) = buildJsonObject {
            put("name", name)
            put("description", description)
            putJsonObject("inputSchema") {
                put("type", "object")
                putJsonObject("properties") {
                    properties.forEach { (key, value) -> put(key, value) }
                    if (name != "list_apps") {
                        put("app_id", prop("string", "Target app, optional when only one app is connected."))
                    }
                }
                if (required.isNotEmpty()) put("required", JsonArray(required.map(::JsonPrimitive)))
            }
        }
    }
}
