package ro.cosminmihu.ktor.monitor.ui.formater

import androidx.compose.ui.text.AnnotatedString
import ro.cosminmihu.ktor.monitor.domain.model.ContentType
import ro.cosminmihu.ktor.monitor.domain.model.contentType

internal fun bodyCode(
    contentType: String?,
    body: ByteArray?,
): AnnotatedString? {
    body ?: return null
    return when (contentType?.contentType) {
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
            -> formatXml(body)

        ContentType.APPLICATION_JSON,
        ContentType.APPLICATION_HAL_JSON,
        ContentType.APPLICATION_PROBLEM_JSON,
        ContentType.APPLICATION_VND_API_JSON,
            -> formatJson(body)

        ContentType.TEXT_CSS,
            -> formatCSS(body)

        ContentType.TEXT_JAVASCRIPT,
        ContentType.APPLICATION_JAVASCRIPT,
            -> formatJavascript(body)

        ContentType.APPLICATION_FORM_URLENCODED,
            -> formatQueryString(body.decodeToString())

        else -> null
    }
}