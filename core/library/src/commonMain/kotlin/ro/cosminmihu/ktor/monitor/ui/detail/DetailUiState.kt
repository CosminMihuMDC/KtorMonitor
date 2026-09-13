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
        val isHttpError: Boolean,
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
        val contentFormat: ContentFormat?,
        val supportsStream: Boolean = false,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Body) return false
            if (bytes != other.bytes) return false
            if (raw != other.raw) return false
            if (isTrimmed != other.isTrimmed) return false
            if (contentFormat != other.contentFormat) return false
            if (supportsStream != other.supportsStream) return false
            if (image == null) return other.image == null
            if (other.image == null) return false
            return image.contentEquals(other.image)
        }

        override fun hashCode(): Int {
            var result = bytes?.hashCode() ?: 0
            result = 31 * result + (raw?.hashCode() ?: 0)
            result = 31 * result + (image?.contentHashCode() ?: 0)
            result = 31 * result + isTrimmed.hashCode()
            result = 31 * result + (contentFormat?.hashCode() ?: 0)
            result = 31 * result + supportsStream.hashCode()
            return result
        }
    }

    enum class ClipboardCopyType {
        Url,
        Curl,
        Wget,
        Text,
    }

    enum class FileShareType {
        Text,
        Markdown,
    }

    enum class ContentFormat {
        CSS,
        FORM_URLENCODED,
        JAVASCRIPT,
        JSON,
        MARKDOWN,
        MULTIPART,
        XML,
        YAML,
    }
}

internal val DetailUiState.Response.isLoading
    get() = responseCode.isBlank() && error.isBlank()

internal val DetailUiState.Response.isError
    get() = responseCode.isBlank() && error.isNotBlank()

internal val DetailUiState.Body?.noBody
    get() = this == null || bytes.isNullOrEmpty()

internal val DetailUiState.Body.hasPreview
    get() = when {
        image != null -> true
        contentFormat == DetailUiState.ContentFormat.MARKDOWN && !raw.isNullOrBlank() -> true
        else -> false
    }

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

        ContentType.APPLICATION_JAVASCRIPT,
        ContentType.TEXT_JAVASCRIPT,
            -> DetailUiState.ContentFormat.JAVASCRIPT

        ContentType.APPLICATION_FORM_URLENCODED,
            -> DetailUiState.ContentFormat.FORM_URLENCODED

        ContentType.MULTIPART_ANY,
        ContentType.MULTIPART_MIXED,
        ContentType.MULTIPART_ALTERNATIVE,
        ContentType.MULTIPART_RELATED,
        ContentType.MULTIPART_FORM_DATA,
        ContentType.MULTIPART_SIGNED,
        ContentType.MULTIPART_ENCRYPTED,
        ContentType.MULTIPART_BYTE_RANGES,
            -> DetailUiState.ContentFormat.MULTIPART

        ContentType.APPLICATION_JSON,
        ContentType.APPLICATION_HAL_JSON,
        ContentType.APPLICATION_PROBLEM_JSON,
        ContentType.APPLICATION_VND_API_JSON,
            -> DetailUiState.ContentFormat.JSON

        ContentType.APPLICATION_MARKDOWN,
        ContentType.TEXT_MARKDOWN,
        ContentType.TEXT_X_MARKDOWN,
            -> DetailUiState.ContentFormat.MARKDOWN

        ContentType.APPLICATION_YAML,
        ContentType.APPLICATION_X_YAML,
        ContentType.TEXT_YAML,
        ContentType.TEXT_X_YAML,
            -> DetailUiState.ContentFormat.YAML

        else -> null
    }

internal fun DetailUiState.Body.hasCopyableContent(displayMode: DisplayMode): Boolean = when (displayMode) {
    DisplayMode.CODE,
    DisplayMode.RAW -> !raw.isNullOrEmpty()
    DisplayMode.BYTES -> !bytes.isNullOrEmpty()
    DisplayMode.STREAM -> !raw.isNullOrEmpty() || !bytes.isNullOrEmpty()
    DisplayMode.PREVIEW -> false
}

internal fun DetailUiState.Body.copyTextFor(displayMode: DisplayMode): String? = when (displayMode) {
    DisplayMode.CODE,
    DisplayMode.RAW -> raw?.takeIf { it.isNotEmpty() }
    DisplayMode.BYTES -> bytes?.takeIf { it.isNotEmpty() }
    DisplayMode.STREAM -> raw?.takeIf { it.isNotEmpty() } ?: bytes?.takeIf { it.isNotEmpty() }
    DisplayMode.PREVIEW -> null
}