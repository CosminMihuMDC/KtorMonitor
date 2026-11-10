# MCP

Let any MCP-capable AI agent (Copilot, Claude, Cursor, ...) query the calls captured by KtorMonitor.
Read-only, debug-time, works on Android, iOS, Desktop and Web.

```
AI client ──stdio (MCP)──> companion JAR ──WebSocket (127.0.0.1:8765)──< your app
```

The app connects **out** to the companion, so it also works in browsers and on iOS.

## Quick setup checklist

Do these in order. Every step is required unless marked optional.

1. **App:** add the `ktor-monitor-mcp` dependency (step 1).
2. **App:** call `KtorMonitorMcp.start()` once at startup, in debug builds only (step 1).
3. **Machine:** download the companion JAR (step 2).
4. **AI client:** register the JAR as an MCP server and restart the client (step 3).
5. **Android/iOS only:** make the app able to reach `127.0.0.1:8765` (see [Connectivity](#5-connectivity)).
6. **Verify:** run the app, then ask the AI to call `list_apps` (step 4).

!!! note "Prerequisites"
    Ktor Monitor itself (`ktor-monitor-logging`, `-okhttp-interceptor` or `-http4k-filter`) must already be
    installed, otherwise there are no calls to query. Java 11+ must be on the `PATH` of the machine running the AI client.

## 1. Add the module to your app (debug only)

Dependency (replace `<version>` with the Ktor Monitor version, e.g. `1.18.0`):

```kotlin
// Android module
debugImplementation("ro.cosminmihu.ktor:ktor-monitor-mcp:<version>")
releaseImplementation("ro.cosminmihu.ktor:ktor-monitor-mcp-no-op:<version>")
```

For a Kotlin Multiplatform module without build types, add it to `commonMain` (or the target source set you need)
and only call `start` from debug code:

```kotlin
kotlin.sourceSets.commonMain.dependencies {
    implementation("ro.cosminmihu.ktor:ktor-monitor-mcp:<version>")
}
```

Start it once, as early as possible (e.g. `Application.onCreate`, `main()`, or where you create the `HttpClient`).
It does nothing unless you call it:

```kotlin
import ro.cosminmihu.ktor.monitor.KtorMonitorMcp

KtorMonitorMcp.start()

// or with options (these are the defaults):
KtorMonitorMcp.start {
    host = "127.0.0.1"
    port = 8765
    token = null      // shared secret, must match the companion (optional)
    appName = null    // name shown by list_apps (optional)
}
```

`KtorMonitorMcp.status` exposes the connection state (`Stopped`, `Connecting`, `Connected`, `Rejected`).
The list header shows a blinking **AI** badge while connected and a static red **AI!** when the companion
rejected the connection (wrong `token`, or different library and companion versions). Nothing is shown while
connecting or stopped.

## 2. Download the companion

The companion is the MCP server your AI client starts. It is a single JAR (Java 11+):

```bash
VERSION=1.18.0   # same as the library version
mkdir -p ~/.ktor-monitor
curl -L -o ~/.ktor-monitor/ktor-monitor-mcp-companion-all.jar \
  "https://repo1.maven.org/maven2/ro/cosminmihu/ktor/ktor-monitor-mcp-companion/$VERSION/ktor-monitor-mcp-companion-$VERSION-all.jar"
```

!!! warning
    Use the **`-all`** JAR (fat jar) and keep its version equal to the `ktor-monitor-mcp` library version,
    otherwise the app is rejected (red **AI!** badge).

Use an **absolute path** in the client config (no `~` or `$HOME`; most clients do not expand them),
e.g. `/Users/you/.ktor-monitor/ktor-monitor-mcp-companion-all.jar`.

## 3. Register the companion in your AI client

The client launches the JAR itself over stdio; you never run it manually. The server name is `ktor-monitor`.

=== "Copilot CLI"

    Add to `~/.copilot/mcp-config.json` (or run `/mcp add` in the CLI):

    ```json
    {
      "mcpServers": {
        "ktor-monitor": {
          "type": "local",
          "command": "java",
          "args": ["-jar", "/absolute/path/ktor-monitor-mcp-companion-all.jar"],
          "tools": ["*"]
        }
      }
    }
    ```

=== "VS Code / Copilot Chat"

    `.vscode/mcp.json` (note the key is `servers`, not `mcpServers`):

    ```json
    {
      "servers": {
        "ktor-monitor": {
          "type": "stdio",
          "command": "java",
          "args": ["-jar", "/absolute/path/ktor-monitor-mcp-companion-all.jar"]
        }
      }
    }
    ```

=== "Claude Code"

    ```bash
    claude mcp add ktor-monitor -- java -jar /absolute/path/ktor-monitor-mcp-companion-all.jar
    ```

=== "Claude Desktop / Cursor"

    `claude_desktop_config.json` or `~/.cursor/mcp.json`:

    ```json
    {
      "mcpServers": {
        "ktor-monitor": {
          "command": "java",
          "args": ["-jar", "/absolute/path/ktor-monitor-mcp-companion-all.jar"]
        }
      }
    }
    ```

=== "Android Studio (Gemini)"

    Settings > Tools > AI > MCP Servers, or the same `mcpServers` JSON as above.

Restart the client (or reload MCP servers) after editing the config.

Options (optional): `--port` / `KTOR_MONITOR_MCP_PORT` (default `8765`), `--token` / `KTOR_MONITOR_MCP_TOKEN`
(must match `token` in the app config). Add them to `args`, or use `"env": { "KTOR_MONITOR_MCP_TOKEN": "..." }`
to keep the token out of the process list. The server only listens on `127.0.0.1`.

!!! tip "Port in use"
    Only **one** companion can listen on a port. If you run several AI clients or projects at once,
    give each a different `--port` and set the matching `port` in the app's `start {}`.

## 4. Verify it works

1. Start the app (debug build). The list header should show the blinking **AI** badge once the AI client has
   launched the companion.
2. Ask the AI: *"Use ktor-monitor `list_apps`"*. It should return your app. Then *"List the last 10 calls"*.
3. If it is empty, make some HTTP requests in the app first.

## 5. Connectivity

| Platform          | Notes |
|-------------------|-------|
| Desktop / Web     | `127.0.0.1` works as is. |
| Android emulator/device | `adb reverse tcp:8765 tcp:8765` (emulator can also use `10.0.2.2`). Needs cleartext traffic allowed in debug. |
| iOS simulator     | Add `NSAppTransportSecurity > NSAllowsLocalNetworking = true` to `Info.plist`. |

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| AI client shows the server as failed / no tools | Check `java -version` is 11+, the JAR path is absolute and is the `-all` JAR. Companion logs go to stderr prefixed with `[ktor-monitor-mcp]` (see the client's MCP log). |
| `list_apps` returns nothing | The app is not connected: it did not call `KtorMonitorMcp.start()`, is a release build (no-op), or cannot reach the host/port (see [Connectivity](#5-connectivity)). |
| Red **AI!** badge / `Rejected` | `token` differs between app and companion, or library and companion versions differ. |
| Port already in use | Another companion is running; change `--port` and the app `port`. |
| Android: connection fails | Run `adb reverse tcp:8765 tcp:8765`, allow cleartext in debug, add `INTERNET` permission. |

## Tools

`list_apps`, `list_calls`, `get_call`, `search_calls`, `get_stats` (all accept an optional `app_id`).
Arguments are validated against the tool schema (types and ranges, e.g. `limit` 1..200, `max_body_length` up to 256000); invalid input is returned to the AI as a tool error so it can retry.
`list_calls`/`search_calls` paginate with `offset` / `next_offset`; bodies are truncated by `max_body_length`.

## Security notes

* Headers are masked by your `sanitizeHeader` config at capture time, so the AI sees the same masked values as the UI.
* **Bodies are not sanitized.** Do not enable MCP when traffic contains data you don't want an AI to read.
* Set a `token` (and pass the same value with `--token` or `KTOR_MONITOR_MCP_TOKEN`, which keeps it out of the process list).
  The token is never sent over the socket: app and companion prove they know it with a challenge/response (HMAC-SHA256),
  so a process that grabs the port first can neither learn the token nor read your traffic. Without a token, any local
  process can pose as an app or read the calls, only web pages are blocked.
* Responses are bounded (max 4 concurrent queries per app, bodies capped at 256000 characters) and the companion
  cancels the work in the app when a request times out.
* The MCP server is a small hand-written JSON-RPC implementation (protocol versions 2025-06-18, 2025-03-26, 2024-11-05).
