package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.runtime.Stable
import ro.cosminmihu.ktor.monitor.domain.model.ContentType
import ro.cosminmihu.ktor.monitor.domain.model.contentType

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
        val bytes: String?,
        val raw: String?,
        val image: ByteArray?,
        val isTrimmed: Boolean,
        val contentFormat: ContentFormat?
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

    enum class ContentFormat {
        CSS,
        FORM_URLENCODED,
        JSON,
        XML,
    }
}

internal val DetailUiState.Response.isLoading
    get() = responseCode.isBlank() && error.isBlank()

internal val DetailUiState.Response.isError
    get() = responseCode.isBlank() && error.isNotBlank()

internal val DetailUiState.Body?.noBody
    get() = this == null || bytes.isNullOrEmpty()

internal val ContentType.contentFormat
    get() = when (contentType.contentType) {
        ContentType.TEXT_HTML,
        ContentType.TEXT_XML,
        ContentType.APPLICATION_ATOM,
        ContentType.APPLICATION_XML,
        ContentType.APPLICATION_XML_DTD,
        ContentType.APPLICATION_XAML,
        ContentType.APPLICATION_RSS,
        ContentType.APPLICATION_SOAP,
        ContentType.APPLICATION_PROBLEM_XML,
        ContentType.IMAGE_SVG,
            -> DetailUiState.ContentFormat.XML

        ContentType.TEXT_CSS,
            -> DetailUiState.ContentFormat.CSS

        ContentType.APPLICATION_FORM_URLENCODED,
            -> DetailUiState.ContentFormat.FORM_URLENCODED

        ContentType.APPLICATION_JSON,
        ContentType.APPLICATION_HAL_JSON,
        ContentType.APPLICATION_PROBLEM_JSON,
        ContentType.APPLICATION_VND_API_JSON,
            -> DetailUiState.ContentFormat.JSON

        else -> null
    }