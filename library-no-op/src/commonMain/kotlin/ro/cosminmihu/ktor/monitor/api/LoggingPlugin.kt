package ro.cosminmihu.ktor.monitor.api

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin

private const val PluginName = "KtorMonitorLogging"

internal val LoggingPlugin: ClientPlugin<LoggingConfig> =
    createClientPlugin(PluginName, ::LoggingConfig) {}