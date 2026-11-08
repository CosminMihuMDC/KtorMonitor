package ro.cosminmihu.ktor.monitor.mcp.companion

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ro.cosminmihu.ktor.monitor.mcp.protocol.DEFAULT_PORT
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import kotlin.system.exitProcess

private const val USAGE = """
Ktor Monitor MCP companion

Usage: java -jar ktor-monitor-mcp-companion-<version>-all.jar [--port 8765] [--token <secret>]

  --port    WebSocket port the apps connect to (env KTOR_MONITOR_MCP_PORT, default 8765).
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
    val server = try {
        startWebSocketServer(registry, options.port, options.token)
    } catch (e: Exception) {
        log("Cannot listen on 127.0.0.1:${options.port}: ${e.message}")
        exitProcess(1)
    }
    log("Listening for apps on ws://127.0.0.1:${options.port}")

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
    // stdin closed: the AI client went away.
    server.stop(500, 1000)
    exitProcess(0)
}

private class Options(val port: Int, val token: String?)

private fun parseArgs(args: Array<String>): Options? {
    var port = System.getenv("KTOR_MONITOR_MCP_PORT")?.toIntOrNull() ?: DEFAULT_PORT
    var token = System.getenv("KTOR_MONITOR_MCP_TOKEN")?.takeIf { it.isNotEmpty() }
    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--port" -> port = args.getOrNull(++i)?.toIntOrNull() ?: return null
            "--token" -> token = args.getOrNull(++i) ?: return null
            else -> return null
        }
        i++
    }
    return Options(port, token)
}
