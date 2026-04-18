package ro.cosminmihu.ktor.monitor.core

import kotlin.experimental.ExperimentalNativeApi

internal actual object PlatformDebug {

    @OptIn(ExperimentalNativeApi::class)
    actual val isDebug: Boolean = Platform.isDebugBinary
}