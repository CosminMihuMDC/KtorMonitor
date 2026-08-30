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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ro.cosminmihu.ktor.monitor.db.sqldelight.SelectCalls
import ro.cosminmihu.ktor.monitor.domain.CallsExportFormat
import ro.cosminmihu.ktor.monitor.domain.ConfigUseCase
import ro.cosminmihu.ktor.monitor.domain.DeleteCallsUseCase
import ro.cosminmihu.ktor.monitor.domain.ExportCallsUseCase
import ro.cosminmihu.ktor.monitor.domain.GetCallsUseCase
import ro.cosminmihu.ktor.monitor.core.ShareManager
import ro.cosminmihu.ktor.monitor.domain.model.ClientSource
import ro.cosminmihu.ktor.monitor.domain.model.ContentType
import ro.cosminmihu.ktor.monitor.domain.model.contentType
import ro.cosminmihu.ktor.monitor.domain.model.durationAsText
import ro.cosminmihu.ktor.monitor.domain.model.encodedPathAndQuery
import ro.cosminmihu.ktor.monitor.domain.model.host
import ro.cosminmihu.ktor.monitor.domain.model.isError
import ro.cosminmihu.ktor.monitor.domain.model.isSecure
import ro.cosminmihu.ktor.monitor.domain.model.isWebsocket
import ro.cosminmihu.ktor.monitor.domain.model.requestTimeAsText
import ro.cosminmihu.ktor.monitor.domain.model.sizeAsText
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
internal class ListViewModel(
    configUseCase: ConfigUseCase,
    getCallsUseCase: GetCallsUseCase,
    private val deleteCallsUseCase: DeleteCallsUseCase,
    private val exportCallsUseCase: ExportCallsUseCase,
    private val shareManager: ShareManager,
) : ViewModel() {

    private val _filter = MutableStateFlow(ListUiState.Filter.NoFilter)
    private val _isSelectionMode = MutableStateFlow(false)
    private val _selectedCallIds = MutableStateFlow<Set<String>>(emptySet())
    private val filter = _filter.debounce(0.2.seconds)
    private val calls = getCallsUseCase()

    val uiState = combine(
        this@ListViewModel.filter,
        calls,
        configUseCase.clientSource,
        _isSelectionMode,
        _selectedCallIds,
    ) { filterOption, calls, clientSource, isSelectionMode, selectedCallIds ->
        val (appliedFilter, filtered) = filter(filterOption, calls)
        val availableMethods = calls.map { it.method }.toSet()
        val availableHosts = calls.map { it.host }.toSet()
        val availableContentTypes = calls.mapNotNull { call ->
            when {
                call.isWebsocket -> ContentType.WEB_SOCKET
                else -> call.responseContentType?.contentType?.takeIf { it != ContentType.UNKNOWN }
            }
        }.toSet()

        buildUiState(
            filter = appliedFilter,
            calls = filtered,
            showNotification = configUseCase.isShowNotification(),
            clientSource = clientSource,
            availableMethods = availableMethods,
            availableHosts = availableHosts,
            availableContentTypes = availableContentTypes,
            totalCallsCount = calls.size,
            isSelectionMode = isSelectionMode,
            selectedCallIds = selectedCallIds,
        )
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
        var filterCalls = when {
            query.onlyError -> calls.filter { it.isError }
            else -> calls
        }

        if (query.methods.isNotEmpty()) {
            filterCalls = filterCalls.filter { it.method in query.methods }
        }

        if (query.responseCodeRanges.isNotEmpty()) {
            filterCalls = filterCalls.filter { call ->
                val code = call.responseCode?.toInt()
                code != null && query.responseCodeRanges.any { code in it.range }
            }
        }

        if (query.hosts.isNotEmpty()) {
            filterCalls = filterCalls.filter { it.host in query.hosts }
        }

        if (query.durations.isNotEmpty()) {
            filterCalls = filterCalls.filter { call ->
                val durationMs = call.responseTimestamp?.minus(call.requestTimestamp)
                durationMs != null && query.durations.any { durationMs in it.rangeMs }
            }
        }

        if (query.contentTypes.isNotEmpty()) {
            filterCalls = filterCalls.filter { call ->
                val ct = when {
                    call.isWebsocket -> ContentType.WEB_SOCKET
                    else -> call.responseContentType?.contentType ?: ContentType.UNKNOWN
                }
                ct in query.contentTypes
            }
        }

        filterCalls = when {
            query.searchQuery.isBlank() -> filterCalls
            else -> filterCalls.filter {
                it.url.contains(query.searchQuery.trim(), ignoreCase = true)
            }
        }

        filterCalls = when (query.sizeSort) {
            ListUiState.Filter.SizeSort.ASC -> filterCalls.sortedBy { it.responseContentLength ?: Long.MAX_VALUE }
            ListUiState.Filter.SizeSort.DESC -> filterCalls.sortedByDescending { it.responseContentLength ?: -1L }
            null -> filterCalls
        }

        return query to filterCalls
    }

    private fun buildUiState(
        filter: ListUiState.Filter,
        calls: List<SelectCalls>,
        showNotification: Boolean,
        clientSource: ClientSource?,
        availableMethods: Set<String> = emptySet(),
        availableHosts: Set<String> = emptySet(),
        availableContentTypes: Set<ContentType> = emptySet(),
        totalCallsCount: Int = 0,
        isSelectionMode: Boolean = false,
        selectedCallIds: Set<String> = emptySet(),
    ): ListUiState = ListUiState(
        filter = filter,
        showNotification = showNotification,
        clientSource = clientSource,
        availableMethods = availableMethods,
        availableHosts = availableHosts,
        availableContentTypes = availableContentTypes,
        totalCallsCount = totalCallsCount,
        isSelectionMode = isSelectionMode,
        selectedCallIds = selectedCallIds,
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
                    contentType = when {
                        it.isWebsocket -> ContentType.WEB_SOCKET
                        else -> it.responseContentType?.contentType ?: ContentType.UNKNOWN
                    },
                    duration = it.durationAsText ?: "",
                    size = it.responseContentLength?.sizeAsText() ?: "",
                    error = it.error ?: "",
                )
            )
        }
    )


    fun deleteSelectedCalls() {
        viewModelScope.launch {
            val selectedIds = uiState.value.selectedCallIds
            if (selectedIds.isEmpty()) return@launch

            deleteCallsUseCase(selectedIds)
            clearSelection()
        }
    }

    fun enterSelectionMode() {
        _isSelectionMode.value = true
    }

    fun clearSelection() {
        _selectedCallIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun clearSelectedCalls() {
        _selectedCallIds.value = emptySet()
        _isSelectionMode.value = true
    }

    fun toggleSelection(id: String) {
        _isSelectionMode.value = true
        _selectedCallIds.update { selected ->
            when (id in selected) {
                true -> selected - id
                false -> selected + id
            }
        }
    }

    fun selectAllVisible() {
        val visibleIds = uiState.value.calls.orEmpty().map(ListUiState.Call::id).toSet()
        if (visibleIds.isEmpty()) return

        _isSelectionMode.value = true
        _selectedCallIds.value = visibleIds
    }

    fun exportSelectedCalls() {
        viewModelScope.launch {
            val selectedIds = uiState.value.selectedCallIds.toList()
            val export = exportCallsUseCase(selectedIds, CallsExportFormat.Json) ?: return@launch
            shareManager.shareAsFile(
                content = export.content,
                name = export.fileName,
                mimeType = export.mimeType,
            )
            clearSelection()
        }
    }

    fun setSearchQuery(query: String) {
        _filter.update { it.copy(searchQuery = query) }
    }

    fun clearSearchQuery() {
        _filter.update { it.copy(searchQuery = "") }
    }

    fun resetFilter() {
        _filter.value = ListUiState.Filter.NoFilter
    }

    fun toggleOnlyError() {
        _filter.update { it.copy(onlyError = !it.onlyError) }
    }

    fun toggleMethod(method: String) {
        _filter.update { filter ->
            val updated = if (method in filter.methods) filter.methods - method else filter.methods + method
            filter.copy(methods = updated)
        }
    }

    fun toggleResponseCodeRange(range: ListUiState.Filter.ResponseCodeRange) {
        _filter.update { filter ->
            val updated = when (range) {
                in filter.responseCodeRanges -> filter.responseCodeRanges - range
                else -> filter.responseCodeRanges + range
            }
            filter.copy(responseCodeRanges = updated)
        }
    }

    fun setSizeSort(sort: ListUiState.Filter.SizeSort?) {
        _filter.update { it.copy(sizeSort = sort) }
    }

    fun toggleHost(host: String) {
        _filter.update { filter ->
            val updated = if (host in filter.hosts) filter.hosts - host else filter.hosts + host
            filter.copy(hosts = updated)
        }
    }

    fun toggleDuration(range: ListUiState.Filter.DurationRange) {
        _filter.update { filter ->
            val updated = if (range in filter.durations) filter.durations - range else filter.durations + range
            filter.copy(durations = updated)
        }
    }

    fun toggleContentType(contentType: ContentType) {
        _filter.update { filter ->
            val updated = if (contentType in filter.contentTypes) filter.contentTypes - contentType else filter.contentTypes + contentType
            filter.copy(contentTypes = updated)
        }
    }
}