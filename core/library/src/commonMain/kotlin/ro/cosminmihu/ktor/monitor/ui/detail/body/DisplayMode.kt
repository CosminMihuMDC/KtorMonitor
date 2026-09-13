package ro.cosminmihu.ktor.monitor.ui.detail.body

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.detail.DetailUiState
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_body_trimmed
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_response_view_binary
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_response_view_code
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_response_view_preview
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_response_view_raw
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_response_view_stream
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import ro.cosminmihu.ktor.monitor.ui.detail.DisplayMode
import ro.cosminmihu.ktor.monitor.ui.detail.hasPreview
import ro.cosminmihu.ktor.monitor.ui.preview.UI_MODE_NIGHT_YES
import ro.cosminmihu.ktor.monitor.ui.theme.LibraryTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DisplayModeSelector(
    body: DetailUiState.Body,
    displayMode: DisplayMode,
    onDisplayMode: (DisplayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val segmentedButtons = buildList {
        if (body.hasPreview) {
            add(
                BodyShowTypeSegment(
                    text = stringResource(Res.string.ktor_response_view_preview),
                    selected = displayMode == DisplayMode.PREVIEW,
                    onClick = { onDisplayMode(DisplayMode.PREVIEW) },
                )
            )
        }

        if (body.contentFormat != null && body.raw != null) {
            add(
                BodyShowTypeSegment(
                    text = stringResource(Res.string.ktor_response_view_code),
                    selected = displayMode == DisplayMode.CODE,
                    onClick = { onDisplayMode(DisplayMode.CODE) },
                )
            )
        }

        if (body.raw != null) {
            add(
                BodyShowTypeSegment(
                    text = stringResource(Res.string.ktor_response_view_raw),
                    selected = displayMode == DisplayMode.RAW,
                    onClick = { onDisplayMode(DisplayMode.RAW) },
                )
            )
        }

        if (body.supportsStream) {
            add(
                BodyShowTypeSegment(
                    text = stringResource(Res.string.ktor_response_view_stream),
                    selected = displayMode == DisplayMode.STREAM,
                    onClick = { onDisplayMode(DisplayMode.STREAM) },
                )
            )
        }

        if (!body.bytes.isNullOrEmpty()) {
            add(
                BodyShowTypeSegment(
                    text = stringResource(Res.string.ktor_response_view_binary),
                    selected = displayMode == DisplayMode.BYTES,
                    onClick = { onDisplayMode(DisplayMode.BYTES) },
                )
            )
        }
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        if (body.isTrimmed) {
            Text(
                text = stringResource(Res.string.ktor_body_trimmed),
                modifier = Modifier
                    .padding(vertical = Dimens.Small, horizontal = Dimens.Medium)
                    .align(Alignment.CenterStart),
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        FlowRow(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .align(Alignment.CenterEnd)
                .padding(horizontal = Dimens.Small),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            segmentedButtons.forEachIndexed { index, item ->
                ToggleButton(
                    checked = item.selected,
                    onCheckedChange = { if (it) item.onClick() },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        segmentedButtons.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    modifier = Modifier.semantics { role = Role.RadioButton },
                ) {
                    Text(text = item.text, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class BodyShowTypeSegment(
    val text: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)


@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DisplayModeSelectorPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    val sentence = lorem.take(40).replace("\"", "")
    val raw = """{"message":"$sentence"}"""
    LibraryTheme {
        DisplayModeSelector(
            body = DetailUiState.Body(
                bytes = raw.encodeToByteArray().toString(),
                raw = raw,
                image = null,
                isTrimmed = false,
                contentFormat = DetailUiState.ContentFormat.JSON,
            ),
            displayMode = DisplayMode.CODE,
            onDisplayMode = {},
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DisplayModeSelectorTrimmedPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    val raw = lorem.take(80)
    LibraryTheme {
        DisplayModeSelector(
            body = DetailUiState.Body(
                bytes = raw.encodeToByteArray().toString(),
                raw = raw,
                image = null,
                isTrimmed = true,
                contentFormat = null,
            ),
            displayMode = DisplayMode.RAW,
            onDisplayMode = {},
        )
    }
}
