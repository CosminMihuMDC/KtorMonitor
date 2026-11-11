package ro.cosminmihu.ktor.monitor.mcp.companion

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_HOST
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_PORT
import ro.cosminmihu.ktor.monitor.mcp.protocol.PORT_FALLBACK_COUNT
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import kotlin.system.exitProcess

private const val USAGE = """
Ktor Monitor MCP companion

Usage: java -jar ktor-monitor-mcp-companion-<version>-all.jar [--port 2991] [--token <secret>]

  --port    Preferred WebSocket port (env KTOR_MONITOR_MCP_PORT, default 2991); if busy, the next free one (up to +9).
  --token   Shared secret the apps must send (env KTOR_MONITOR_MCP_TOKEN, default: none).

The MCP server speaks over stdin/stdout. Logs go to stderr.
"""

/** Logs to stderr, stdout is reserved for the MCP protocol. */
internal fun log(message: String) = System.err.println("[ktor-monitor-mcp] $message")

fun main(args: Array<String>) {
    val options = parseArgs(args)
    if (options == null) {
        System.err.println(USAGE.trim())
        exitProcess(2)
    }

    val registry = AppRegistry()
    var lastError: Exception? = null
    val started = (options.port until options.port + PORT_FALLBACK_COUNT)
        .filter { it <= 65535 }
        .firstNotNullOfOrNull { port ->
            try {
                port to startWebSocketServer(registry, port, options.token)
            } catch (exception: Exception) {
                lastError = exception
                log("Port $port is busy, trying the next one")
                null
            }
        }
    if (started == null) {
        log("Cannot listen on $DEFAULT_HOST:${options.port}..: ${lastError?.message}")
        exitProcess(1)
    }
    val (port, server) = started
    log("Listening for apps on ws://$DEFAULT_HOST:$port")

    val mcp = McpServer(registry, McpServer::class.java.`package`?.implementationVersion ?: "dev")
    val out = PrintStream(FileOutputStream(FileDescriptor.out), true, Charsets.UTF_8)
    val writeLock = Mutex()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    runBlocking {
        System.`in`.bufferedReader(Charsets.UTF_8).useLines { lines ->
            for (line in lines) {
                if (line.isBlank()) continue
                scope.launch {
                    val response = mcp.handle(line) ?: return@launch
                    writeLock.withLock { out.println(response) }
                }
            }
        }
    }
    // stdin closed: let in-flight requests answer before exiting.
    runBlocking { withTimeoutOrNull(5_000) { scope.coroutineContext[Job]?.children?.toList()?.joinAll() } }
    server.stop(500, 1000)
    exitProcess(0)
}

private class Options(val port: Int, val token: String?)

private fun parseArgs(args: Array<String>): Options? {
    var port = System.getenv("KTOR_MONITOR_MCP_PORT")?.let { it.toIntOrNull()?.takeIf { p -> p in 1..65535 } ?: return null }
        ?: DEFAULT_PORT
    var token = System.getenv("KTOR_MONITOR_MCP_TOKEN")?.takeIf { it.isNotEmpty() }
    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--port" -> port = args.getOrNull(++i)?.toIntOrNull()?.takeIf { it in 1..65535 } ?: return null
            "--token" -> token = args.getOrNull(++i) ?: return null
            else -> return null
        }
        i++
    }
    return Options(port, token)
}
