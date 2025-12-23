package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import ro.cosminmihu.ktor.monitor.domain.model.ContentType

internal data class DetailUiState(
    val call: Call? = null,
    val summary: Summary? = null,
) {
    data class Call(
        val id: String,
        val isSecure: Boolean,
        val request: Request,
        val response: Response,
    )

    data class Summary(
        val url: String,
        val method: String,
        val protocol: String,
        val requestTime: String,
        val responseCode: String,
        val responseTime: String,
        val duration: String,
        val requestSize: String,
        val responseSize: String,
        val totalSize: String,
        val isLoading: Boolean,
        val isRedirect: Boolean,
        val isError: Boolean,
    )

    @Stable
    data class Request(
        val method: String,
        val host: String,
        val pathAndQuery: String,
        val requestTime: String,
        val headers: Map<String, List<String>>,
        val body: Body,
    )

    @Stable
    data class Response(
        val responseCode: String,
        val contentType: ContentType,
        val duration: String,
        val size: String,
        val error: String,
        val headers: Map<String, List<String>>,
        val body: Body,
    )

    @Stable
    data class Body(
        val bytes: AnnotatedString?,
        val raw: AnnotatedString?,
        val code: AnnotatedString?,
        val image: ByteArray?,
        val html: AnnotatedString?,
        val json: String?,
        val isTrimmed: Boolean,
    )

    enum class ClipboardCopyType {
        Url,
        Curl,
        Wget,
        Text,
    }

    enum class FileShareType {
        Text,
    }
}

internal val DetailUiState.Response.isLoading
    get() = responseCode.isBlank() && error.isBlank()

internal val DetailUiState.Response.isError
    get() = responseCode.isBlank() && error.isNotBlank()

internal val DetailUiState.Body?.noBody
    get() = this == null || bytes == null || bytes.isEmpty()
