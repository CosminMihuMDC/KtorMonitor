package ro.cosminmihu.ktor.monitor.domain

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import ro.cosminmihu.ktor.monitor.db.LibraryDao
import ro.cosminmihu.ktor.monitor.db.LibraryDatabase
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import ro.cosminmihu.ktor.monitor.domain.model.Mcp

class McpQueryUseCaseTest {

    private lateinit var file: File
    private lateinit var database: LibraryDatabase
    private lateinit var dao: LibraryDao
    private lateinit var useCase: McpQueryUseCase

    @BeforeTest
    fun setUp() {
        file = File.createTempFile("ktor_monitor_mcp_test", ".db").also { it.delete() }
        database = Room.databaseBuilder<LibraryDatabase>(name = file.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()
        dao = LibraryDao(database)
        useCase = McpQueryUseCase(dao)
    }

    @AfterTest
    fun tearDown() {
        database.close()
        file.delete()
    }

    private suspend fun save(id: String, method: String, url: String, status: Int, body: String, timestamp: Long) {
        dao.saveRequest(
            id = id,
            method = method,
            url = url,
            requestTimestamp = timestamp,
            requestHeaders = mapOf("Authorization" to listOf("***")),
            requestContentType = null,
            requestContentLength = 0,
            requestBody = null,
            isRequestBodyTruncated = false,
        )
        dao.saveResponse(id, "HTTP/1.1", timestamp, status, timestamp + 10, "application/json", mapOf("X" to listOf("y")))
        dao.saveResponseBody(id, body.length.toLong(), body.encodeToByteArray(), false)
    }

    private suspend fun page(method: String, params: String = "{}"): Mcp.CallPage =
        Json.decodeFromString(useCase(method, params))

    private fun Mcp.CallPage.ids() = calls.map { it.id }

    @Test
    fun listCallsFiltersAndPaginatesNewestFirst() = runTest {
        save("1", "GET", "https://a.com/users", 200, "{}", 1)
        save("2", "POST", "https://a.com/login", 401, "{}", 2)
        save("3", "GET", "https://b.com/users", 500, "{}", 3)

        assertEquals(listOf("3", "2", "1"), page("list_calls").ids())
        assertEquals(listOf("3", "1"), page("list_calls", """{"method":"get"}""").ids())
        assertEquals(listOf("3", "2"), page("list_calls", """{"only_errors":true}""").ids())
        assertEquals(listOf("2"), page("list_calls", """{"status_min":400,"status_max":499}""").ids())

        val first = page("list_calls", """{"limit":2}""")
        assertEquals(3, first.total)
        assertEquals(2, first.nextOffset)
    }

    @Test
    fun searchCallsFindsTextInBodies() = runTest {
        save("1", "GET", "https://a.com/one", 200, """{"name":"needle"}""", 1)
        save("2", "GET", "https://a.com/two", 200, """{"name":"hay"}""", 2)

        assertEquals(listOf("1"), page("search_calls", """{"query":"NEEDLE"}""").ids())
        assertEquals(emptyList(), page("search_calls", """{"query":"needle","in_bodies":false}""").ids())
        assertEquals(listOf("2"), page("search_calls", """{"query":"/two"}""").ids())
    }

    @Test
    fun getCallReturnsDecodedAndTruncatedBody() = runTest {
        save("1", "GET", "https://a.com/one", 200, "0123456789", 1)

        val call = Json.decodeFromString<Mcp.CallDetail>(useCase("get_call", """{"id":"1","max_body_length":4}"""))
        assertEquals("0123", call.response.body)
        assertTrue(call.response.bodyTruncated)
        assertEquals(listOf("***"), call.request.headers?.get("Authorization"))
        assertEquals(10, call.durationMs)
    }

    @Test
    fun statsAndErrors() = runTest {
        save("1", "GET", "https://a.com/one", 200, "{}", 1)
        save("2", "GET", "https://a.com/two", 500, "{}", 2)

        val stats = Json.decodeFromString<Mcp.Stats>(useCase("get_stats", "{}"))
        assertEquals(2, stats.total)
        assertEquals(1, stats.errors)
        assertEquals(1, stats.byStatusClass["5xx"])

        assertFailsWith<IllegalArgumentException> { useCase("get_call", """{"id":"missing"}""") }
        assertFailsWith<IllegalArgumentException> { useCase("unknown", "{}") }
        assertFailsWith<IllegalArgumentException> { useCase("search_calls", "{}") }
    }
}
