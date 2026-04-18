package ro.cosminmihu.ktor.monitor.core

import platform.UIKit.UIPasteboard

internal actual class ClipboardManager {

    internal actual suspend fun setText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}