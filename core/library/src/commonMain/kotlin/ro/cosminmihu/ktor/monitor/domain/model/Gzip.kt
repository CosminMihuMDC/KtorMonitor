package ro.cosminmihu.ktor.monitor.domain.model

import io.ktor.http.HttpHeaders
import io.ktor.util.GZipEncoder
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.coroutineScope
import kotlinx.io.readByteArray

internal suspend fun ByteArray.gunzip(): ByteArray? {
    if (!isGzip()) return null
    return try {
        coroutineScope {
            val inputChannel = ByteReadChannel(this@gunzip)
            val decodedChannel = GZipEncoder.decode(inputChannel, coroutineContext)
            decodedChannel.readRemaining().readByteArray()
        }
    } catch (_: Exception) {
        null
    }
}

internal suspend fun ByteArray.decodeBody(headers: Map<String, List<String>>?): String {
    val isGzipEncoded = headers?.entries?.any { (key, values) ->
        key.equals(HttpHeaders.ContentEncoding, ignoreCase = true) &&
                values.any { it.contains("gzip", ignoreCase = true) }
    } == true || isGzip()
    return if (isGzipEncoded) gunzip()?.asString() ?: asString() else asString()
}
