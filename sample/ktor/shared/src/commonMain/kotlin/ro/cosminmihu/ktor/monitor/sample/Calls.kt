package ro.cosminmihu.ktor.monitor.sample

import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.contentType
import ro.cosminmihu.ktor.monitor.sample.shared.HTTP_BIN_URL
import ro.cosminmihu.ktor.monitor.sample.shared.MARKDOWN_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.SVG_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.TEXT_FILE_CONTENT_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.TEXT_SAMPLE
import ro.cosminmihu.ktor.monitor.sample.shared.YAML_SAMPLE


suspend fun samples() {
    val client = httpClient()

    // HTTP Methods
    runCatching { client.get("$HTTP_BIN_URL/get") }
    runCatching { client.post("$HTTP_BIN_URL/post") { setBody("") } }
    runCatching { client.put("$HTTP_BIN_URL/put") { setBody("") } }
    runCatching { client.delete("$HTTP_BIN_URL/delete") }
    runCatching { client.patch("$HTTP_BIN_URL/patch") { setBody("") } }

    // Status codes
    runCatching { client.get("$HTTP_BIN_URL/status/200") }
    runCatching { client.post("$HTTP_BIN_URL/status/201") { setBody("") } }
    runCatching { client.put("$HTTP_BIN_URL/status/204") { setBody("") } }
    runCatching { client.patch("$HTTP_BIN_URL/status/206") { setBody("") } }
    runCatching {
        client.request("$HTTP_BIN_URL/status/302") {
            method = HttpMethod.Trace
        }
    }
    runCatching { client.get("$HTTP_BIN_URL/status/404") }
    runCatching { client.delete("$HTTP_BIN_URL/status/418") }
    runCatching { client.get("$HTTP_BIN_URL/status/500") }

    // Redirects
    runCatching { client.get("$HTTP_BIN_URL/absolute-redirect/1") }
    runCatching { client.get("$HTTP_BIN_URL/absolute-redirect/3") }
    runCatching { client.get("$HTTP_BIN_URL/redirect/1") }
    runCatching { client.get("$HTTP_BIN_URL/redirect/3") }
    runCatching { client.delete("$HTTP_BIN_URL/redirect-to?url=/get") }
    runCatching { client.get("$HTTP_BIN_URL/redirect-to?url=/get") }
    runCatching { client.patch("$HTTP_BIN_URL/redirect-to?url=/get") { setBody("") } }
    runCatching { client.post("$HTTP_BIN_URL/redirect-to?url=/get") { setBody("") } }
    runCatching { client.put("$HTTP_BIN_URL/redirect-to?url=/get") { setBody("") } }
    runCatching {
        client.request("$HTTP_BIN_URL/redirect-to?url=/get") {
            method = HttpMethod.Trace
        }
    }
    runCatching { client.get("$HTTP_BIN_URL/redirect-to?url=/get&status_code=301") }
    runCatching { client.get("$HTTP_BIN_URL/redirect-to?url=/get&status_code=302") }
    runCatching { client.get("$HTTP_BIN_URL/redirect-to?url=/get&status_code=303") }
    runCatching { client.get("$HTTP_BIN_URL/redirect-to?url=/get&status_code=307") }
    runCatching { client.get("$HTTP_BIN_URL/redirect-to?url=/get&status_code=308") }
    runCatching { client.get("$HTTP_BIN_URL/relative-redirect/1") }
    runCatching { client.get("$HTTP_BIN_URL/relative-redirect/3") }

    // Anything
    runCatching { client.delete("$HTTP_BIN_URL/anything") }
    runCatching { client.get("$HTTP_BIN_URL/anything") }
    runCatching { client.patch("$HTTP_BIN_URL/anything") { setBody("") } }
    runCatching { client.post("$HTTP_BIN_URL/anything") { setBody("") } }
    runCatching { client.put("$HTTP_BIN_URL/anything") { setBody("") } }
    runCatching {
        client.request("$HTTP_BIN_URL/anything") {
            method = HttpMethod.Trace
        }
    }
    runCatching { client.delete("$HTTP_BIN_URL/anything/nested/path") }
    runCatching { client.get("$HTTP_BIN_URL/anything/nested/path") }
    runCatching { client.patch("$HTTP_BIN_URL/anything/nested/path") { setBody("") } }
    runCatching { client.post("$HTTP_BIN_URL/anything/nested/path") { setBody("") } }
    runCatching { client.put("$HTTP_BIN_URL/anything/nested/path") { setBody("") } }
    runCatching {
        client.request("$HTTP_BIN_URL/anything/nested/path") {
            method = HttpMethod.Trace
        }
    }

    // Request inspection
    runCatching { client.get("$HTTP_BIN_URL/headers") }
    runCatching { client.get("$HTTP_BIN_URL/ip") }
    runCatching { client.get("$HTTP_BIN_URL/user-agent") }

    // Auth
    runCatching { client.get("$HTTP_BIN_URL/basic-auth/user/passwd") }
    runCatching { client.get("$HTTP_BIN_URL/bearer") }
    runCatching { client.get("$HTTP_BIN_URL/digest-auth/auth/user/passwd") }
    runCatching { client.get("$HTTP_BIN_URL/digest-auth/auth/user/passwd/MD5") }
    runCatching { client.get("$HTTP_BIN_URL/digest-auth/auth/user/passwd/MD5/never") }
    runCatching { client.get("$HTTP_BIN_URL/hidden-basic-auth/user/passwd") }

    // Response formats
    runCatching { client.get("$HTTP_BIN_URL/base64/SGVsbG8sIGh0dHBiaW4h") }
    runCatching { client.get("$HTTP_BIN_URL/deny") }
    runCatching { client.get("$HTTP_BIN_URL/encoding/utf8") }
    runCatching { client.get("$HTTP_BIN_URL/html") }
    runCatching { client.get("$HTTP_BIN_URL/json") }
    runCatching { client.get("$HTTP_BIN_URL/robots.txt") }
    runCatching { client.get("$HTTP_BIN_URL/xml") }
    runCatching {
        client.post("$HTTP_BIN_URL/anything/markdown") {
            contentType(ContentType.parse("text/markdown"))
            setBody(MARKDOWN_SAMPLE)
        }
    }
    runCatching {
        client.post("$HTTP_BIN_URL/anything/yaml") {
            contentType(ContentType.parse("application/yaml"))
            setBody(YAML_SAMPLE)
        }
    }

    // Images
    runCatching { client.get("$HTTP_BIN_URL/image") }
    runCatching { client.get("$HTTP_BIN_URL/image/jpeg") }
    runCatching { client.get("$HTTP_BIN_URL/image/png") }
    runCatching { client.get("$HTTP_BIN_URL/image/svg") }
    runCatching { client.get("$HTTP_BIN_URL/image/webp") }

    // Compression
    runCatching { client.get("$HTTP_BIN_URL/brotli") }
    runCatching { client.get("$HTTP_BIN_URL/deflate") }
    runCatching { client.get("$HTTP_BIN_URL/gzip") }

    // Cookies and cache
    runCatching { client.get("$HTTP_BIN_URL/cache") }
    runCatching { client.get("$HTTP_BIN_URL/cache/30") }
    runCatching { client.get("$HTTP_BIN_URL/cookies") }
    runCatching { client.get("$HTTP_BIN_URL/cookies/delete?theme") }
    runCatching { client.get("$HTTP_BIN_URL/cookies/set?theme=dark") }
    runCatching { client.get("$HTTP_BIN_URL/cookies/set/session/ktor-monitor") }
    runCatching { client.get("$HTTP_BIN_URL/etag/sample-etag") }

    // Dynamic data
    runCatching { client.get("$HTTP_BIN_URL/bytes/1024") }
    runCatching { client.delete("$HTTP_BIN_URL/delay/1") }
    runCatching { client.get("$HTTP_BIN_URL/delay/1") }
    runCatching { client.patch("$HTTP_BIN_URL/delay/1") { setBody("") } }
    runCatching { client.post("$HTTP_BIN_URL/delay/1") { setBody("") } }
    runCatching { client.put("$HTTP_BIN_URL/delay/1") { setBody("") } }
    runCatching {
        client.request("$HTTP_BIN_URL/delay/1") {
            method = HttpMethod.Trace
        }
    }
    runCatching { client.get("$HTTP_BIN_URL/drip?duration=1&numbytes=16&delay=0") }
    runCatching { client.get("$HTTP_BIN_URL/links/5/0") }
    runCatching { client.get("$HTTP_BIN_URL/range/256") }
    runCatching { client.get("$HTTP_BIN_URL/response-headers?X-Debug=ktor-monitor&Server=example") }
    runCatching { client.post("$HTTP_BIN_URL/response-headers?X-Debug=ktor-monitor") { setBody("") } }
    runCatching { client.get("$HTTP_BIN_URL/stream/5") }
    runCatching { client.get("$HTTP_BIN_URL/stream-bytes/256") }
    runCatching { client.get("$HTTP_BIN_URL/uuid") }

    // Form data (url-encoded)
    runCatching {
        client.post("$HTTP_BIN_URL/post") {
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
        client.post("$HTTP_BIN_URL/post") {
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
}