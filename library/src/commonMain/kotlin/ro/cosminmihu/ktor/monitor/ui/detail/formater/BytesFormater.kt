package ro.cosminmihu.ktor.monitor.ui.detail.formater


internal fun bodyBytes(body: ByteArray?): String? = body?.let {
    buildString {
        append(body.joinToString(" ") { byte -> byte.toString() })
    }
}