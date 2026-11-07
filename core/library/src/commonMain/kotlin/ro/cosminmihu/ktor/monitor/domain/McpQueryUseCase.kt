package ro.cosminmihu.ktor.monitor.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ro.cosminmihu.ktor.monitor.db.Call
import ro.cosminmihu.ktor.monitor.db.LibraryDao
import ro.cosminmihu.ktor.monitor.db.SelectCalls
import ro.cosminmihu.ktor.monitor.db.StreamEvent
import ro.cosminmihu.ktor.monitor.db.StreamEventType
import ro.cosminmihu.ktor.monitor.domain.model.Mcp
import ro.cosminmihu.ktor.monitor.domain.model.decodeBody
import ro.cosminmihu.ktor.monitor.domain.model.isError

/**
 * Read-only queries over the stored calls, used by the MCP bridge.
 *
 * Headers are already sanitized when stored, so nothing here can reveal a sanitized value.
 */
internal class McpQueryUseCase(
    private val dao: LibraryDao,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Runs [method] with JSON [paramsJson] and returns the JSON result. */
    suspend operator fun invoke(method: String, paramsJson: String): String =
        withContext(Dispatchers.Default) {
            when (method) {
                METHOD_LIST_CALLS -> json.encodeToString(listCalls(json.decodeFromString(paramsJson)))
                METHOD_GET_CALL -> json.encodeToString(getCall(json.decodeFromString(paramsJson)))
                METHOD_SEARCH_CALLS -> json.encodeToString(searchCalls(json.decodeFromString(paramsJson)))
                METHOD_GET_STATS -> json.encodeToString(getStats())
                else -> throw IllegalArgumentException("Unknown method: $method")
            }
        }

    private suspend fun listCalls(params: Mcp.ListCallsParams): Mcp.CallPage {
        val limit = (params.limit ?: DEFAULT_LIMIT).coerceIn(1, MAX_LIMIT)
        val offset = (params.offset ?: 0).coerceAtLeast(0)

        val matching = dao.getCalls().first().filter { call ->
            (params.method == null || call.method.equals(params.method, ignoreCase = true)) &&
                (params.urlContains == null || call.url.contains(params.urlContains, ignoreCase = true)) &&
                (params.statusMin == null || (call.responseCode ?: -1) >= params.statusMin) &&
                (params.statusMax == null || (call.responseCode ?: Long.MAX_VALUE) <= params.statusMax) &&
                (params.onlyErrors != true || call.isError)
        }
        return page(matching, offset, limit)
    }

    private suspend fun searchCalls(params: Mcp.SearchCallsParams): Mcp.CallPage {
        val query = params.query?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("'query' is required")
        val inBodies = params.inBodies != false
        val limit = (params.limit ?: DEFAULT_LIMIT).coerceIn(1, MAX_LIMIT)
        val offset = (params.offset ?: 0).coerceAtLeast(0)

        val calls = dao.getCalls().first()
        val matches = mutableListOf<SelectCalls>()
        val needBodyCheck = mutableListOf<SelectCalls>()
        calls.forEach { call ->
            if (call.url.contains(query, ignoreCase = true) ||
                call.method.contains(query, ignoreCase = true) ||
                call.responseCode?.toString() == query
            ) {
                matches += call
            } else {
                needBodyCheck += call
            }
        }
        if (inBodies) {
            needBodyCheck.chunked(BODY_SEARCH_CHUNK).forEach { chunk ->
                val byId = dao.getCalls(chunk.map { it.id }).associateBy { it.id }
                chunk.forEach { summary ->
                    if (byId[summary.id]?.toSearchable()?.containsText(query) == true) matches += summary
                }
            }
        }
        return page(matches.sortedByDescending { it.requestTimestamp }, offset, limit)
    }

    private fun Call.toSearchable() = Searchable(requestHeaders, responseHeaders, requestBody, responseBody)

    private class Searchable(
        val requestHeaders: Map<String, List<String>>,
        val responseHeaders: Map<String, List<String>>?,
        val requestBody: ByteArray?,
        val responseBody: ByteArray?,
    )

    private suspend fun Searchable.containsText(query: String): Boolean {
        val inHeaders = requestHeaders.containsText(query) || responseHeaders?.containsText(query) == true
        return inHeaders ||
            requestBody?.decodeBody(requestHeaders)?.contains(query, ignoreCase = true) == true ||
            responseBody?.decodeBody(responseHeaders)?.contains(query, ignoreCase = true) == true
    }

    private fun Map<String, List<String>>.containsText(query: String) = any { (name, values) ->
        name.contains(query, ignoreCase = true) || values.any { it.contains(query, ignoreCase = true) }
    }

    private fun page(calls: List<SelectCalls>, offset: Int, limit: Int): Mcp.CallPage {
        val slice = calls.drop(offset).take(limit)
        return Mcp.CallPage(
            total = calls.size,
            offset = offset,
            limit = limit,
            nextOffset = (offset + slice.size).takeIf { it < calls.size },
            calls = slice.map { it.toModel() },
        )
    }

    private suspend fun getCall(params: Mcp.GetCallParams): Mcp.CallDetail {
        val id = params.id ?: throw IllegalArgumentException("'id' is required")
        val maxBody = (params.maxBodyLength ?: DEFAULT_BODY_LENGTH).coerceAtLeast(0)
        val call = dao.getCalls(listOf(id)).firstOrNull()
            ?: throw IllegalArgumentException("Call not found: $id")
        val events = if (call.streamKind != null) dao.getStreamEvents(listOf(id)) else emptyList()
        val requestBody = call.requestBody?.decodeBody(call.requestHeaders)
        val responseBody = call.responseBody?.decodeBody(call.responseHeaders)

        return Mcp.CallDetail(
            id = call.id,
            method = call.method,
            url = call.url,
            protocol = call.protocol,
            requestTimestamp = call.requestTimestamp,
            responseTimestamp = call.responseTimestamp,
            durationMs = call.responseTimestamp?.minus(call.requestTimestamp),
            status = call.responseCode,
            error = call.error,
            request = Mcp.Message(
                headers = call.requestHeaders,
                contentType = call.requestContentType,
                contentLength = call.requestContentLength,
                body = requestBody?.take(maxBody),
                bodyTruncated = call.isRequestBodyTruncated == true || (requestBody?.length ?: 0) > maxBody,
            ),
            response = Mcp.Message(
                headers = call.responseHeaders,
                contentType = call.responseContentType,
                contentLength = call.responseContentLength,
                body = responseBody?.take(maxBody),
                bodyTruncated = call.isResponseBodyTruncated == true || (responseBody?.length ?: 0) > maxBody,
            ),
            stream = call.streamKind?.let {
                Mcp.Stream(
                    kind = it,
                    status = call.streamStatus,
                    closeCode = call.streamCloseCode,
                    closeReason = call.streamCloseReason,
                    error = call.streamError,
                    events = events.take(MAX_STREAM_EVENTS).map { event -> event.toModel(maxBody) },
                    eventsTruncated = events.size > MAX_STREAM_EVENTS,
                )
            },
        )
    }

    private suspend fun getStats(): Mcp.Stats {
        val calls = dao.getCalls().first()
        val latencies = calls.mapNotNull { call -> call.responseTimestamp?.minus(call.requestTimestamp) }
            .sorted()

        return Mcp.Stats(
            total = calls.size,
            inProgress = calls.count { it.responseCode == null && it.error.isNullOrBlank() },
            errors = calls.count { it.isError },
            byStatusClass = calls.mapNotNull { it.responseCode }
                .groupingBy { "${it / 100}xx" }.eachCount().entries.sortedBy { it.key }
                .associate { it.key to it.value },
            byMethod = calls.groupingBy { it.method.uppercase() }.eachCount().entries.sortedBy { it.key }
                .associate { it.key to it.value },
            byHost = calls.groupingBy { it.url.hostOf() }.eachCount().entries
                .sortedByDescending { it.value }.take(TOP_HOSTS)
                .associate { it.key to it.value },
            latencyMs = Mcp.Latency(
                avg = latencies.takeIf { it.isNotEmpty() }?.average(),
                p50 = latencies.percentile(50),
                p95 = latencies.percentile(95),
                max = latencies.lastOrNull(),
            ),
            oldestRequestTimestamp = calls.minOfOrNull { it.requestTimestamp },
            newestRequestTimestamp = calls.maxOfOrNull { it.requestTimestamp },
        )
    }

    private fun SelectCalls.toModel() = Mcp.CallSummary(
        id = id,
        method = method,
        url = url,
        requestTimestamp = requestTimestamp,
        status = responseCode,
        durationMs = responseTimestamp?.minus(requestTimestamp),
        contentType = responseContentType,
        responseContentLength = responseContentLength,
        error = error,
        streamKind = streamKind,
    )

    private fun StreamEvent.toModel(maxBody: Int) = Mcp.StreamEvent(
        timestamp = timestamp,
        direction = if (isSent) STREAM_DIRECTION_SENT else STREAM_DIRECTION_RECEIVED,
        type = type,
        name = name,
        size = size,
        payload = if (type != StreamEventType.BINARY) payload?.decodeToString()?.take(maxBody) else null,
        truncated = isTruncated,
    )

    private fun List<Long>.percentile(p: Int): Long? =
        if (isEmpty()) null else this[((size - 1) * p) / 100]

    private fun String.hostOf(): String =
        substringAfter("://").substringBefore('/').substringBefore('?').ifEmpty { this }

    companion object {
        const val METHOD_LIST_CALLS = "list_calls"
        const val METHOD_GET_CALL = "get_call"
        const val METHOD_SEARCH_CALLS = "search_calls"
        const val METHOD_GET_STATS = "get_stats"

        private const val STREAM_DIRECTION_SENT = "sent"
        private const val STREAM_DIRECTION_RECEIVED = "received"

        private const val DEFAULT_LIMIT = 50
        private const val MAX_LIMIT = 200
        private const val DEFAULT_BODY_LENGTH = 32_000
        private const val MAX_STREAM_EVENTS = 200
        private const val BODY_SEARCH_CHUNK = 50
        private const val TOP_HOSTS = 10
    }
}
