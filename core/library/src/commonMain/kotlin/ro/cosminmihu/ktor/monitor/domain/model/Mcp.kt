package ro.cosminmihu.ktor.monitor.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Models of the MCP queries exposed through the bridge. */
internal object Mcp {

    /** State of the MCP connection, shown in the list header. `null` when MCP is not started. */
    enum class Status {
        Connecting,
        Connected,
    }

    /** Parameters of the MCP `list_calls` method. */
    @Serializable
    data class ListCallsParams(
        @SerialName("limit") val limit: Int? = null,
        @SerialName("offset") val offset: Int? = null,
        @SerialName("method") val method: String? = null,
        @SerialName("url_contains") val urlContains: String? = null,
        @SerialName("status_min") val statusMin: Int? = null,
        @SerialName("status_max") val statusMax: Int? = null,
        @SerialName("only_errors") val onlyErrors: Boolean? = null,
    )

    /** Parameters of the MCP `search_calls` method. */
    @Serializable
    data class SearchCallsParams(
        @SerialName("query") val query: String? = null,
        @SerialName("in_bodies") val inBodies: Boolean? = null,
        @SerialName("limit") val limit: Int? = null,
        @SerialName("offset") val offset: Int? = null,
    )

    /** Parameters of the MCP `get_call` method. */
    @Serializable
    data class GetCallParams(
        @SerialName("id") val id: String? = null,
        @SerialName("max_body_length") val maxBodyLength: Int? = null,
    )

    @Serializable
    data class CallPage(
        @SerialName("total") val total: Int,
        @SerialName("offset") val offset: Int,
        @SerialName("limit") val limit: Int,
        @SerialName("next_offset") val nextOffset: Int? = null,
        @SerialName("calls") val calls: List<CallSummary>,
    )

    @Serializable
    data class CallSummary(
        @SerialName("id") val id: String,
        @SerialName("method") val method: String,
        @SerialName("url") val url: String,
        @SerialName("request_timestamp") val requestTimestamp: Long,
        @SerialName("status") val status: Long? = null,
        @SerialName("duration_ms") val durationMs: Long? = null,
        @SerialName("content_type") val contentType: String? = null,
        @SerialName("response_content_length") val responseContentLength: Long? = null,
        @SerialName("error") val error: String? = null,
        @SerialName("stream_kind") val streamKind: String? = null,
    )

    @Serializable
    data class CallDetail(
        @SerialName("id") val id: String,
        @SerialName("method") val method: String,
        @SerialName("url") val url: String,
        @SerialName("protocol") val protocol: String? = null,
        @SerialName("request_timestamp") val requestTimestamp: Long,
        @SerialName("response_timestamp") val responseTimestamp: Long? = null,
        @SerialName("duration_ms") val durationMs: Long? = null,
        @SerialName("status") val status: Long? = null,
        @SerialName("error") val error: String? = null,
        @SerialName("request") val request: Message,
        @SerialName("response") val response: Message,
        @SerialName("stream") val stream: Stream? = null,
    )

    @Serializable
    data class Message(
        @SerialName("headers") val headers: Map<String, List<String>>? = null,
        @SerialName("content_type") val contentType: String? = null,
        @SerialName("content_length") val contentLength: Long? = null,
        @SerialName("body") val body: String? = null,
        @SerialName("body_truncated") val bodyTruncated: Boolean = false,
    )

    @Serializable
    data class Stream(
        @SerialName("kind") val kind: String? = null,
        @SerialName("status") val status: String? = null,
        @SerialName("close_code") val closeCode: Int? = null,
        @SerialName("close_reason") val closeReason: String? = null,
        @SerialName("error") val error: String? = null,
        @SerialName("events") val events: List<StreamEvent>,
        @SerialName("events_truncated") val eventsTruncated: Boolean,
    )

    @Serializable
    data class StreamEvent(
        @SerialName("timestamp") val timestamp: Long,
        @SerialName("direction") val direction: String,
        @SerialName("type") val type: String,
        @SerialName("name") val name: String? = null,
        @SerialName("size") val size: Long? = null,
        @SerialName("payload") val payload: String? = null,
        @SerialName("truncated") val truncated: Boolean? = null,
    )

    @Serializable
    data class Stats(
        @SerialName("total") val total: Int,
        @SerialName("in_progress") val inProgress: Int,
        @SerialName("errors") val errors: Int,
        @SerialName("by_status_class") val byStatusClass: Map<String, Int>,
        @SerialName("by_method") val byMethod: Map<String, Int>,
        @SerialName("by_host") val byHost: Map<String, Int>,
        @SerialName("latency_ms") val latencyMs: Latency,
        @SerialName("oldest_request_timestamp") val oldestRequestTimestamp: Long? = null,
        @SerialName("newest_request_timestamp") val newestRequestTimestamp: Long? = null,
    )

    @Serializable
    data class Latency(
        @SerialName("avg") val avg: Double? = null,
        @SerialName("p50") val p50: Long? = null,
        @SerialName("p95") val p95: Long? = null,
        @SerialName("max") val max: Long? = null,
    )
}
