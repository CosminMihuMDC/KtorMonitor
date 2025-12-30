package ro.cosminmihu.ktor.monitor.sample

import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put

private const val HTTP_BIN_URL = "https://httpbin.org"
private const val REDIRECT_URL = "https://cosminmihu.ro/"

internal suspend fun makeCalls() {
    with(httpClient()) {
        // HTTP Methods
        runCatching { this.delete("$HTTP_BIN_URL/delete") }
        runCatching { this.get("$HTTP_BIN_URL/get") }
        runCatching { this.patch("$HTTP_BIN_URL/patch") }
        runCatching { this.post("$HTTP_BIN_URL/post") }
        runCatching { this.put("$HTTP_BIN_URL/put") }

        // Auth
        runCatching { this.get("$HTTP_BIN_URL/basic-auth/user/passwd") }
        runCatching { this.get("$HTTP_BIN_URL/bearer") }
        runCatching { this.get("$HTTP_BIN_URL/digest-auth/auth/user/passwd") }
        runCatching { this.get("$HTTP_BIN_URL/digest-auth/auth/user/passwd/MD5") }
        runCatching { this.get("$HTTP_BIN_URL/digest-auth/auth/user/passwd/MD5/stale_after") }
        runCatching { this.get("$HTTP_BIN_URL/hidden-basic-auth/user/passwd") }

        // Status codes
        runCatching { this.delete("$HTTP_BIN_URL/status/200") }
        runCatching { this.get("$HTTP_BIN_URL/status/200") }
        runCatching { this.patch("$HTTP_BIN_URL/status/200") }
        runCatching { this.post("$HTTP_BIN_URL/status/200") }
        runCatching { this.put("$HTTP_BIN_URL/status/200") }

        // Request inspection
        runCatching { this.get("$HTTP_BIN_URL/headers") }
        runCatching { this.get("$HTTP_BIN_URL/ip") }
        runCatching { this.get("$HTTP_BIN_URL/user-agent") }

        // Response inspection
        runCatching { this.get("$HTTP_BIN_URL/cache") }
        runCatching { this.get("$HTTP_BIN_URL/cache/60") }
        runCatching { this.get("$HTTP_BIN_URL/etag/abc") }
        runCatching { this.get("$HTTP_BIN_URL/response-headers?freeform=") }
        runCatching { this.post("$HTTP_BIN_URL/response-headers?freeform=") }

        // Response formats
        runCatching { this.get("$HTTP_BIN_URL/brotli") }
        runCatching { this.get("$HTTP_BIN_URL/deflate") }
        runCatching { this.get("$HTTP_BIN_URL/deny") }
        runCatching { this.get("$HTTP_BIN_URL/encoding/utf8") }
        runCatching { this.get("$HTTP_BIN_URL/gzip") }
        runCatching { this.get("$HTTP_BIN_URL/html") }
        runCatching { this.get("$HTTP_BIN_URL/json") }
        runCatching { this.get("$HTTP_BIN_URL/robots.txt") }
        runCatching { this.get("$HTTP_BIN_URL/xml") }

        // Dynamic data
        runCatching { this.get("$HTTP_BIN_URL/base64/SFRUUEJJTiBpcyBhd2Vzb21l") }
        runCatching { this.get("$HTTP_BIN_URL/bytes/1024") }
        runCatching { this.delete("$HTTP_BIN_URL/delay/3") }
        runCatching { this.get("$HTTP_BIN_URL/delay/3") }
        runCatching { this.patch("$HTTP_BIN_URL/delay/3") }
        runCatching { this.post("$HTTP_BIN_URL/delay/3") }
        runCatching { this.put("$HTTP_BIN_URL/delay/3") }
        runCatching { this.get("$HTTP_BIN_URL/drip") }
        runCatching { this.get("$HTTP_BIN_URL/links/10/0") }
        runCatching { this.get("$HTTP_BIN_URL/range/1024") }
        runCatching { this.get("$HTTP_BIN_URL/stream-bytes/1024") }
        runCatching { this.get("$HTTP_BIN_URL/stream/10") }
        runCatching { this.get("$HTTP_BIN_URL/uuid") }

        // Cookies
        runCatching { this.get("$HTTP_BIN_URL/cookies") }
        runCatching { this.get("$HTTP_BIN_URL/cookies/delete") }
        runCatching { this.get("$HTTP_BIN_URL/cookies/set") }
        runCatching { this.get("$HTTP_BIN_URL/cookies/set/name/value") }

        // Images
        runCatching { this.get("$HTTP_BIN_URL/image") }
        runCatching { this.get("$HTTP_BIN_URL/image/jpeg") }
        runCatching { this.get("$HTTP_BIN_URL/image/png") }
        runCatching { this.get("$HTTP_BIN_URL/image/svg") }
        runCatching { this.get("$HTTP_BIN_URL/image/webp") }

        // Redirects
        runCatching { this.get("$HTTP_BIN_URL/absolute-redirect/3") }
        runCatching {
            this.delete("$HTTP_BIN_URL/redirect-to") { parameter("url", REDIRECT_URL) }
        }
        runCatching {
            this.get("$HTTP_BIN_URL/redirect-to") { parameter("url", REDIRECT_URL) }
        }
        runCatching {
            this.patch("$HTTP_BIN_URL/redirect-to") { parameter("url", REDIRECT_URL) }
        }
        runCatching {
            this.post("$HTTP_BIN_URL/redirect-to") { parameter("url", REDIRECT_URL) }
        }
        runCatching {
            this.put("$HTTP_BIN_URL/redirect-to") { formData { append("url", REDIRECT_URL) } }
        }
        runCatching { this.get("$HTTP_BIN_URL/redirect/3") }
        runCatching { this.get("$HTTP_BIN_URL/relative-redirect/3") }

        // Anything
        runCatching { this.delete("$HTTP_BIN_URL/anything") }
        runCatching { this.get("$HTTP_BIN_URL/anything") }
        runCatching { this.patch("$HTTP_BIN_URL/anything") }
        runCatching { this.post("$HTTP_BIN_URL/anything") }
        runCatching { this.put("$HTTP_BIN_URL/anything") }
        runCatching { this.delete("$HTTP_BIN_URL/anything/test") }
        runCatching { this.get("$HTTP_BIN_URL/anything/test") }
        runCatching { this.patch("$HTTP_BIN_URL/anything/test") }
        runCatching { this.post("$HTTP_BIN_URL/anything/test") }
        runCatching { this.put("$HTTP_BIN_URL/anything/test") }

        // Other Utilities
        runCatching { this.get("$HTTP_BIN_URL/forms/post") }
    }
}