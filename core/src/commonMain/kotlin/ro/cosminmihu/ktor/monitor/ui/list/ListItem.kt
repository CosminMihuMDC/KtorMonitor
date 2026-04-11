package ro.cosminmihu.ktor.monitor.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.domain.model.ContentType
import ro.cosminmihu.ktor.monitor.domain.model.asColor
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.Loading
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_error
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_in_progress
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_redirect
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_secure
import ro.cosminmihu.ktor.monitor.ui.theme.LibraryTheme

@Composable
internal fun CallItem(
    call: ListUiState.Call,
    modifier: Modifier = Modifier,
) {
    SelectionContainer(modifier = modifier) {
        Row(
            modifier = modifier.height(IntrinsicSize.Max).padding(vertical = Dimens.Small)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight().weight(0.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when {
                    call.isLoading -> Loading.Small()

                    else -> {
                        Text(
                            modifier = Modifier.padding(horizontal = Dimens.Small),
                            text = call.response.responseCode,
                            fontWeight = FontWeight.Bold,
                            color = if (call.isError) MaterialTheme.colorScheme.error else Color.Unspecified,
                        )
                        if (call.isRedirect) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = stringResource(Res.string.ktor_redirect),
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                        if (call.isError) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = stringResource(Res.string.ktor_error),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                        Text(
                            modifier = Modifier.padding(Dimens.Small),
                            text = call.response.contentType.contentName,
                            fontWeight = FontWeight.Bold,
                            color = call.response.contentType.asColor,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight().weight(0.8f)
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = Dimens.Small),
                    text = call.request.method + " " + call.request.pathAndQuery,
                    fontWeight = FontWeight.Bold,
                    color = if (call.isError) MaterialTheme.colorScheme.error else Color.Unspecified,
                )
                Row(
                    modifier = Modifier.padding(horizontal = Dimens.Small),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
                ) {
                    Icon(
                        imageVector = when {
                            call.isSecure -> Icons.Filled.Lock
                            else -> Icons.Filled.LockOpen
                        },
                        contentDescription = stringResource(Res.string.ktor_secure),
                        modifier = Modifier.size(Dimens.Medium),
                        tint = when {
                            call.isSecure -> LocalContentColor.current
                            else -> MaterialTheme.colorScheme.error
                        },
                    )
                    Text(text = call.request.host)
                }
                when {
                    call.isLoading -> Text(
                        modifier = Modifier.padding(horizontal = Dimens.Small),
                        text = stringResource(Res.string.ktor_in_progress),
                        fontStyle = FontStyle.Italic,
                    )

                    else -> {
                        Row(modifier = Modifier.padding(horizontal = Dimens.Small)) {
                            Text(
                                text = call.request.requestTime,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = call.response.duration,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = call.response.size,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (call.response.error.isNotBlank()) {
                            Text(
                                modifier = Modifier.padding(horizontal = Dimens.Small),
                                text = call.response.error,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CallItemSuccessPreview() {
    val call = ListUiState.Call(
        id = "1",
        isSecure = true,
        request = ListUiState.Request(
            method = "GET",
            host = "www.example.com",
            pathAndQuery = "/path/to/resource?param1=value1&param2=value2",
            requestTime = "2023-05-01 12:34:56",
        ),
        response = ListUiState.Response(
            responseCode = "200",
            contentType = ContentType.APPLICATION_JSON,
            duration = "123 ms",
            size = "123 KB",
            error = "",
        )
    )
    CallItem(call)
}

@Preview
@Composable
private fun CallItemFailurePreview() {
    val call = ListUiState.Call(
        id = "1",
        isSecure = true,
        request = ListUiState.Request(
            method = "GET",
            host = "www.example.com",
            pathAndQuery = "/path/to/resource?param1=value1&param2=value2",
            requestTime = "2023-05-01 12:34:56",
        ),
        response = ListUiState.Response(
            responseCode = "",
            contentType = ContentType.UNKNOWN,
            duration = "123 ms",
            size = "123 KB",
            error = "TimeoutException: Connection timed out.",
        )
    )

    LibraryTheme {
        CallItem(call)
    }
}