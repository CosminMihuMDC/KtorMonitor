package ro.cosminmihu.ktor.monitor.sample

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put

private const val httpbin = "https://httpbin.org"

internal suspend fun makeCalls() {
    with(httpClient()) {
        // HTTP Methods
        runCatching { this.delete("$httpbin/delete") }
        runCatching { this.get("$httpbin/get") }
        runCatching { this.patch("$httpbin/patch") }
        runCatching { this.post("$httpbin/post") }
        runCatching { this.put("$httpbin/put") }

        // Auth
        runCatching { this.get("$httpbin/basic-auth/user/passwd") }
        runCatching { this.get("$httpbin/bearer") }
        runCatching { this.get("$httpbin/digest-auth/auth/user/passwd") }
        runCatching { this.get("$httpbin/digest-auth/auth/user/passwd/MD5") }
        runCatching { this.get("$httpbin/digest-auth/auth/user/passwd/MD5/stale_after") }
        runCatching { this.get("$httpbin/hidden-basic-auth/user/passwd") }

        // Status codes
        runCatching { this.delete("$httpbin/status/200") }
        runCatching { this.get("$httpbin/status/200") }
        runCatching { this.patch("$httpbin/status/200") }
        runCatching { this.post("$httpbin/status/200") }
        runCatching { this.put("$httpbin/status/200") }

        // Request inspection
        runCatching { this.get("$httpbin/headers") }
        runCatching { this.get("$httpbin/ip") }
        runCatching { this.get("$httpbin/user-agent") }

        // Response inspection
        runCatching { this.get("$httpbin/cache") }
        runCatching { this.get("$httpbin/cache/60") }
        runCatching { this.get("$httpbin/etag/abc") }
        runCatching { this.get("$httpbin/response-headers") }
        runCatching { this.post("$httpbin/call/response-headers") }

        // Response formats
        runCatching { this.get("$httpbin/brotli") }
        runCatching { this.get("$httpbin/deflate") }
        runCatching { this.get("$httpbin/deny") }
        runCatching { this.get("$httpbin/encoding/utf8") }
        runCatching { this.get("$httpbin/gzip") }
        runCatching { this.get("$httpbin/html") }
        runCatching { this.get("$httpbin/json") }
        runCatching { this.get("$httpbin/robots.txt") }
        runCatching { this.get("$httpbin/xml") }

        // Dynamic data
        runCatching { this.get("$httpbin/base64/SFRUUEJJTiBpcyBhd2Vzb21l") }
        runCatching { this.get("$httpbin/bytes/1024") }
        runCatching { this.delete("$httpbin/delay/3") }
        runCatching { this.get("$httpbin/delay/3") }
        runCatching { this.patch("$httpbin/delay/3") }
        runCatching { this.post("$httpbin/delay/3") }
        runCatching { this.put("$httpbin/delay/3") }
        runCatching { this.get("$httpbin/drip") }
        runCatching { this.get("$httpbin/links/10/0") }
        runCatching { this.get("$httpbin/range/1024") }
        runCatching { this.get("$httpbin/stream-bytes/1024") }
        runCatching { this.get("$httpbin/stream/10") }
        runCatching { this.get("$httpbin/uuid") }

        // Cookies
        runCatching { this.get("$httpbin/cookies") }
        runCatching { this.get("$httpbin/cookies/delete") }
        runCatching { this.get("$httpbin/cookies/set") }
        runCatching { this.get("$httpbin/cookies/set/name/value") }

        // Images
        runCatching { this.get("$httpbin/image") }
        runCatching { this.get("$httpbin/image/jpeg") }
        runCatching { this.get("$httpbin/image/png") }
        runCatching { this.get("$httpbin/image/svg") }
        runCatching { this.get("$httpbin/image/webp") }

        // Redirects
        runCatching { this.get("$httpbin/absolute-redirect/3") }
        runCatching { this.delete("$httpbin/redirect-to") }
        runCatching { this.get("$httpbin/redirect-to") }
        runCatching { this.patch("$httpbin/redirect-to") }
        runCatching { this.post("$httpbin/redirect-to") }
        runCatching { this.put("$httpbin/redirect-to") }
        runCatching { this.get("$httpbin/redirect/3") }
        runCatching { this.get("$httpbin/relative-redirect/3") }

        // Anything
        runCatching { this.delete("$httpbin/anything") }
        runCatching { this.get("$httpbin/anything") }
        runCatching { this.patch("$httpbin/anything") }
        runCatching { this.post("$httpbin/anything") }
        runCatching { this.put("$httpbin/anything") }
        runCatching { this.delete("$httpbin/anything/test") }
        runCatching { this.get("$httpbin/anything/test") }
        runCatching { this.patch("$httpbin/anything/test") }
        runCatching { this.post("$httpbin/anything/test") }
        runCatching { this.put("$httpbin/anything/test") }
    }
}
