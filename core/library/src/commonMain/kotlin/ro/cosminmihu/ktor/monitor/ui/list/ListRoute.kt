package ro.cosminmihu.ktor.monitor.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ListRoute(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
) {
    val viewModel: ListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ListScreen(
        modifier = modifier,
        uiState = uiState,
        toggleOnlyError = viewModel::toggleOnlyError,
        setSearchQuery = viewModel::setSearchQuery,
        clearSearchQuery = viewModel::clearSearchQuery,
        enterSelectionMode = viewModel::enterSelectionMode,
        clearSelection = viewModel::clearSelection,
        clearSelectedCalls = viewModel::clearSelectedCalls,
        toggleSelection = viewModel::toggleSelection,
        selectAllVisible = viewModel::selectAllVisible,
        exportSelectedCalls = viewModel::exportSelectedCalls,
        deleteSelectedCalls = viewModel::deleteSelectedCalls,
        onCallClick = onClick,
        toggleMethod = viewModel::toggleMethod,
        toggleResponseCodeRange = viewModel::toggleResponseCodeRange,
        setSizeSort = viewModel::setSizeSort,
        toggleHost = viewModel::toggleHost,
        toggleDuration = viewModel::toggleDuration,
        toggleContentType = viewModel::toggleContentType,
        resetFilter = viewModel::resetFilter,
    )
}