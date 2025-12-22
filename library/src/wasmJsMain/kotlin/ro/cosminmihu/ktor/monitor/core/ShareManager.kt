package ro.cosminmihu.ktor.monitor.core

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

internal actual class ShareManager {

    @OptIn(ExperimentalWasmJsInterop::class)
    internal actual suspend fun shareAsFile(
        content: String,
        name: String,
        title: String?,
    ) {
        val string = content.toJsString()
        val blobParts = listOf(string).toJsArray() as JsArray<JsAny?>
        val blob = Blob(blobParts, BlobPropertyBag("text/plain"))
        val url = URL.createObjectURL(blob)

        val a = document.createElement("a") as HTMLAnchorElement
        a.href = url
        a.download = name
        document.body?.appendChild(a)
        a.click()
        a.remove()

        URL.revokeObjectURL(url)
    }
}