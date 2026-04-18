package ro.cosminmihu.ktor.monitor.core

internal actual object PlatformDebug {

    /**
     * JVM does not differentiate between debug and release builds.
     */
    actual val isDebug: Boolean = true
}