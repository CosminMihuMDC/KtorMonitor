package ro.cosminmihu.ktor.monitor.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ro.cosminmihu.ktor.monitor.domain.model.Config

internal class ConfigUseCase {

    private val config = MutableStateFlow(Config.Disabled)
    internal val isActive = config.map { it.isActive }

    internal fun setConfig(config: Config) {
        this@ConfigUseCase.config.update { config }
    }

    internal suspend fun getRetentionPeriod() = config.first().retentionPeriod

    internal suspend fun isShowNotification() = config.first().showNotification

    internal fun getMaxContentLength() = config.value.maxContentLength
}