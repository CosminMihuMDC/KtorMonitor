package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
internal fun SummaryScreen(summary: DetailUiState.Summary, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(
            vertical = Dimens.Small,
            horizontal = Dimens.Medium
        )
    ) {
        KeyValue(key = stringResource(Res.string.ktor_summary_url), value = summary.url)
        KeyValue(key = stringResource(Res.string.ktor_summary_method), value = summary.method)
        KeyValue(key = stringResource(Res.string.ktor_summary_protocol), value = summary.protocol)
        when {
            summary.isLoading ->
                KeyLoading(key = stringResource(Res.string.ktor_summary_status))

            summary.isError ->
                KeyError(key = stringResource(Res.string.ktor_summary_status))

            else ->
                KeyValue(
                    key = stringResource(Res.string.ktor_summary_response_code),
                    value = summary.responseCode
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

@Composable
private fun KeyValue(key: String, value: String) {
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