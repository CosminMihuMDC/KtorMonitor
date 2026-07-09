package ro.cosminmihu.ktor.monitor.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.db.sqldelight.Call
import ro.cosminmihu.ktor.monitor.db.sqldelight.LibraryDatabase
import ro.cosminmihu.ktor.monitor.db.sqldelight.SelectCalls
import ro.cosminmihu.ktor.monitor.db.sqldelight.SelectCallsWithLimit

internal class LibraryDao(private val database: LibraryDatabase) {

    suspend fun saveRequest(
        id: String,
        method: String,
        url: String,
        requestTimestamp: Long,
        requestHeaders: Map<String, List<String>>,
        requestContentType: String?,
        requestContentLength: Long,
        requestBody: ByteArray?,
        isRequestBodyTruncated: Boolean,
    ) = withContext(Dispatchers.IO) {
        database.callQueries.saveRequest(
            id,
            method,
            url,
            requestTimestamp,
            requestHeaders,
            requestContentType,
            requestContentLength,
            requestBody,
            isRequestBodyTruncated,
        )
    }

    suspend fun saveRequest(
        id: String,
        error: Throwable,
    ) = withContext(Dispatchers.IO) {
        database.callQueries.saveError(
            error.stackTraceToString(),
            id
        )
    }

    suspend fun saveResponse(
        id: String,
        protocol: String?,
        requestTimestamp: Long,
        responseCode: Int,
        responseTimestamp: Long,
        responseContentType: String?,
        responseHeaders: Map<String, List<String>>?,
    ) = withContext(Dispatchers.IO) {
        database.callQueries.saveResponse(
            protocol,
            requestTimestamp,
            responseCode.toLong(),
            responseTimestamp,
            responseContentType,
            responseHeaders,
            id
        )
    }

    suspend fun saveResponseBody(
        id: String,
        responseContentLength: Long?,
        responseBody: ByteArray?,
        isResponseBodyTruncated: Boolean,
    ) = withContext(Dispatchers.IO) {
        database.callQueries.saveResponseBody(
            responseContentLength,
            responseBody,
            isResponseBodyTruncated,
            id
        )
    }

    suspend fun appendResponseBody(
        id: String,
        chunk: ByteArray,
        deltaSize: Long,
        isResponseBodyTruncated: Boolean,
    ) = withContext(Dispatchers.IO) {
        database.callQueries.appendResponseBody(
            chunk,
            deltaSize,
            isResponseBodyTruncated,
            id,
        )
    }

    suspend fun saveResponse(
        id: String,
        error: Throwable,
    ) = withContext(Dispatchers.IO) {
        database.callQueries.saveError(
            error.stackTraceToString(),
            id
        )
    }

    fun getCalls(): Flow<Query<SelectCalls>> =
        database.callQueries.selectCalls().asFlow()

    fun getCall(id: String): Flow<Query<Call>> =
        database.callQueries.selectCall(id).asFlow()

    fun getCalls(limit: Long): Flow<Query<SelectCallsWithLimit>> =
        database.callQueries.selectCallsWithLimit(limit).asFlow()

    suspend fun deleteCalls() = withContext(Dispatchers.IO) {
        database.callQueries.deleteCalls()
    }

    suspend fun deleteCallsBefore(threshold: Long) = withContext(Dispatchers.IO) {
        database.callQueries.deleteCallsBefore(threshold)
    }
}
