package ro.cosminmihu.ktor.monitor.core

import android.content.Context
import android.content.pm.ApplicationInfo
import ro.cosminmihu.ktor.monitor.di.LibraryKoinContext

internal actual object PlatformDebug {

    actual val isDebug: Boolean
        get() = runCatching {
            val context = LibraryKoinContext.koin.get<Context>()
            context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        }.getOrDefault(false)
}