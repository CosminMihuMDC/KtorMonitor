package ro.cosminmihu.ktor.monitor.api

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.observer.ResponseHandler
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import ro.cosminmihu.ktor.monitor.SanitizedHeader
import ro.cosminmihu.ktor.monitor.api.util.ReceiveHook
import ro.cosminmihu.ktor.monitor.api.util.ResponseHook
import ro.cosminmihu.ktor.monitor.api.util.SendHook
import ro.cosminmihu.ktor.monitor.api.util.logRequest
import ro.cosminmihu.ktor.monitor.api.util.logRequestException
import ro.cosminmihu.ktor.monitor.api.util.logResponse
import ro.cosminmihu.ktor.monitor.api.util.logResponseBody
import ro.cosminmihu.ktor.monitor.api.util.logResponseException
import ro.cosminmihu.ktor.monitor.db.LibraryDao
import ro.cosminmihu.ktor.monitor.di.LibraryKoinContext
import ro.cosminmihu.ktor.monitor.di.inject
import ro.cosminmihu.ktor.monitor.domain.ConfigUseCase
import ro.cosminmihu.ktor.monitor.domain.ListenByRecentCallsUseCase
import ro.cosminmihu.ktor.monitor.domain.model.Config

private val DisableLogging = AttributeKey<Unit>("KtorMonitorDisableLogging")
private val CallIdentifier = AttributeKey<String>("KtorMonitorCallIdentifier")
private const val PluginName = "KtorMonitorLogging"

internal val LoggingPlugin: ClientPlugin<LoggingConfig> =
    createClientPlugin(PluginName, ::LoggingConfig) {
        // Setup library dependency.
        val config = Config(
            isActive = pluginConfig.isActive,
            showNotification = pluginConfig.showNotification,
            retentionPeriod = pluginConfig.retentionPeriod,
            maxContentLength = pluginConfig.maxContentLength,
        )
        LibraryKoinContext.koin.get<ConfigUseCase>().setConfig(config)

        // Check if plugin is active.
        if (!pluginConfig.isActive) return@createClientPlugin
        // Check if retention period is zero.
        if (!pluginConfig.retentionPeriod.isPositive()) return@createClientPlugin

        // Plugin configuration.
        val filters: List<(HttpRequestBuilder) -> Boolean> = pluginConfig.filters
        val sanitizedHeaders: List<SanitizedHeader> = pluginConfig.sanitizedHeaders

        // Start listen by recent calls.
        LibraryKoinContext.koin.get<ListenByRecentCallsUseCase>()()

        // Get library dependencies.
        val dao: LibraryDao by LibraryKoinContext.inject<LibraryDao>()
        val coroutineScope by LibraryKoinContext.inject<CoroutineScope>()

        // Filter out requests that should not be logged.
        fun shouldBeLogged(request: HttpRequestBuilder): Boolean =
            filters.isEmpty() || filters.any { it(request) }

        on(SendHook) { request ->
            // Disable logging for requests that should not be logged.
            if (!shouldBeLogged(request)) {
                request.attributes.put(DisableLogging, Unit)
                return@on
            }

            // Generate id.
            request.attributes.put(CallIdentifier, callIdentifier)

            // Log request.
            val loggedRequest = try {
                logRequest(
                    dao = dao,
                    id = request.attributes[CallIdentifier],
                    request = request,
                    maxContentLength = pluginConfig.maxContentLength,
                    coroutineScope = coroutineScope,
                    sanitizedHeaders = sanitizedHeaders
                )
            } catch (_: Throwable) {
                null
            }

            // Proceed with request.
            try {
                proceedWith(loggedRequest ?: request.body)
            } catch (cause: Throwable) {
                logRequestException(
                    dao = dao,
                    id = request.attributes[CallIdentifier],
                    cause = cause
                )
                throw cause
            }
        }

        on(ResponseHook) { response ->
            if (response.call.attributes.contains(DisableLogging)) return@on

            try {
                // Log response.
                logResponse(
                    dao = dao,
                    id = response.call.attributes[CallIdentifier],
                    response = response,
                    sanitizedHeaders = sanitizedHeaders
                )
                proceed()
            } catch (cause: Throwable) {
                // Log response exception.
                logResponseException(
                    dao = dao,
                    id = response.call.attributes[CallIdentifier],
                    cause = cause
                )
                throw cause
            }
        }

        on(ReceiveHook) { call ->
            if (call.attributes.contains(DisableLogging)) {
                return@on
            }

            try {
                proceed()
            } catch (cause: Throwable) {
                // Log response exception.
                logResponseException(
                    dao = dao,
                    id = call.attributes[CallIdentifier],
                    cause = cause
                )
                throw cause
            }
        }

        val responseObserver: ResponseHandler = observer@{ response ->
            if (response.call.attributes.contains(DisableLogging)) {
                return@observer
            }

            // Log response body.
            logResponseBody(
                dao = dao,
                id = response.call.attributes[CallIdentifier],
                maxContentLength = pluginConfig.maxContentLength,
                response = response,
            )
        }

        ResponseObserver.install(ResponseObserver.prepare { onResponse(responseObserver) }, client)
    }
