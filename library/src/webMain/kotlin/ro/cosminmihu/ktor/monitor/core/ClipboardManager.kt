package ro.cosminmihu.ktor.monitor.core

import kotlinx.browser.window

internal actual class ClipboardManager {

    @OptIn(ExperimentalWasmJsInterop::class)
    internal actual suspend fun setText(text: String) {
        window.navigator.clipboard.writeText(text)
    }
}