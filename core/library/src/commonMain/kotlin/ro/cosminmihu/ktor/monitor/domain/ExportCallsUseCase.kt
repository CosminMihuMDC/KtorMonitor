package ro.cosminmihu.ktor.monitor.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ro.cosminmihu.ktor.monitor.core.BuildKonfig
import ro.cosminmihu.ktor.monitor.db.LibraryDao
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call
import ro.cosminmihu.ktor.monitor.domain.model.decodeBody
import kotlin.time.Clock
import kotlin.time.Instant

internal enum class CallsExportFormat(
    val extension: String,
    val mimeType: String,
) {
    Json(extension = "json", mimeType = "application/json"),
    Text(extension = "txt", mimeType = "text/plain"),
    Curl(extension = "sh", mimeType = "text/plain"),
    Wget(extension = "sh", mimeType = "text/plain"),
    Url(extension = "txt", mimeType = "text/plain"),
}

internal data class CallsExportResult(
    val fileName: String,
    val mimeType: String,
    val content: String,
)

internal class ExportCallsUseCase(
    private val dao: LibraryDao,
    private val exportCallUrlUseCase: ExportCallUrlUseCase,
    private val exportCallRequestAsCurlUseCase: ExportCallRequestAsCurlUseCase,
    private val exportCallRequestAsWgetUseCase: ExportCallRequestAsWgetUseCase,
    private val exportCallAsTextUseCase: ExportCallAsTextUseCase,
) {

    suspend operator fun invoke(
        ids: List<String>,
        format: CallsExportFormat,
    ): CallsExportResult? = withContext(Dispatchers.Default) {
        if (ids.isEmpty()) return@withContext null

        val distinctIds = ids.distinct()
        val callsById = dao.getCalls(distinctIds).associateBy(Call::id)
        val calls = distinctIds.mapNotNull(callsById::get)
        if (calls.isEmpty()) return@withContext null

        val fileName = "ktor-monitor-export-${Clock.System.now().toString().take(10)}.${format.extension}"

        when (format) {
            CallsExportFormat.Json -> CallsExportResult(
                fileName = fileName,
                mimeType = format.mimeType,
                content = exportAsJson(calls),
            )

            CallsExportFormat.Text -> CallsExportResult(
                fileName = fileName,
                mimeType = format.mimeType,
                content = exportAsText(calls),
            )

            CallsExportFormat.Curl -> CallsExportResult(
                fileName = fileName,
                mimeType = format.mimeType,
                content = exportAsCurl(calls),
            )

            CallsExportFormat.Wget -> CallsExportResult(
                fileName = fileName,
                mimeType = format.mimeType,
                content = exportAsWget(calls),
            )

            CallsExportFormat.Url -> CallsExportResult(
                fileName = fileName,
                mimeType = format.mimeType,
                content = exportAsUrlList(calls),
            )
        }
    }

    private suspend fun exportAsJson(calls: List<Call>): String {
        val payload = CallsExportPayload(
            exportedAt = Clock.System.now().toString(),
            app = ExportedApp(
                name = "Ktor Monitor",
                version = BuildKonfig.LIBRARY_VERSION,
            ),
            requests = calls.map { call ->
                ExportedCall(
                    id = call.id,
                    startedAt = Instant.fromEpochMilliseconds(call.requestTimestamp).toString(),
                    durationMs = call.responseTimestamp?.minus(call.requestTimestamp),
                    request = ExportedRequest(
                        method = call.method,
                        url = call.url,
                        headers = call.requestHeaders,
                        bodyPreview = call.requestBody
                            ?.decodeBody(call.requestHeaders)
                            ?.toBodyPreview(call.isRequestBodyTruncated == true),
                    ),
                    response = ExportedResponse(
                        statusCode = call.responseCode?.toInt(),
                        headers = call.responseHeaders,
                        bodyPreview = call.responseBody
                            ?.decodeBody(call.responseHeaders)
                            ?.toBodyPreview(call.isResponseBodyTruncated == true),
                        error = call.error,
                    )
                )
            }
        )

        return Json.encodeToString(payload)
    }

    private suspend fun exportAsText(calls: List<Call>): String {
        val sections = mutableListOf<String>()
        for (call in calls) {
            sections += exportCallAsTextUseCase(call).trimEnd()
        }
        return sections.joinToString(separator = "\n\n---\n\n")
    }

    private suspend fun exportAsCurl(calls: List<Call>): String {
        val commands = mutableListOf<String>()
        for (call in calls) {
            commands += exportCallRequestAsCurlUseCase(call)
        }
        return commands.joinToString(separator = "\n\n")
    }

    private suspend fun exportAsWget(calls: List<Call>): String {
        val commands = mutableListOf<String>()
        for (call in calls) {
            commands += exportCallRequestAsWgetUseCase(call)
        }
        return commands.joinToString(separator = "\n\n")
    }

    private suspend fun exportAsUrlList(calls: List<Call>): String {
        val urls = mutableListOf<String>()
        for (call in calls) {
            urls += exportCallUrlUseCase(call)
        }
        return urls.joinToString(separator = "\n")
    }
}

private const val BODY_PREVIEW_LIMIT = 2_000

private fun String.toBodyPreview(isTruncatedAtCapture: Boolean): String {
    val clipped = take(BODY_PREVIEW_LIMIT)
    return when {
        isTruncatedAtCapture -> "$clipped (truncated)"
        length > BODY_PREVIEW_LIMIT -> "$clipped...(clipped for export)"
        else -> clipped
    }
}

@Serializable
private data class CallsExportPayload(
    val exportedAt: String,
    val app: ExportedApp,
    val requests: List<ExportedCall>,
)

@Serializable
private data class ExportedApp(
    val name: String,
    val version: String,
)

@Serializable
private data class ExportedCall(
    val id: String,
    val startedAt: String,
    val durationMs: Long?,
    val request: ExportedRequest,
    val response: ExportedResponse,
)

@Serializable
private data class ExportedRequest(
    val method: String,
    val url: String,
    val headers: Map<String, List<String>>,
    val bodyPreview: String?,
)

@Serializable
private data class ExportedResponse(
    val statusCode: Int?,
    val headers: Map<String, List<String>>?,
    val bodyPreview: String?,
    val error: String?,
)

