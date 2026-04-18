package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ro.cosminmihu.ktor.monitor.ui.detail.transaction.Transaction

@Composable
internal fun RequestScreen(request: DetailUiState.Request, modifier: Modifier = Modifier) {
    Transaction(
        isLoading = false,
        isError = false,
        headers = request.headers,
        body = request.body,
        error = "",
        modifier = modifier,
    )
}