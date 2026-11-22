package ro.cosminmihu.ktor.monitor.ui

import androidx.compose.runtime.saveable.Saver

internal inline fun <reified T : Enum<T>> enumSaver(): Saver<T, String> =
    Saver(save = { it.name }, restore = { enumValueOf<T>(it) })
