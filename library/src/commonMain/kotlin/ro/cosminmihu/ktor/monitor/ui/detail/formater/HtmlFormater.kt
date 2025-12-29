package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.ui.text.AnnotatedString
import ro.cosminmihu.ktor.monitor.domain.model.ContentType
import ro.cosminmihu.ktor.monitor.domain.model.contentType

internal fun bodyHtml(
    contentType: String?,
    body: ByteArray?,
): AnnotatedString? {
    body ?: return null
    return when (contentType?.contentType) {
        ContentType.TEXT_HTML -> formatXml(body)
        else -> null
    }
}
