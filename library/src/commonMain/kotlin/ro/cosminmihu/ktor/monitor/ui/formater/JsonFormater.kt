package ro.cosminmihu.ktor.monitor.ui.formater

import kotlinx.serialization.json.Json

private val json = Json { isLenient = false }

internal fun bodyJson(body: ByteArray?): String? {
    if (body == null) return null
    try {
        val string = body.decodeToString()
        json.parseToJsonElement(string)
        return string
    } catch (_: Exception) {
        return null
    }
}