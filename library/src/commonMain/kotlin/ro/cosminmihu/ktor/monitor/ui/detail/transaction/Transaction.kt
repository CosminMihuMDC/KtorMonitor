package ro.cosminmihu.ktor.monitor.ui.detail.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import io.ktor.utils.io.core.toByteArray
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.Loading
import ro.cosminmihu.ktor.monitor.ui.detail.DetailUiState
import ro.cosminmihu.ktor.monitor.ui.detail.body.Body
import ro.cosminmihu.ktor.monitor.ui.detail.body.DisplayMode
import ro.cosminmihu.ktor.monitor.ui.detail.headers.Headers
import ro.cosminmihu.ktor.monitor.ui.theme.LibraryTheme

@Composable
internal fun Transaction(
    isLoading: Boolean,
    isError: Boolean,
    headers: Map<String, List<String>>,
    body: DetailUiState.Body?,
    error: String,
    modifier: Modifier = Modifier,
) {
    var displayMode by remember(body) {
        mutableStateOf(
            when {
                body?.image != null -> DisplayMode.IMAGE
                body?.code != null -> DisplayMode.CODE
                body?.raw != null -> DisplayMode.RAW
                else -> DisplayMode.BYTES
            }
        )
    }

    Column(
        modifier = modifier.padding(Dimens.Small)
    ) {
        if (isLoading) {
            Loading.Medium()
            return
        }

        if (error.isNotBlank()) {
            Error(error)
            return
        }

        Headers(
            headers = headers
        )
        Body(
            body = body,
            displayMode = displayMode,
            onDisplayMode = { displayMode = it }
        )
    }
}

@Preview
@Composable
private fun TransactionPreview() {
    LibraryTheme {
        Transaction(
            isLoading = false,
            isError = false,
            headers = mapOf(
                "Content-Type" to listOf("text/html"),
                "Content-Length" to listOf("1234"),
            ),
            body = DetailUiState.Body(
                json = null,
                html = AnnotatedString("<html><body><h1>Hello, World!</h1></body></html>"),
                image = null,
                code = AnnotatedString("Hello, World!"),
                raw = AnnotatedString("Hello, World!"),
                bytes = AnnotatedString("Hello, World!".toByteArray().toString()),
                isTrimmed = false,
            ),
            error = "Error message",
        )
    }
}
