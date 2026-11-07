# MCP

Let any MCP-capable AI agent (Copilot, Claude, Cursor, ...) query the calls captured by KtorMonitor.
Read-only, debug-time, works on Android, iOS, Desktop and Web.

```
AI client ──stdio (MCP)──> companion JAR ──WebSocket (localhost:8765)──< your app
```

The app connects **out** to the companion, so it also works in browsers and on iOS.

## 1. Add the module (debug only)

```kotlin
debugImplementation("ro.cosminmihu.ktor:ktor-monitor-mcp:<version>")
releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-mcp-no-op:<version>")
```

```kotlin
KtorMonitorMcp.start {
    // host = "localhost"; port = 8765; token = null; appName = null
}
```

`KtorMonitorMcp.status` exposes the connection state; it is also shown in the list header.

## 2. Run the companion in your AI client

Download `ktor-monitor-mcp-companion-<version>-all.jar` from Maven Central (Java 17+), then register it:

```json
{
  "mcpServers": {
    "ktor-monitor": {
      "command": "java",
      "args": ["-jar", "/path/to/ktor-monitor-mcp-companion-<version>-all.jar"]
    }
  }
}
```

Options: `--port` / `KTOR_MONITOR_MCP_PORT`, `--token` / `KTOR_MONITOR_MCP_TOKEN` (must match `token` in the app config).
The server only listens on `127.0.0.1`.

## 3. Connectivity

| Platform          | Notes |
|-------------------|-------|
| Desktop / Web     | `localhost` works as is. |
| Android emulator/device | `adb reverse tcp:8765 tcp:8765` (emulator can also use `10.0.2.2`). Needs cleartext traffic allowed in debug. |
| iOS simulator     | Add `NSAppTransportSecurity > NSAllowsLocalNetworking = true` to `Info.plist`. |

## Tools

`list_apps`, `list_calls`, `get_call`, `search_calls`, `get_stats` (all accept an optional `app_id`).
`list_calls`/`search_calls` paginate with `offset` / `next_offset`; bodies are truncated by `max_body_length`.

## Security notes

* Headers are masked by your `sanitizeHeader` config at capture time, so the AI sees the same masked values as the UI.
* **Bodies are not sanitized.** Do not enable MCP when traffic contains data you don't want an AI to read.
* Use the optional `token` as a shared secret between app and companion.
* The MCP server is a small hand-written JSON-RPC implementation (protocol versions 2025-06-18, 2025-03-26, 2024-11-05).
