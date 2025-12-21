package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_no_selection

@Composable
internal fun DetailRoute(
    id: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (id == null) {
        EmptyState(modifier)
        return
    }

    val viewModel: DetailViewModel = koinViewModel(key = id) { parametersOf(id) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailScreen(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onShare = viewModel::share,
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val string = stringResource(Res.string.ktor_no_selection)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics(true) { contentDescription = string },
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(Dimens.ExtraLarge)
            )
            Text(
                text = string,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = Dimens.Medium)
            )
        }
    }
}