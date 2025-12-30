package ro.cosminmihu.ktor.monitor.sample

import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import kotlinx.coroutines.launch

private const val HTTP_BIN_URL = "https://httpbin.org"
private const val REDIRECT_URL = "https://cosminmihu.ro/"

internal suspend fun makeCalls() {
    with(httpClient()) {
        // HTTP Methods
        launch { delete("$HTTP_BIN_URL/delete") }
        launch { get("$HTTP_BIN_URL/get") }
        launch { patch("$HTTP_BIN_URL/patch") }
        launch { post("$HTTP_BIN_URL/post") }
        launch { put("$HTTP_BIN_URL/put") }

        // Auth
        launch { get("$HTTP_BIN_URL/basic-auth/user/passwd") }
        launch { get("$HTTP_BIN_URL/bearer") }
        launch { get("$HTTP_BIN_URL/digest-auth/auth/user/passwd") }
        launch { get("$HTTP_BIN_URL/digest-auth/auth/user/passwd/MD5") }
        launch { get("$HTTP_BIN_URL/digest-auth/auth/user/passwd/MD5/stale_after") }
        launch { get("$HTTP_BIN_URL/hidden-basic-auth/user/passwd") }

        // Status codes
        launch { delete("$HTTP_BIN_URL/status/200") }
        launch { get("$HTTP_BIN_URL/status/200") }
        launch { patch("$HTTP_BIN_URL/status/200") }
        launch { post("$HTTP_BIN_URL/status/200") }
        launch { put("$HTTP_BIN_URL/status/200") }

        // Request inspection
        launch { get("$HTTP_BIN_URL/headers") }
        launch { get("$HTTP_BIN_URL/ip") }
        launch { get("$HTTP_BIN_URL/user-agent") }

        // Response inspection
        launch { get("$HTTP_BIN_URL/cache") }
        launch { get("$HTTP_BIN_URL/cache/60") }
        launch { get("$HTTP_BIN_URL/etag/abc") }
        launch { get("$HTTP_BIN_URL/response-headers?freeform=") }
        launch { post("$HTTP_BIN_URL/response-headers?freeform=") }

        // Response formats
        launch { get("$HTTP_BIN_URL/brotli") }
        launch { get("$HTTP_BIN_URL/deflate") }
        launch { get("$HTTP_BIN_URL/deny") }
        launch { get("$HTTP_BIN_URL/encoding/utf8") }
        launch { get("$HTTP_BIN_URL/gzip") }
        launch { get("$HTTP_BIN_URL/html") }
        launch { get("$HTTP_BIN_URL/json") }
        launch { get("$HTTP_BIN_URL/robots.txt") }
        launch { get("$HTTP_BIN_URL/xml") }

        // Dynamic data
        launch { get("$HTTP_BIN_URL/base64/SFRUUEJJTiBpcyBhd2Vzb21l") }
        launch { get("$HTTP_BIN_URL/bytes/1024") }
        launch { delete("$HTTP_BIN_URL/delay/3") }
        launch { get("$HTTP_BIN_URL/delay/3") }
        launch { patch("$HTTP_BIN_URL/delay/3") }
        launch { post("$HTTP_BIN_URL/delay/3") }
        launch { put("$HTTP_BIN_URL/delay/3") }
        launch { get("$HTTP_BIN_URL/drip") }
        launch { get("$HTTP_BIN_URL/links/10/0") }
        launch { get("$HTTP_BIN_URL/range/1024") }
        launch { get("$HTTP_BIN_URL/stream-bytes/1024") }
        launch { get("$HTTP_BIN_URL/stream/10") }
        launch { get("$HTTP_BIN_URL/uuid") }

        // Cookies
        launch { get("$HTTP_BIN_URL/cookies") }
        launch { get("$HTTP_BIN_URL/cookies/delete") }
        launch { get("$HTTP_BIN_URL/cookies/set") }
        launch { get("$HTTP_BIN_URL/cookies/set/name/value") }

        // Images
        launch { get("$HTTP_BIN_URL/image") }
        launch { get("$HTTP_BIN_URL/image/jpeg") }
        launch { get("$HTTP_BIN_URL/image/png") }
        launch { get("$HTTP_BIN_URL/image/svg") }
        launch { get("$HTTP_BIN_URL/image/webp") }

        // Redirects
        launch { get("$HTTP_BIN_URL/absolute-redirect/3") }
        launch { delete("$HTTP_BIN_URL/redirect-to") { parameter("url", REDIRECT_URL) } }
        launch { get("$HTTP_BIN_URL/redirect-to") { parameter("url", REDIRECT_URL) } }
        launch { patch("$HTTP_BIN_URL/redirect-to") { parameter("url", REDIRECT_URL) } }
        launch { post("$HTTP_BIN_URL/redirect-to") { parameter("url", REDIRECT_URL) } }
        launch { put("$HTTP_BIN_URL/redirect-to") { formData { append("url", REDIRECT_URL) } } }
        launch { get("$HTTP_BIN_URL/redirect/3") }
        launch { get("$HTTP_BIN_URL/relative-redirect/3") }

        // Anything
        launch { delete("$HTTP_BIN_URL/anything") }
        launch { get("$HTTP_BIN_URL/anything") }
        launch { patch("$HTTP_BIN_URL/anything") }
        launch { post("$HTTP_BIN_URL/anything") }
        launch { put("$HTTP_BIN_URL/anything") }
        launch { delete("$HTTP_BIN_URL/anything/test") }
        launch { get("$HTTP_BIN_URL/anything/test") }
        launch { patch("$HTTP_BIN_URL/anything/test") }
        launch { post("$HTTP_BIN_URL/anything/test") }
        launch { put("$HTTP_BIN_URL/anything/test") }

        // Other Utilities
        launch { get("$HTTP_BIN_URL/forms/post") }
    }
}