package ro.cosminmihu.ktor.monitor.sample

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.http4k.core.ContentType
import org.http4k.core.Method
import org.http4k.core.MultipartFormBody
import org.http4k.core.Request
import org.http4k.lens.MultipartFormFile
import ro.cosminmihu.ktor.monitor.sample.shared.HTTP_URL
import ro.cosminmihu.ktor.monitor.sample.shared.MARKDOWN_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.WS_URL
import ro.cosminmihu.ktor.monitor.sample.shared.SVG_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.TEXT_FILE_CONTENT_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.TEXT_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.USER_AGENT
import ro.cosminmihu.ktor.monitor.sample.shared.SSE_URL
import ro.cosminmihu.ktor.monitor.sample.shared.YAML_SAMPLE


suspend fun samples() = withContext(Dispatchers.IO) {
    val client = httpClient()

    // HTTP Methods
    runCatching { client(Request(Method.GET, "$HTTP_URL/get")) }
    runCatching { client(Request(Method.POST, "$HTTP_URL/post").body("")) }
    runCatching { client(Request(Method.PUT, "$HTTP_URL/put").body("")) }
    runCatching { client(Request(Method.DELETE, "$HTTP_URL/delete")) }
    runCatching { client(Request(Method.PATCH, "$HTTP_URL/patch").body("")) }

    // Status codes
    runCatching { client(Request(Method.GET, "$HTTP_URL/status/200")) }
    runCatching { client(Request(Method.POST, "$HTTP_URL/status/201").body("")) }
    runCatching { client(Request(Method.PUT, "$HTTP_URL/status/204").body("")) }
    runCatching { client(Request(Method.PATCH, "$HTTP_URL/status/206").body("")) }
    runCatching { client(Request(Method.TRACE, "$HTTP_URL/status/302")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/status/404")) }
    runCatching { client(Request(Method.DELETE, "$HTTP_URL/status/418")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/status/500")) }

    // Redirects
    runCatching { client(Request(Method.GET, "$HTTP_URL/absolute-redirect/1")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/absolute-redirect/3")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/redirect/1")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/redirect/3")) }
    runCatching { client(Request(Method.DELETE, "$HTTP_URL/redirect-to?url=/get")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/redirect-to?url=/get")) }
    runCatching { client(Request(Method.PATCH, "$HTTP_URL/redirect-to?url=/get").body("")) }
    runCatching { client(Request(Method.POST, "$HTTP_URL/redirect-to?url=/get").body("")) }
    runCatching { client(Request(Method.PUT, "$HTTP_URL/redirect-to?url=/get").body("")) }
    runCatching { client(Request(Method.TRACE, "$HTTP_URL/redirect-to?url=/get")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/redirect-to?url=/get&status_code=301")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/redirect-to?url=/get&status_code=302")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/redirect-to?url=/get&status_code=303")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/redirect-to?url=/get&status_code=307")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/redirect-to?url=/get&status_code=308")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/relative-redirect/1")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/relative-redirect/3")) }

    // Anything
    runCatching { client(Request(Method.DELETE, "$HTTP_URL/anything")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/anything")) }
    runCatching { client(Request(Method.PATCH, "$HTTP_URL/anything").body("")) }
    runCatching { client(Request(Method.POST, "$HTTP_URL/anything").body("")) }
    runCatching { client(Request(Method.PUT, "$HTTP_URL/anything").body("")) }
    runCatching { client(Request(Method.TRACE, "$HTTP_URL/anything")) }
    runCatching { client(Request(Method.DELETE, "$HTTP_URL/anything/nested/path")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/anything/nested/path")) }
    runCatching { client(Request(Method.PATCH, "$HTTP_URL/anything/nested/path").body("")) }
    runCatching { client(Request(Method.POST, "$HTTP_URL/anything/nested/path").body("")) }
    runCatching { client(Request(Method.PUT, "$HTTP_URL/anything/nested/path").body("")) }
    runCatching { client(Request(Method.TRACE, "$HTTP_URL/anything/nested/path")) }

    // Request inspection
    runCatching { client(Request(Method.GET, "$HTTP_URL/headers")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/ip")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/user-agent")) }

    // Auth
    runCatching { client(Request(Method.GET, "$HTTP_URL/basic-auth/user/passwd")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/bearer")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/digest-auth/auth/user/passwd")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/digest-auth/auth/user/passwd/MD5")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/digest-auth/auth/user/passwd/MD5/never")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/hidden-basic-auth/user/passwd")) }

    // Response formats
    runCatching { client(Request(Method.GET, "$HTTP_URL/base64/SGVsbG8sIGh0dHBiaW4h")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/deny")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/encoding/utf8")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/html")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/json")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/robots.txt")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/xml")) }
    runCatching {
        client(
            Request(Method.POST, "$HTTP_URL/anything/markdown")
                .header("Content-Type", "text/markdown")
                .body(MARKDOWN_SAMPLE)
        )
    }
    runCatching {
        client(
            Request(Method.POST, "$HTTP_URL/anything/yaml")
                .header("Content-Type", "application/yaml")
                .body(YAML_SAMPLE)
        )
    }

    // Images
    runCatching { client(Request(Method.GET, "$HTTP_URL/image")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/image/jpeg")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/image/png")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/image/svg")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/image/webp")) }

    // Compression
    runCatching { client(Request(Method.GET, "$HTTP_URL/brotli")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/deflate")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/gzip")) }

    // Cookies and cache
    runCatching { client(Request(Method.GET, "$HTTP_URL/cache")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/cache/30")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/cookies")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/cookies/delete?theme")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/cookies/set?theme=dark")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/cookies/set/session/ktor-monitor")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/etag/sample-etag")) }

    // Dynamic data
    runCatching { client(Request(Method.GET, "$HTTP_URL/bytes/1024")) }
    runCatching { client(Request(Method.DELETE, "$HTTP_URL/delay/1")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/delay/1")) }
    runCatching { client(Request(Method.PATCH, "$HTTP_URL/delay/1").body("")) }
    runCatching { client(Request(Method.POST, "$HTTP_URL/delay/1").body("")) }
    runCatching { client(Request(Method.PUT, "$HTTP_URL/delay/1").body("")) }
    runCatching { client(Request(Method.TRACE, "$HTTP_URL/delay/1")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/drip?duration=1&numbytes=16&delay=0")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/links/5/0")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/range/256")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/response-headers?X-Debug=http4k-monitor&Server=example")) }
    runCatching { client(Request(Method.POST, "$HTTP_URL/response-headers?X-Debug=http4k-monitor").body("")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/stream/5")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/stream-bytes/256")) }
    runCatching { client(Request(Method.GET, "$HTTP_URL/uuid")) }

    // Form data (url-encoded)
    runCatching {
        client(
            Request(Method.POST, "$HTTP_URL/post")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("username=ktor-monitor&email=demo%40example.com&notes=Hello+from+Ktor+Monitor+sample")
        )
    }

    // Multipart / form-data
    runCatching {
        val body = MultipartFormBody()
            .plus("username" to "ktor-monitor")
            .plus("email" to "demo@example.com")
            .plus("notes" to TEXT_SAMPLE)
            .plus(
                "file" to MultipartFormFile(
                    "sample.txt",
                    ContentType.TEXT_PLAIN,
                    TEXT_FILE_CONTENT_SAMPLE.byteInputStream(),
                )
            )
            .plus(
                "logo" to MultipartFormFile(
                    "logo.svg",
                    ContentType("image/svg+xml"),
                    SVG_SAMPLE.inputStream(),
                )
            )

        client(
            Request(Method.POST, "$HTTP_URL/post")
                .header("Content-Type", "multipart/form-data; boundary=${body.boundary}")
                .body(body)
        )
    }

    // Server-Sent Events.
    runCatching {
        val response = client(
            Request(Method.GET, SSE_URL)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/event-stream")
        )
        try {
            response.body.stream.bufferedReader().use { reader ->
                var messages = 0
                while (messages < 3) {
                    val line = reader.readLine() ?: break
                    if (line.startsWith("data:")) messages++
                }
            }
        } finally {
            response.body.close()
        }
    }

    // Web Socket.
    runCatching {
        client(Request(Method.GET, WS_URL.replace("wss://", "https://")))
    }
}
