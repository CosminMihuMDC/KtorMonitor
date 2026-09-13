package ro.cosminmihu.ktor.monitor.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ro.cosminmihu.ktor.monitor.domain.model.Config

internal class ConfigUseCase {

    private val config = MutableStateFlow(Config.Disabled)
    internal val isActive = config.map { it.isActive }
    internal val networkClient = config.map { it.networkClient }

    internal fun setConfig(config: Config) {
        this@ConfigUseCase.config.update { config }
    }

    internal fun getRetentionPeriod() = config.value.retentionPeriod

    internal fun isShowNotification() = config.value.showNotification
}