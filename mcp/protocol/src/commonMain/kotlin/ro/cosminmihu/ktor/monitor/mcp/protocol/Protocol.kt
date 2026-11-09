package ro.cosminmihu.ktor.monitor.mcp.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/*
 * Wire protocol between an app (WebSocket client) and the companion (WebSocket server).
 *
 * This source folder (`mcp/protocol`) is not a published module: it is compiled directly into both
 * `library-mcp` and `companion` through `kotlin.srcDir`, so every declaration is internal to each.
 *
 * Flow: the app connects to `ws://host:port/app` and sends [Hello]. The companion answers with a
 * [Challenge], the app with an [Auth] (see ProtocolAuth.kt) and the companion closes the handshake with
 * a [HelloAck]. Afterwards the companion sends [Request]s and the app answers each with a [Response].
 */

internal const val PROTOCOL_VERSION: Int = 2
internal const val APP_PATH: String = "/app"

internal val ProtocolJson: Json = Json {
    classDiscriminator = "type"
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
internal sealed interface Message

@Serializable
@SerialName("hello")
internal data class Hello(
    @SerialName("protocol_version") val protocolVersion: Int = PROTOCOL_VERSION,
    @SerialName("app_id") val appId: String,
    @SerialName("app_name") val appName: String,
    @SerialName("platform") val platform: String,
    @SerialName("client_nonce") val clientNonce: String = "",
) : Message

@Serializable
@SerialName("challenge")
internal data class Challenge(
    @SerialName("server_nonce") val serverNonce: String,
    @SerialName("proof") val proof: String? = null,
) : Message

@Serializable
@SerialName("auth")
internal data class Auth(
    @SerialName("proof") val proof: String? = null,
) : Message

@Serializable
@SerialName("hello_ack")
internal data class HelloAck(
    @SerialName("ok") val ok: Boolean,
    @SerialName("error") val error: String? = null,
) : Message

@Serializable
@SerialName("request")
internal data class Request(
    @SerialName("id") val id: Long,
    @SerialName("method") val method: String,
    @SerialName("params") val params: JsonObject,
) : Message

/** Sent by the companion when it gave up waiting for the answer to request [id]. */
@Serializable
@SerialName("cancel")
internal data class Cancel(
    @SerialName("id") val id: Long,
) : Message

/** [result] is a JSON document serialized as text, [error] is set when the request failed. */
@Serializable
@SerialName("response")
internal data class Response(
    @SerialName("id") val id: Long,
    @SerialName("result") val result: String? = null,
    @SerialName("error") val error: String? = null,
) : Message

internal fun Message.encode(): String = ProtocolJson.encodeToString(Message.serializer(), this)

internal fun decodeMessage(text: String): Message = ProtocolJson.decodeFromString(Message.serializer(), text)

/** Returns `null` when [text] is not a valid protocol message. */
internal fun decodeMessageOrNull(text: String): Message? = try {
    decodeMessage(text)
} catch (exception: SerializationException) {
    null
} catch (exception: IllegalArgumentException) {
    null
}
