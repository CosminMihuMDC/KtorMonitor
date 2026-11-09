package ro.cosminmihu.ktor.monitor

import kotlinx.coroutines.CoroutineScope
import ro.cosminmihu.ktor.monitor.db.LibraryDao
import ro.cosminmihu.ktor.monitor.di.LibraryKoinContext
import ro.cosminmihu.ktor.monitor.di.inject
import ro.cosminmihu.ktor.monitor.domain.ConfigUseCase
import ro.cosminmihu.ktor.monitor.domain.ListenByRecentCallsUseCase
import ro.cosminmihu.ktor.monitor.domain.McpQueryUseCase
import ro.cosminmihu.ktor.monitor.domain.model.Mcp
import ro.cosminmihu.ktor.monitor.domain.model.NetworkClient
import ro.cosminmihu.ktor.monitor.domain.model.Config
import kotlin.time.Duration
import kotlin.uuid.Uuid

/**
 * Marks declarations that are **internal** to the Ktor Monitor library modules.
 *
 * These APIs are not intended for external use and may change without notice.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal API for Ktor Monitor library modules. Do not use it directly."
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
public annotation class InternalKtorMonitorApi

/**
 * Internal bridge API for Ktor Monitor library modules.
 *
 * This object provides access to internal components of the library
 * for use by the Ktor plugin module. Do not use directly.
 */
@InternalKtorMonitorApi
public object InternalLibraryBridge {

    /**
     * Configure the library.
     */
    @InternalKtorMonitorApi
    public fun setConfig(
        isActive: Boolean,
        showNotification: Boolean,
        retentionPeriod: Duration,
        maxContentLength: Int,
        networkClient: NetworkClient,
    ) {
        LibraryKoinContext.koin.get<ConfigUseCase>().setConfig(
            Config(
                isActive = isActive,
                showNotification = showNotification,
                retentionPeriod = retentionPeriod,
                maxContentLength = maxContentLength,
                networkClient = networkClient,
            )
        )
    }

    /**
     * Start listening for recent calls (triggers notifications & retention).
     */
    @InternalKtorMonitorApi
    public fun startListening() {
        LibraryKoinContext.koin.get<ListenByRecentCallsUseCase>()()
    }


    /**
     * Show the MCP connection state in the UI.
     *
     * @param isConnected `true` when connected, `false` when connecting, `null` when MCP is stopped.
     * @param isRejected `true` when the companion refused the connection (wrong token or version), overrides [isConnected].
     */
    @InternalKtorMonitorApi
    public fun setMcpStatus(isConnected: Boolean?, isRejected: Boolean = false) {
        LibraryKoinContext.koin.get<ConfigUseCase>().mcpStatus.value = when {
            isRejected -> Mcp.Status.Rejected
            isConnected == null -> null
            isConnected -> Mcp.Status.Connected
            else -> Mcp.Status.Connecting
        }
    }

    /**
     * Run a read-only MCP query (`list_calls`, `get_call`, `search_calls`, `get_stats`) over the stored calls.
     *
     * @param method The query name.
     * @param paramsJson The query parameters as a JSON object.
     * @return The result as a JSON string.
     * @throws IllegalArgumentException If the method is unknown or the parameters are invalid.
     */
    @InternalKtorMonitorApi
    public suspend fun mcpQuery(method: String, paramsJson: String): String {
        return LibraryKoinContext.koin.get<McpQueryUseCase>()(method, paramsJson)
    }

    /**
     * Generate a unique call identifier.
     */
    @InternalKtorMonitorApi
    public fun newCallId(): String = Uuid.random().toString()

    /**
     * Replace the values of the headers that have a placeholder with that placeholder and sort by name.
     *
     * @param placeholderFor Returns the placeholder for a header name, or `null` to keep its values.
     */
    @InternalKtorMonitorApi
    public fun sanitizeHeaders(
        headers: Map<String, List<String>>,
        placeholderFor: (name: String) -> String?,
    ): Map<String, List<String>> = headers.entries
        .sortedBy { it.key }
        .associate { (name, values) ->
            name to (placeholderFor(name)?.let { listOf(it) } ?: values)
        }

    /**
     * Get the library coroutine scope.
     */
    @InternalKtorMonitorApi
    public fun coroutineScope(): CoroutineScope {
        return scope
    }

    private val scope: CoroutineScope by lazy { LibraryKoinContext.koin.get<CoroutineScope>() }

    /**
     * Save request data.
     */
    @InternalKtorMonitorApi
    public suspend fun saveRequest(
        id: String,
        method: String,
        url: String,
        requestTimestamp: Long,
        requestHeaders: Map<String, List<String>>,
        requestContentType: String?,
        requestContentLength: Long,
        requestBody: ByteArray?,
        isRequestBodyTruncated: Boolean,
    ) {
        val dao by LibraryKoinContext.inject<LibraryDao>()
        dao.saveRequest(
            id = id,
            method = method,
            url = url,
            requestTimestamp = requestTimestamp,
            requestHeaders = requestHeaders,
            requestContentType = requestContentType,
            requestContentLength = requestContentLength,
            requestBody = requestBody,
            isRequestBodyTruncated = isRequestBodyTruncated,
        )
    }

    /**
     * Save request error.
     */
    @InternalKtorMonitorApi
    public suspend fun saveRequestError(
        id: String,
        error: Throwable,
    ) {
        val dao by LibraryKoinContext.inject<LibraryDao>()
        dao.saveRequest(id = id, error = error)
    }

    /**
     * Save response data.
     */
    @InternalKtorMonitorApi
    public suspend fun saveResponse(
        id: String,
        protocol: String?,
        requestTimestamp: Long,
        responseCode: Int,
        responseTimestamp: Long,
        responseContentType: String?,
        responseHeaders: Map<String, List<String>>?,
    ) {
        val dao by LibraryKoinContext.inject<LibraryDao>()
        dao.saveResponse(
            id = id,
            protocol = protocol,
            requestTimestamp = requestTimestamp,
            responseCode = responseCode,
            responseTimestamp = responseTimestamp,
            responseContentType = responseContentType,
            responseHeaders = responseHeaders,
        )
    }

    /**
     * Save response body.
     */
    @InternalKtorMonitorApi
    public suspend fun saveResponseBody(
        id: String,
        responseContentLength: Long?,
        responseBody: ByteArray?,
        isResponseBodyTruncated: Boolean,
    ) {
        val dao by LibraryKoinContext.inject<LibraryDao>()
        dao.saveResponseBody(
            id = id,
            responseContentLength = responseContentLength,
            responseBody = responseBody,
            isResponseBodyTruncated = isResponseBodyTruncated,
        )
    }

    /**
     * Append a chunk to the response body of an existing call.
     *
     * Used by streamed responses (Server-Sent Events, chunked text streams) so the
     * UI can observe the body grow incrementally instead of waiting for the
     * entire stream to terminate.
     *
     * @param id Call identifier returned by [saveRequest].
     * @param chunk Raw bytes to append. Should already be truncated by the caller
     *   so total stored size never exceeds the configured `maxContentLength`.
     * @param deltaSize Real size of the chunk on the wire (before truncation),
     *   used to keep `responseContentLength` accurate.
     * @param isResponseBodyTruncated Final truncation flag for the call so far.
     */
    @InternalKtorMonitorApi
    public suspend fun appendResponseBody(
        id: String,
        chunk: ByteArray,
        deltaSize: Long,
        isResponseBodyTruncated: Boolean,
    ) {
        val dao by LibraryKoinContext.inject<LibraryDao>()
        dao.appendResponseBody(
            id = id,
            chunk = chunk,
            deltaSize = deltaSize,
            isResponseBodyTruncated = isResponseBodyTruncated,
        )
    }

    /**
     * Save response error.
     */
    @InternalKtorMonitorApi
    public suspend fun saveResponseError(
        id: String,
        error: Throwable,
    ) {
        val dao by LibraryKoinContext.inject<LibraryDao>()
        dao.saveResponse(id = id, error = error)
    }
}

