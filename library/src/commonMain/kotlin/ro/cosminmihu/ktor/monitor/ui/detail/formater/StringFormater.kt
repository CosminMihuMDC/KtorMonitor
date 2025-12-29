package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.ui.text.buildAnnotatedString

internal fun bodyString(body: ByteArray?) = buildAnnotatedString {
    append(body?.decodeToString())
}