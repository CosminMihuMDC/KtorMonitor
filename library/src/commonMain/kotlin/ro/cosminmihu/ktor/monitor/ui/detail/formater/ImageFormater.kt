package ro.cosminmihu.ktor.monitor.ui.detail.formater

import ro.cosminmihu.ktor.monitor.domain.model.ContentType
import ro.cosminmihu.ktor.monitor.domain.model.contentType

internal fun bodyImage(contentType: String?, body: ByteArray?) = when (contentType?.contentType) {
    null -> null
    ContentType.IMAGE_PNG -> body
    ContentType.IMAGE_JPEG -> body
    ContentType.IMAGE_WEBP -> body
    ContentType.IMAGE_GIF -> body
    else -> null
}