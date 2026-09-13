package ro.cosminmihu.ktor.monitor.sample

import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readUTF8Line
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.cancel
import ro.cosminmihu.ktor.monitor.sample.shared.HTTP_URL
import ro.cosminmihu.ktor.monitor.sample.shared.MARKDOWN_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.USER_AGENT
import ro.cosminmihu.ktor.monitor.sample.shared.SSE_URL
import ro.cosminmihu.ktor.monitor.sample.shared.SVG_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.TEXT_FILE_CONTENT_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.TEXT_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.WS_URL
import ro.cosminmihu.ktor.monitor.sample.shared.YAML_SAMPLE


suspend fun samples() {
    val client = httpClient()

    // HTTP Methods
    runCatching { client.get("$HTTP_URL/get") }
    runCatching { client.post("$HTTP_URL/post") { setBody("") } }
    runCatching { client.put("$HTTP_URL/put") { setBody("") } }
    runCatching { client.delete("$HTTP_URL/delete") }
    runCatching { client.patch("$HTTP_URL/patch") { setBody("") } }

    // Status codes
    runCatching { client.get("$HTTP_URL/status/200") }
    runCatching { client.post("$HTTP_URL/status/201") { setBody("") } }
    runCatching { client.put("$HTTP_URL/status/204") { setBody("") } }
    runCatching { client.patch("$HTTP_URL/status/206") { setBody("") } }
    runCatching {
        client.request("$HTTP_URL/status/302") {
            method = HttpMethod.Trace
        }
    }
    runCatching { client.get("$HTTP_URL/status/404") }
    runCatching { client.delete("$HTTP_URL/status/418") }
    runCatching { client.get("$HTTP_URL/status/500") }

    // Redirects
    runCatching { client.get("$HTTP_URL/absolute-redirect/1") }
    runCatching { client.get("$HTTP_URL/absolute-redirect/3") }
    runCatching { client.get("$HTTP_URL/redirect/1") }
    runCatching { client.get("$HTTP_URL/redirect/3") }
    runCatching { client.delete("$HTTP_URL/redirect-to?url=/get") }
    runCatching { client.get("$HTTP_URL/redirect-to?url=/get") }
    runCatching { client.patch("$HTTP_URL/redirect-to?url=/get") { setBody("") } }
    runCatching { client.post("$HTTP_URL/redirect-to?url=/get") { setBody("") } }
    runCatching { client.put("$HTTP_URL/redirect-to?url=/get") { setBody("") } }
    runCatching {
        client.request("$HTTP_URL/redirect-to?url=/get") {
            method = HttpMethod.Trace
        }
    }
    runCatching { client.get("$HTTP_URL/redirect-to?url=/get&status_code=301") }
    runCatching { client.get("$HTTP_URL/redirect-to?url=/get&status_code=302") }
    runCatching { client.get("$HTTP_URL/redirect-to?url=/get&status_code=303") }
    runCatching { client.get("$HTTP_URL/redirect-to?url=/get&status_code=307") }
    runCatching { client.get("$HTTP_URL/redirect-to?url=/get&status_code=308") }
    runCatching { client.get("$HTTP_URL/relative-redirect/1") }
    runCatching { client.get("$HTTP_URL/relative-redirect/3") }

    // Anything
    runCatching { client.delete("$HTTP_URL/anything") }
    runCatching { client.get("$HTTP_URL/anything") }
    runCatching { client.patch("$HTTP_URL/anything") { setBody("") } }
    runCatching { client.post("$HTTP_URL/anything") { setBody("") } }
    runCatching { client.put("$HTTP_URL/anything") { setBody("") } }
    runCatching {
        client.request("$HTTP_URL/anything") {
            method = HttpMethod.Trace
        }
    }
    runCatching { client.delete("$HTTP_URL/anything/nested/path") }
    runCatching { client.get("$HTTP_URL/anything/nested/path") }
    runCatching { client.patch("$HTTP_URL/anything/nested/path") { setBody("") } }
    runCatching { client.post("$HTTP_URL/anything/nested/path") { setBody("") } }
    runCatching { client.put("$HTTP_URL/anything/nested/path") { setBody("") } }
    runCatching {
        client.request("$HTTP_URL/anything/nested/path") {
            method = HttpMethod.Trace
        }
    }

    // Request inspection
    runCatching { client.get("$HTTP_URL/headers") }
    runCatching { client.get("$HTTP_URL/ip") }
    runCatching { client.get("$HTTP_URL/user-agent") }

    // Auth
    runCatching { client.get("$HTTP_URL/basic-auth/user/passwd") }
    runCatching { client.get("$HTTP_URL/bearer") }
    runCatching { client.get("$HTTP_URL/digest-auth/auth/user/passwd") }
    runCatching { client.get("$HTTP_URL/digest-auth/auth/user/passwd/MD5") }
    runCatching { client.get("$HTTP_URL/digest-auth/auth/user/passwd/MD5/never") }
    runCatching { client.get("$HTTP_URL/hidden-basic-auth/user/passwd") }

    // Response formats
    runCatching { client.get("$HTTP_URL/base64/SGVsbG8sIGh0dHBiaW4h") }
    runCatching { client.get("$HTTP_URL/deny") }
    runCatching { client.get("$HTTP_URL/encoding/utf8") }
    runCatching { client.get("$HTTP_URL/html") }
    runCatching { client.get("$HTTP_URL/json") }
    runCatching { client.get("$HTTP_URL/robots.txt") }
    runCatching { client.get("$HTTP_URL/xml") }
    runCatching {
        client.post("$HTTP_URL/anything/markdown") {
            contentType(ContentType.parse("text/markdown"))
            setBody(MARKDOWN_SAMPLE)
        }
    }
    runCatching {
        client.post("$HTTP_URL/anything/yaml") {
            contentType(ContentType.parse("application/yaml"))
            setBody(YAML_SAMPLE)
        }
    }

    // Images
    runCatching { client.get("$HTTP_URL/image") }
    runCatching { client.get("$HTTP_URL/image/jpeg") }
    runCatching { client.get("$HTTP_URL/image/png") }
    runCatching { client.get("$HTTP_URL/image/svg") }
    runCatching { client.get("$HTTP_URL/image/webp") }

    // Compression
    runCatching { client.get("$HTTP_URL/brotli") }
    runCatching { client.get("$HTTP_URL/deflate") }
    runCatching { client.get("$HTTP_URL/gzip") }

    // Cookies and cache
    runCatching { client.get("$HTTP_URL/cache") }
    runCatching { client.get("$HTTP_URL/cache/30") }
    runCatching { client.get("$HTTP_URL/cookies") }
    runCatching { client.get("$HTTP_URL/cookies/delete?theme") }
    runCatching { client.get("$HTTP_URL/cookies/set?theme=dark") }
    runCatching { client.get("$HTTP_URL/cookies/set/session/ktor-monitor") }
    runCatching { client.get("$HTTP_URL/etag/sample-etag") }

    // Dynamic data
    runCatching { client.get("$HTTP_URL/bytes/1024") }
    runCatching { client.delete("$HTTP_URL/delay/1") }
    runCatching { client.get("$HTTP_URL/delay/1") }
    runCatching { client.patch("$HTTP_URL/delay/1") { setBody("") } }
    runCatching { client.post("$HTTP_URL/delay/1") { setBody("") } }
    runCatching { client.put("$HTTP_URL/delay/1") { setBody("") } }
    runCatching {
        client.request("$HTTP_URL/delay/1") {
            method = HttpMethod.Trace
        }
    }
    runCatching { client.get("$HTTP_URL/drip?duration=1&numbytes=16&delay=0") }
    runCatching { client.get("$HTTP_URL/links/5/0") }
    runCatching { client.get("$HTTP_URL/range/256") }
    runCatching { client.get("$HTTP_URL/response-headers?X-Debug=ktor-monitor&Server=example") }
    runCatching { client.post("$HTTP_URL/response-headers?X-Debug=ktor-monitor") { setBody("") } }
    runCatching { client.get("$HTTP_URL/stream/5") }
    runCatching { client.get("$HTTP_URL/stream-bytes/256") }
    runCatching { client.get("$HTTP_URL/uuid") }

    // Form data (url-encoded)
    runCatching {
        client.post("$HTTP_URL/post") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("username", "ktor-monitor")
                        append("email", "demo@example.com")
                        append("notes", TEXT_SAMPLE)
                    }
                )
            )
        }
    }

    // Multipart / form-data
    runCatching {
        client.post("$HTTP_URL/post") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("username", "ktor-monitor")
                        append("email", "demo@example.com")
                        append(
                            key = "notes",
                            value = TEXT_SAMPLE,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "text/plain; charset=utf-8")
                            },
                        )
                        append(
                            key = "file",
                            value = TEXT_FILE_CONTENT_SAMPLE.encodeToByteArray(),
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "text/plain")
                                append(HttpHeaders.ContentDisposition, "filename=\"sample.txt\"")
                            },
                        )
                        append(
                            key = "logo",
                            value = SVG_SAMPLE,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "image/svg+xml")
                                append(HttpHeaders.ContentDisposition, "filename=\"logo.svg\"")
                            },
                        )
                    }
                )
            )
        }
    }

    // Server-Sent Events.
    runCatching {
        val response = client.get(SSE_URL) {
            headers {
                append(HttpHeaders.UserAgent, USER_AGENT)
                append(HttpHeaders.Accept, "text/event-stream")
            }
        }
        val channel = response.bodyAsChannel()
        var messages = 0
        try {
            while (!channel.isClosedForRead && messages < 3) {
                val line = channel.readUTF8Line() ?: break
                if (line.startsWith("data:")) messages++
            }
        } finally {
            channel.cancel()
            response.call.cancel()
        }
    }

    // Web Socket.
    runCatching {
        val session = client.webSocketSession(urlString = WS_URL)
        session.send(Frame.Text(TEXT_SAMPLE))
        session.close()
    }
}