package ro.cosminmihu.ktor.monitor.ui.detail.formater

internal fun bodyString(body: ByteArray?) = buildString {
    append(body?.decodeToString())
}