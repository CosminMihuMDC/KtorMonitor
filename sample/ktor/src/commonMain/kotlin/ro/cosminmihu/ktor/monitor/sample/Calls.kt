package ro.cosminmihu.ktor.monitor.sample

import io.ktor.client.plugins.sse.sse
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private const val HTTP_BIN_URL = "https://httpbin.org"
private const val REDIRECT_URL = "https://cosminmihu.ro/"

internal suspend fun samples() {
    with(httpClient()) {
        // HTTP Methods
        runCatching { this@with.delete("$HTTP_BIN_URL/delete") }
        runCatching { this@with.get("$HTTP_BIN_URL/get") }
        runCatching { this@with.patch("$HTTP_BIN_URL/patch") }
        runCatching { this@with.post("$HTTP_BIN_URL/post") }
        runCatching { this@with.put("$HTTP_BIN_URL/put") }

        // Auth
        runCatching { this@with.get("$HTTP_BIN_URL/basic-auth/user/passwd") }
        runCatching { this@with.get("$HTTP_BIN_URL/bearer") }
        runCatching { this@with.get("$HTTP_BIN_URL/digest-auth/auth/user/passwd") }
        runCatching { this@with.get("$HTTP_BIN_URL/digest-auth/auth/user/passwd/MD5") }
        runCatching { this@with.get("$HTTP_BIN_URL/digest-auth/auth/user/passwd/MD5/stale_after") }
        runCatching { this@with.get("$HTTP_BIN_URL/hidden-basic-auth/user/passwd") }

        // Status codes
        runCatching { this@with.delete("$HTTP_BIN_URL/status/200") }
        runCatching { this@with.get("$HTTP_BIN_URL/status/200") }
        runCatching { this@with.patch("$HTTP_BIN_URL/status/200") }
        runCatching { this@with.post("$HTTP_BIN_URL/status/200") }
        runCatching { this@with.put("$HTTP_BIN_URL/status/200") }

        // Request inspection
        runCatching { this@with.get("$HTTP_BIN_URL/headers") }
        runCatching { this@with.get("$HTTP_BIN_URL/ip") }
        runCatching { this@with.get("$HTTP_BIN_URL/user-agent") }

        // Response inspection
        runCatching { this@with.get("$HTTP_BIN_URL/cache") }
        runCatching { this@with.get("$HTTP_BIN_URL/cache/60") }
        runCatching { this@with.get("$HTTP_BIN_URL/etag/abc") }
        runCatching { this@with.get("$HTTP_BIN_URL/response-headers?freeform=") }
        runCatching { this@with.post("$HTTP_BIN_URL/response-headers?freeform=") }

        // Response formats
        runCatching { this@with.get("$HTTP_BIN_URL/brotli") }
        runCatching { this@with.get("$HTTP_BIN_URL/deflate") }
        runCatching { this@with.get("$HTTP_BIN_URL/deny") }
        runCatching { this@with.get("$HTTP_BIN_URL/encoding/utf8") }
        runCatching { this@with.get("$HTTP_BIN_URL/gzip") }
        runCatching { this@with.get("$HTTP_BIN_URL/html") }
        runCatching { this@with.get("$HTTP_BIN_URL/json") }
        runCatching { this@with.get("$HTTP_BIN_URL/robots.txt") }
        runCatching { this@with.get("$HTTP_BIN_URL/xml") }
        runCatching { this@with.get("$HTTP_BIN_URL/links/10/0") }

        // Images
        runCatching { this@with.get("$HTTP_BIN_URL/image") }
        runCatching { this@with.get("$HTTP_BIN_URL/image/jpeg") }
        runCatching { this@with.get("$HTTP_BIN_URL/image/png") }
        runCatching { this@with.get("$HTTP_BIN_URL/image/svg") }
        runCatching { this@with.get("$HTTP_BIN_URL/image/webp") }

        // Dynamic data
        runCatching { this@with.get("$HTTP_BIN_URL/base64/SFRUUEJJTiBpcyBhd2Vzb21l") }
        runCatching { this@with.get("$HTTP_BIN_URL/bytes/1024") }
        runCatching { this@with.delete("$HTTP_BIN_URL/delay/3") }
        runCatching { this@with.get("$HTTP_BIN_URL/delay/3") }
        runCatching { this@with.patch("$HTTP_BIN_URL/delay/3") }
        runCatching { this@with.post("$HTTP_BIN_URL/delay/3") }
        runCatching { this@with.put("$HTTP_BIN_URL/delay/3") }
        runCatching { this@with.get("$HTTP_BIN_URL/drip") }
        runCatching { this@with.get("$HTTP_BIN_URL/links/10/0") }
        runCatching { this@with.get("$HTTP_BIN_URL/range/1024") }
        runCatching { this@with.get("$HTTP_BIN_URL/stream-bytes/1024") }
        runCatching { this@with.get("$HTTP_BIN_URL/stream/10") }
        runCatching { this@with.get("$HTTP_BIN_URL/uuid") }

        // Cookies
        runCatching { this@with.get("$HTTP_BIN_URL/cookies") }
        runCatching { this@with.get("$HTTP_BIN_URL/cookies/delete") }
        runCatching { this@with.get("$HTTP_BIN_URL/cookies/set") }
        runCatching { this@with.get("$HTTP_BIN_URL/cookies/set/name/value") }


        // Redirects
        runCatching { this@with.get("$HTTP_BIN_URL/absolute-redirect/3") }
        runCatching {
            this@with.delete("$HTTP_BIN_URL/redirect-to") {
                parameter(
                    "url",
                    REDIRECT_URL
                )
            }
        }
        runCatching {
            this@with.get("$HTTP_BIN_URL/redirect-to") {
                parameter(
                    "url",
                    REDIRECT_URL
                )
            }
        }
        runCatching {
            this@with.patch("$HTTP_BIN_URL/redirect-to") {
                parameter(
                    "url",
                    REDIRECT_URL
                )
            }
        }
        runCatching {
            this@with.post("$HTTP_BIN_URL/redirect-to") {
                parameter(
                    "url",
                    REDIRECT_URL
                )
            }
        }
        runCatching {
            this@with.put("$HTTP_BIN_URL/redirect-to") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build { append("url", REDIRECT_URL) }))
            }
        }
        runCatching { this@with.get("$HTTP_BIN_URL/redirect/3") }
        runCatching { this@with.get("$HTTP_BIN_URL/relative-redirect/3") }

        // Anything
        runCatching { this@with.delete("$HTTP_BIN_URL/anything") }
        runCatching { this@with.get("$HTTP_BIN_URL/anything") }
        runCatching { this@with.patch("$HTTP_BIN_URL/anything") }
        runCatching { this@with.post("$HTTP_BIN_URL/anything") }
        runCatching { this@with.put("$HTTP_BIN_URL/anything") }
        runCatching { this@with.delete("$HTTP_BIN_URL/anything/test") }
        runCatching { this@with.get("$HTTP_BIN_URL/anything/test") }
        runCatching { this@with.patch("$HTTP_BIN_URL/anything/test") }
        runCatching { this@with.post("$HTTP_BIN_URL/anything/test") }
        runCatching { this@with.put("$HTTP_BIN_URL/anything/test") }

        // Other Utilities
        runCatching { this@with.get("$HTTP_BIN_URL/forms/post") }

        // Javascript
        runCatching { this@with.get("https://code.jquery.com/jquery-3.7.1.min.js") }

        // CSS
        runCatching { this@with.get("https://www.w3schools.com/plus/plans/main.css?v=1.0.1") }

        // IGNORED
        runCatching { this@with.get("https://cosminmihu.ro/resume") }

        // Web Socket (https://websocketking.com)
        launch {
            webSocket(host = "echo.websocket.org") {
                // Receive
                launch {
                    incoming.consumeAsFlow().collect {
                        if (it is Frame.Text) {
                            println("Frame received:")
                            println(it.readText())
                            println()
                        }
                    }
                }

                // Send
                (20 downTo 0).forEach {
                    delay(1.seconds)
                    send(Frame.Text("Text Frame: $it"))
                }
            }
        }

        // Server-Sent Events (https://websocket.org/tools/websocket-echo-server)
        launch {
            sse(
                scheme = "https",
                host = "echo.websocket.org",
                path = ".sse"
            ) {
                while (true) {
                    incoming.collect { event ->
                        println("Event received:")
                        println(event)
                    }
                }
            }
        }
    }
}