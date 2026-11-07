package ro.cosminmihu.ktor.monitor.mcp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/*
 * Wire protocol between an app (WebSocket client) and the companion (WebSocket server).
 *
 * This source directory is shared by `library-mcp` and `companion` (it is not a published artifact),
 * so every declaration is internal to the module that includes it.
 *
 * Flow: the app connects to `ws://host:port/app` and sends [Hello]. The companion answers with
 * [HelloAck]. Afterwards the companion sends [Request]s and the app answers each with a [Response].
 */

internal const val PROTOCOL_VERSION = 1
internal const val DEFAULT_PORT = 8765
internal const val APP_PATH = "/app"

internal val ProtocolJson = Json {
    classDiscriminator = "type"
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
internal sealed interface Message

@Serializable
@SerialName("hello")
internal data class Hello(
    val protocolVersion: Int = PROTOCOL_VERSION,
    val appId: String,
    val appName: String,
    val platform: String,
    val token: String? = null,
) : Message

@Serializable
@SerialName("hello_ack")
internal data class HelloAck(
    val ok: Boolean,
    val error: String? = null,
) : Message

@Serializable
@SerialName("request")
internal data class Request(
    val id: Long,
    val method: String,
    val params: JsonObject,
) : Message

/** [result] is a JSON document serialized as text, [error] is set when the request failed. */
@Serializable
@SerialName("response")
internal data class Response(
    val id: Long,
    val result: String? = null,
    val error: String? = null,
) : Message

internal fun Message.encode(): String = ProtocolJson.encodeToString(Message.serializer(), this)

internal fun decodeMessage(text: String): Message = ProtocolJson.decodeFromString(Message.serializer(), text)
