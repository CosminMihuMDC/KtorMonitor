package ro.cosminmihu.ktor.monitor.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ro.cosminmihu.ktor.monitor.domain.ConfigUseCase
import ro.cosminmihu.ktor.monitor.domain.GetCallsUseCase
import kotlin.time.Duration.Companion.seconds

internal class MainViewModel(
    setupUseCase: ConfigUseCase,
    getCallsUseCase: GetCallsUseCase,
) : ViewModel() {

    val uiState = combine(getCallsUseCase(), setupUseCase.isActive) { calls, isActive ->
        MainUiState(isActive = calls.isNotEmpty() || isActive)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        MainUiState()
    )
}
