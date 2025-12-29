package ro.cosminmihu.ktor.monitor.ui.detail.formater


import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

internal fun bodyBytes(body: ByteArray?): AnnotatedString? = body?.let {
    buildAnnotatedString {
        append(body.joinToString(" ") { byte -> byte.toString() })
    }
}