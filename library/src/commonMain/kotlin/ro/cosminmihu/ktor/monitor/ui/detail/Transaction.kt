package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import io.ktor.utils.io.core.toByteArray
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.Loading
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_error
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
    Column(modifier = modifier) {

        if (isLoading) {
            Loading.Medium(
                modifier = Modifier.padding(
                    horizontal = Dimens.Medium,
                    vertical = Dimens.Small,
                )
            )
            return
        }

        var displayMode by remember(body) {
            mutableStateOf(
                when {
                    body?.html != null -> DisplayMode.HTML
                    body?.image != null -> DisplayMode.IMAGE
                    body?.code != null -> DisplayMode.CODE
                    body?.raw != null -> DisplayMode.RAW
                    else -> DisplayMode.BYTES
                }
            )
        }

        SelectionContainer {
            LazyColumn(contentPadding = PaddingValues(Dimens.Medium)) {
                item { Headers(headers) }

                if (error.isNotBlank()) {
                    item {
                        Error(error)
                    }
                    return@LazyColumn
                }

                if (body == null || body.noBody) {
                    item { NoBody() }
                } else {
                    Body(
                        body = body,
                        displayMode = displayMode,
                        onDisplayMode = { displayMode = it })
                }
            }
        }
    }
}

@Composable
internal fun Error(error: String) {
    Icon(
        imageVector = Icons.Filled.Warning,
        contentDescription = stringResource(Res.string.ktor_error),
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .padding(horizontal = Dimens.Medium)
    )
    SelectionContainer {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(
                    horizontal = Dimens.Medium,
                    vertical = Dimens.Small,
                )
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
