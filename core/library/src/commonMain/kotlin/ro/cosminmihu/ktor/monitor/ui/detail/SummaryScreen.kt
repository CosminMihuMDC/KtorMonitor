package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import ro.cosminmihu.ktor.monitor.ui.VerticalScrollbarBox
import ro.cosminmihu.ktor.monitor.ui.icons.Icons
import ro.cosminmihu.ktor.monitor.ui.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.Loading
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_error
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_duration
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_method
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_protocol
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_request_size
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_request_time
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_response_code
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_response_size
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_response_time
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_status
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_total_size
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary_url
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import ro.cosminmihu.ktor.monitor.ui.preview.UI_MODE_NIGHT_YES
import ro.cosminmihu.ktor.monitor.ui.theme.LibraryTheme

@Composable
internal fun SummaryScreen(summary: DetailUiState.Summary, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    VerticalScrollbarBox(scrollState, modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(
                    vertical = Dimens.Small,
                    horizontal = Dimens.Medium
                )
        ) {
        KeyValue(
            key = stringResource(Res.string.ktor_summary_url),
            value = summary.url
        )
        KeyValue(
            key = stringResource(Res.string.ktor_summary_method),
            value = summary.method,
            valueFontWeight = FontWeight.Bold
        )
        KeyValue(
            key = stringResource(Res.string.ktor_summary_protocol),
            value = summary.protocol
        )
        when {
            summary.isLoading ->
                KeyLoading(key = stringResource(Res.string.ktor_summary_status))

            summary.isError ->
                KeyError(key = stringResource(Res.string.ktor_summary_status))

            else ->
                KeyValue(
                    key = stringResource(Res.string.ktor_summary_response_code),
                    value = summary.responseCode,
                    valueFontWeight = FontWeight.Bold,
                    valueColor = when {
                        summary.isError || summary.isHttpError -> MaterialTheme.colorScheme.error
                        else -> Color.Unspecified
                    },
                )
        }
        Spacer(modifier = Modifier.padding(Dimens.Small))
        KeyValue(
            key = stringResource(Res.string.ktor_summary_request_time),
            value = summary.requestTime
        )
        KeyValue(
            key = stringResource(Res.string.ktor_summary_response_time),
            value = summary.responseTime
        )
        KeyValue(
            key = stringResource(Res.string.ktor_summary_duration),
            value = summary.duration
        )
        Spacer(modifier = Modifier.padding(Dimens.Small))
        KeyValue(
            key = stringResource(Res.string.ktor_summary_request_size),
            value = summary.requestSize
        )
        KeyValue(
            key = stringResource(Res.string.ktor_summary_response_size),
            value = summary.responseSize
        )
        KeyValue(
            key = stringResource(Res.string.ktor_summary_total_size),
            value = summary.totalSize
        )
        }
    }
}

@Composable
private fun KeyValue(
    key: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    valueFontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = key,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.7f).padding(start = Dimens.Small),
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = valueFontWeight,
        )
    }
}

@Composable
private fun KeyLoading(key: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = key,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Box(modifier = Modifier.weight(0.7f)) {
            Loading.Small()
        }
    }
}


@Composable
private fun KeyError(key: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = key,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Box(modifier = Modifier.weight(0.7f)) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = stringResource(Res.string.ktor_error),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun sampleSummary(
    slug: String,
    isLoading: Boolean = false,
    isError: Boolean = false,
    isHttpError: Boolean = false,
    isRedirect: Boolean = false,
    responseCode: String = "200 OK",
): DetailUiState.Summary = DetailUiState.Summary(
    url = "https://api.example.com/v1/$slug?limit=20",
    method = "GET",
    protocol = "HTTP/2.0",
    requestTime = "10:23:45.123",
    responseCode = responseCode,
    responseTime = "10:23:45.456",
    duration = "333 ms",
    requestSize = "0 B",
    responseSize = "1.2 KB",
    totalSize = "1.2 KB",
    isLoading = isLoading,
    isRedirect = isRedirect,
    isError = isError,
    isHttpError = isHttpError,
)

private fun String.toSlug(): String = take(24)
    .lowercase()
    .replace(Regex("[^a-z0-9]+"), "-")
    .trim('-')
    .ifEmpty { "users" }

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SummaryScreenPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    LibraryTheme {
        SummaryScreen(summary = sampleSummary(slug = lorem.toSlug()))
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SummaryScreenLoadingPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    LibraryTheme {
        SummaryScreen(summary = sampleSummary(slug = lorem.toSlug(), isLoading = true, responseCode = ""))
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SummaryScreenErrorPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    LibraryTheme {
        SummaryScreen(summary = sampleSummary(slug = lorem.toSlug(), isError = true, responseCode = ""))
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SummaryScreenHttpErrorPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    LibraryTheme {
        SummaryScreen(
            summary = sampleSummary(
                slug = lorem.toSlug(),
                isHttpError = true,
                responseCode = "500 Internal Server Error",
            ),
        )
    }
}
