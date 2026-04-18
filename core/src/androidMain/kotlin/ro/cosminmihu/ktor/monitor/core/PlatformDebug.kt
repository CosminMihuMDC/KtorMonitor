package ro.cosminmihu.ktor.monitor.core

import ro.cosminmihu.ktor.monitor.BuildConfig

internal actual object PlatformDebug {

    actual val isDebug: Boolean = BuildConfig.DEBUG
}