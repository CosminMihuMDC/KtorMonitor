package ro.cosminmihu.ktor.monitor.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ro.cosminmihu.ktor.monitor.db.sqldelight.SelectCalls
import ro.cosminmihu.ktor.monitor.domain.ConfigUseCase
import ro.cosminmihu.ktor.monitor.domain.DeleteCallsUseCase
import ro.cosminmihu.ktor.monitor.domain.GetCallsUseCase
import ro.cosminmihu.ktor.monitor.domain.model.ContentType
import ro.cosminmihu.ktor.monitor.domain.model.contentType
import ro.cosminmihu.ktor.monitor.domain.model.durationAsText
import ro.cosminmihu.ktor.monitor.domain.model.encodedPathAndQuery
import ro.cosminmihu.ktor.monitor.domain.model.host
import ro.cosminmihu.ktor.monitor.domain.model.isError
import ro.cosminmihu.ktor.monitor.domain.model.isSecure
import ro.cosminmihu.ktor.monitor.domain.model.requestTimeAsText
import ro.cosminmihu.ktor.monitor.domain.model.sizeAsText
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
internal class ListViewModel(
    configUseCase: ConfigUseCase,
    getCallsUseCase: GetCallsUseCase,
    private val deleteCallsUseCase: DeleteCallsUseCase,
) : ViewModel() {

    private val _filter = MutableStateFlow(ListUiState.Filter.NoFilter)
    private val filter = _filter.debounce(0.2.seconds)
    private val calls = getCallsUseCase()

    val uiState = combine(this@ListViewModel.filter, calls, ::filter)
        .map { (filterOption, calls) ->
            buildUiState(filterOption, calls, configUseCase.isShowNotification())
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            ListUiState()
        )

    private fun filter(
        query: ListUiState.Filter,
        calls: List<SelectCalls>
    ): Pair<ListUiState.Filter, List<SelectCalls>> {
        val filterCalls = when {
            query.onlyError -> calls.filter { it.isError }
            else -> calls
        }

        val result = when {
            query.searchQuery.isBlank() -> filterCalls
            else -> filterCalls.filter {
                it.url.contains(query.searchQuery.trim(), ignoreCase = true)
            }
        }
        return query to result
    }

    private fun buildUiState(
        filter: ListUiState.Filter,
        calls: List<SelectCalls>,
        showNotification: Boolean,
    ): ListUiState = ListUiState(
        filter = filter,
        showNotification = showNotification,
        calls = calls.map {
            ListUiState.Call(
                id = it.id,
                isSecure = it.isSecure,
                request = ListUiState.Request(
                    method = it.method,
                    host = it.host,
                    pathAndQuery = it.encodedPathAndQuery,
                    requestTime = it.requestTimeAsText,
                ),
                response = ListUiState.Response(
                    responseCode = it.responseCode?.toString() ?: "",
                    contentType = it.responseContentType?.contentType ?: ContentType.UNKNOWN,
                    duration = it.durationAsText ?: "",
                    size = it.responseContentLength?.sizeAsText() ?: "",
                    error = it.error ?: "",
                )
            )
        }
    )

    fun deleteCalls() {
        viewModelScope.launch {
            deleteCallsUseCase()
        }
    }

    fun setSearchQuery(query: String) {
        this@ListViewModel._filter.update { it.copy(searchQuery = query) }
    }

    fun clearSearchQuery() {
        this@ListViewModel._filter.update { it.copy(searchQuery = "") }
    }

    fun toggleOnlyError() {
        this@ListViewModel._filter.update { it.copy(onlyError = !it.onlyError) }
    }
}