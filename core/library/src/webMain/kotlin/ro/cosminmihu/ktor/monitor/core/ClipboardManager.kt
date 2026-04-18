package ro.cosminmihu.ktor.monitor.core

import kotlinx.browser.window
import kotlin.js.ExperimentalWasmJsInterop

internal actual class ClipboardManager {

    @OptIn(ExperimentalWasmJsInterop::class)
    internal actual suspend fun setText(text: String) {
        window.navigator.clipboard.writeText(text)
    }
}