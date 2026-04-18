package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ro.cosminmihu.ktor.monitor.ui.detail.transaction.Transaction

@Composable
internal fun ResponseScreen(response: DetailUiState.Response, modifier: Modifier = Modifier) {
    Transaction(
        isLoading = response.isLoading,
        isError = response.isError,
        headers = response.headers,
        body = response.body,
        error = response.error,
        modifier = modifier,
    )
}
