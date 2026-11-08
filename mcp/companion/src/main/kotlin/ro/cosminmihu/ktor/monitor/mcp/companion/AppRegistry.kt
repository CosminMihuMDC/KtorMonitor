package ro.cosminmihu.ktor.monitor.mcp.companion

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import ro.cosminmihu.ktor.monitor.mcp.protocol.Hello
import ro.cosminmihu.ktor.monitor.mcp.protocol.Request
import ro.cosminmihu.ktor.monitor.mcp.protocol.Response
import ro.cosminmihu.ktor.monitor.mcp.protocol.encode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/** Error shown to the AI as a failed tool result. */
internal class ToolException(message: String) : Exception(message)

/** A connected app. */
internal class AppConnection(
    val hello: Hello,
    private val sendText: suspend (String) -> Unit,
) {
    private val pending = ConcurrentHashMap<Long, CompletableDeferred<Response>>()
    private val nextId = AtomicLong(1)

    suspend fun request(method: String, params: JsonObject, timeoutMillis: Long): String {
        val id = nextId.getAndIncrement()
        val deferred = CompletableDeferred<Response>()
        pending[id] = deferred
        try {
            sendText(Request(id, method, params).encode())
            val response = withTimeoutOrNull(timeoutMillis) { deferred.await() }
                ?: throw ToolException("The app '${hello.appName}' did not answer within ${timeoutMillis / 1000}s.")
            response.error?.let { throw ToolException(it) }
            return response.result ?: "null"
        } finally {
            pending.remove(id)
        }
    }

    fun complete(response: Response) {
        pending[response.id]?.complete(response)
    }

    fun disconnect() {
        pending.values.forEach { it.completeExceptionally(ToolException("The app '${hello.appName}' disconnected.")) }
    }
}

/** Keeps track of the apps connected to the companion and routes requests to them. */
internal class AppRegistry(private val requestTimeoutMillis: Long = 15_000) {

    private val apps = ConcurrentHashMap<String, AppConnection>()

    fun register(connection: AppConnection) {
        apps.put(connection.hello.appId, connection)?.disconnect()
    }

    fun unregister(connection: AppConnection) {
        apps.remove(connection.hello.appId, connection)
    }

    fun list(): List<Hello> = apps.values.map { it.hello }.sortedBy { it.appName }

    suspend fun call(appId: String?, method: String, params: JsonObject): String {
        val connection = when {
            appId != null -> apps[appId]
                ?: throw ToolException("No app with id '$appId' is connected. Use list_apps to see the connected apps.")

            apps.isEmpty() -> throw ToolException(
                "No app is connected. Start the app with KtorMonitorMcp.start() and make sure it can reach this " +
                    "machine (Android device: adb reverse tcp:PORT tcp:PORT)."
            )

            apps.size == 1 -> apps.values.first()
            else -> throw ToolException(
                "Multiple apps are connected, pass 'app_id'. Connected: " +
                    list().joinToString { "${it.appId} (${it.appName}, ${it.platform})" }
            )
        }
        return connection.request(method, params, requestTimeoutMillis)
    }
}
