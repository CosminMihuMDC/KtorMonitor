package ro.cosminmihu.ktor.monitor.mcp.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/*
 * JSON-RPC 2.0 / Model Context Protocol messages exchanged between the AI client and the companion
 * over stdin/stdout. Only the subset used by the companion (tools) is modelled.
 */

internal val McpJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

/** Exactly one of [result] and [error] is set. */
@Serializable
internal data class JsonRpcResponse(
    @SerialName("jsonrpc") val jsonrpc: String = "2.0",
    @SerialName("id") val id: JsonElement,
    @SerialName("result") val result: JsonElement? = null,
    @SerialName("error") val error: JsonRpcError? = null,
)

@Serializable
internal data class JsonRpcError(
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
)

@Serializable
internal data class InitializeParams(
    @SerialName("protocolVersion") val protocolVersion: String? = null,
)

@Serializable
internal data class InitializeResult(
    @SerialName("protocolVersion") val protocolVersion: String,
    @SerialName("capabilities") val capabilities: ServerCapabilities = ServerCapabilities(),
    @SerialName("serverInfo") val serverInfo: ServerInfo,
    @SerialName("instructions") val instructions: String,
)

@Serializable
internal data class ServerCapabilities(
    @SerialName("tools") val tools: ToolsCapability = ToolsCapability(),
)

@Serializable
internal class ToolsCapability

@Serializable
internal data class ServerInfo(
    @SerialName("name") val name: String,
    @SerialName("version") val version: String,
)

@Serializable
internal data class ListToolsResult(
    @SerialName("tools") val tools: List<ToolDefinition>,
)

@Serializable
internal data class ToolDefinition(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("inputSchema") val inputSchema: ToolInputSchema,
    @SerialName("annotations") val annotations: ToolAnnotations? = null,
)

/** Hints that let AI clients run the tool without asking for confirmation. */
@Serializable
internal data class ToolAnnotations(
    @SerialName("title") val title: String? = null,
    @SerialName("readOnlyHint") val readOnlyHint: Boolean = true,
    @SerialName("destructiveHint") val destructiveHint: Boolean = false,
    @SerialName("idempotentHint") val idempotentHint: Boolean = true,
    @SerialName("openWorldHint") val openWorldHint: Boolean = false,
)

@Serializable
internal data class ToolInputSchema(
    @SerialName("type") val type: String = "object",
    @SerialName("properties") val properties: Map<String, ToolProperty>,
    @SerialName("required") val required: List<String>? = null,
    @SerialName("additionalProperties") val additionalProperties: Boolean = false,
)

@Serializable
internal data class ToolProperty(
    @SerialName("type") val type: String,
    @SerialName("description") val description: String,
    @SerialName("minimum") val minimum: Long? = null,
    @SerialName("maximum") val maximum: Long? = null,
)

@Serializable
internal data class CallToolParams(
    @SerialName("name") val name: String? = null,
    @SerialName("arguments") val arguments: JsonObject? = null,
)

@Serializable
internal data class CallToolResult(
    @SerialName("content") val content: List<TextContent>,
    @SerialName("isError") val isError: Boolean,
) {
    constructor(text: String, isError: Boolean) : this(listOf(TextContent(text = text)), isError)
}

@Serializable
internal data class TextContent(
    @SerialName("type") val type: String = "text",
    @SerialName("text") val text: String,
)

/** An entry of the `list_apps` tool result. */
@Serializable
internal data class AppInfo(
    @SerialName("app_id") val appId: String,
    @SerialName("name") val name: String,
    @SerialName("platform") val platform: String,
)
