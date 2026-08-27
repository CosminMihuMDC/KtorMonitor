package ro.cosminmihu.ktor.monitor.sample

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ro.cosminmihu.ktor.monitor.sample.shared.HTTP_BIN_URL
import ro.cosminmihu.ktor.monitor.sample.shared.MARKDOWN_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.SVG_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.TEXT_FILE_CONTENT_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.TEXT_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.YAML_SAMPLE


suspend fun samples() = withContext(Dispatchers.IO) {
    val client = httpClient()

    // HTTP Methods
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/get").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/post").post("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/put").put("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/delete").delete().build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/patch").patch("".toRequestBody(null)).build()).execute() }

    // Status codes
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/status/200").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/status/201").post("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/status/204").put("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/status/206").patch("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/status/302").method("TRACE", null).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/status/404").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/status/418").delete().build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/status/500").build()).execute() }

    // Redirects
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/absolute-redirect/1").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/absolute-redirect/3").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect/1").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect/3").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get").delete().build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get").patch("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get").post("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get").put("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get").method("TRACE", null).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get&status_code=301").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get&status_code=302").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get&status_code=303").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get&status_code=307").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/redirect-to?url=/get&status_code=308").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/relative-redirect/1").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/relative-redirect/3").build()).execute() }

    // Anything
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything").delete().build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything").patch("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything").post("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything").put("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything").method("TRACE", null).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything/nested/path").delete().build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything/nested/path").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything/nested/path").patch("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything/nested/path").post("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything/nested/path").put("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/anything/nested/path").method("TRACE", null).build()).execute() }

    // Request inspection
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/headers").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/ip").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/user-agent").build()).execute() }

    // Auth
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/basic-auth/user/passwd").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/bearer").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/digest-auth/auth/user/passwd").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/digest-auth/auth/user/passwd/MD5").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/digest-auth/auth/user/passwd/MD5/never").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/hidden-basic-auth/user/passwd").build()).execute() }

    // Response formats
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/base64/SGVsbG8sIGh0dHBiaW4h").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/deny").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/encoding/utf8").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/html").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/json").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/robots.txt").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/xml").build()).execute() }
    runCatching {
        client.newCall(
            Request.Builder()
                .url("$HTTP_BIN_URL/anything/markdown")
                .post(MARKDOWN_SAMPLE.toRequestBody("text/markdown".toMediaType()))
                .build()
        ).execute()
    }
    runCatching {
        client.newCall(
            Request.Builder()
                .url("$HTTP_BIN_URL/anything/yaml")
                .post(YAML_SAMPLE.toRequestBody("application/yaml".toMediaType()))
                .build()
        ).execute()
    }

    // Images
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/image").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/image/jpeg").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/image/png").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/image/svg").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/image/webp").build()).execute() }

    // Compression
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/brotli").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/deflate").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/gzip").build()).execute() }

    // Cookies and cache
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/cache").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/cache/30").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/cookies").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/cookies/delete?theme").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/cookies/set?theme=dark").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/cookies/set/session/ktor-monitor").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/etag/sample-etag").build()).execute() }

    // Dynamic data
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/bytes/1024").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/delay/1").delete().build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/delay/1").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/delay/1").patch("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/delay/1").post("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/delay/1").put("".toRequestBody(null)).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/delay/1").method("TRACE", null).build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/drip?duration=1&numbytes=16&delay=0").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/links/5/0").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/range/256").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/response-headers?X-Debug=okhttp-monitor&Server=example").build()).execute() }
    runCatching {
        client.newCall(
            Request.Builder()
                .url("$HTTP_BIN_URL/response-headers?X-Debug=okhttp-monitor")
                .post("".toRequestBody(null))
                .build()
        ).execute()
    }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/stream/5").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/stream-bytes/256").build()).execute() }
    runCatching { client.newCall(Request.Builder().url("$HTTP_BIN_URL/uuid").build()).execute() }

    // Form data (url-encoded)
    runCatching {
        client.newCall(
            Request.Builder()
                .url("$HTTP_BIN_URL/post")
                .post(
                    "username=ktor-monitor&email=demo%40example.com&notes=Hello+from+Ktor+Monitor+sample"
                        .toRequestBody("application/x-www-form-urlencoded".toMediaType())
                )
                .build()
        ).execute()
    }

    // Multipart / form-data
    runCatching {
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("username", "ktor-monitor")
            .addFormDataPart("email", "demo@example.com")
            .addFormDataPart("notes", TEXT_SAMPLE)
            .addFormDataPart(
                "file",
                "sample.txt",
                TEXT_FILE_CONTENT_SAMPLE.toRequestBody("text/plain".toMediaType()),
            )
            .addFormDataPart(
                "logo",
                "logo.svg",
                SVG_SAMPLE.toRequestBody("image/svg+xml".toMediaType()),
            )
            .build()
        client.newCall(
            Request.Builder().url("$HTTP_BIN_URL/post").post(multipart).build()
        ).execute()
    }
}
