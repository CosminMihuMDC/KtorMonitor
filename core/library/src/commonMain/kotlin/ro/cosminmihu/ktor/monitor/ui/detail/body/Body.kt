package ro.cosminmihu.ktor.monitor.ui.detail.body

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.svg.SvgDecoder
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.detail.DetailUiState
import ro.cosminmihu.ktor.monitor.ui.detail.DisplayMode
import ro.cosminmihu.ktor.monitor.ui.detail.copyTextFor
import ro.cosminmihu.ktor.monitor.ui.detail.formater.Css
import ro.cosminmihu.ktor.monitor.ui.detail.formater.FormUrlEncoded
import ro.cosminmihu.ktor.monitor.ui.detail.formater.HexViewer
import ro.cosminmihu.ktor.monitor.ui.detail.formater.JavaScript
import ro.cosminmihu.ktor.monitor.ui.detail.formater.JsonTree
import ro.cosminmihu.ktor.monitor.ui.detail.formater.MarkdownPreview
import ro.cosminmihu.ktor.monitor.ui.detail.formater.MarkdownHighlighter
import ro.cosminmihu.ktor.monitor.ui.detail.formater.YamlHighlighter
import ro.cosminmihu.ktor.monitor.ui.detail.formater.Multipart
import ro.cosminmihu.ktor.monitor.ui.detail.formater.TextLines
import ro.cosminmihu.ktor.monitor.ui.detail.formater.XmlTree
import ro.cosminmihu.ktor.monitor.ui.detail.hasCopyableContent
import ro.cosminmihu.ktor.monitor.ui.detail.hasPreview
import ro.cosminmihu.ktor.monitor.ui.detail.noBody
import ro.cosminmihu.ktor.monitor.ui.detail.transaction.TransactionSection
import ro.cosminmihu.ktor.monitor.ui.preview.UI_MODE_NIGHT_YES
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_body
import ro.cosminmihu.ktor.monitor.ui.theme.LibraryTheme

@Composable
internal fun Body(
    body: DetailUiState.Body?,
    displayMode: DisplayMode,
    onDisplayMode: (DisplayMode) -> Unit,
) {
    TransactionSection(
        title = stringResource(Res.string.ktor_body),
        copyText = when {
            body?.hasCopyableContent(displayMode) == true -> {
                { body.copyTextFor(displayMode).orEmpty() }
            }

            else -> null
        },
        contentEnter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.97f),
        contentExit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.97f),
    ) {
        if (body == null || body.noBody) {
            NoBody()
            return@TransactionSection
        }

        DisplayModeSelector(
            body = body,
            displayMode = displayMode,
            onDisplayMode = onDisplayMode,
        )

        when {
            body.image != null && displayMode == DisplayMode.PREVIEW -> {
                val context = LocalPlatformContext.current
                val imageLoader = remember(context) {
                    ImageLoader.Builder(context)
                        .components { add(SvgDecoder.Factory()) }
                        .build()
                }
                AsyncImage(
                    model = body.image,
                    imageLoader = imageLoader,
                    contentDescription = null,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                )
            }

            body.hasPreview &&
                    body.contentFormat == DetailUiState.ContentFormat.MARKDOWN &&
                    displayMode == DisplayMode.PREVIEW &&
                    body.raw != null ->
                MarkdownPreview(
                    markdown = body.raw,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )

            body.contentFormat == DetailUiState.ContentFormat.CSS && displayMode == DisplayMode.CODE && body.raw != null ->
                Css(
                    css = body.raw,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )

            body.contentFormat == DetailUiState.ContentFormat.FORM_URLENCODED && displayMode == DisplayMode.CODE && body.raw != null ->
                FormUrlEncoded(
                    body = body.raw,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )

            body.contentFormat == DetailUiState.ContentFormat.MULTIPART && displayMode == DisplayMode.CODE && body.raw != null ->
                Multipart(
                    body = body.raw,
                    bytes = body.bytes,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )

            body.contentFormat == DetailUiState.ContentFormat.JAVASCRIPT && displayMode == DisplayMode.CODE && body.raw != null ->
                JavaScript(
                    code = body.raw,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )

            body.contentFormat == DetailUiState.ContentFormat.JSON && displayMode == DisplayMode.CODE && body.raw != null ->
                JsonTree(
                    json = body.raw,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )

            body.contentFormat == DetailUiState.ContentFormat.XML && displayMode == DisplayMode.CODE && body.raw != null ->
                XmlTree(
                    xml = body.raw,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )

            body.contentFormat == DetailUiState.ContentFormat.MARKDOWN && displayMode == DisplayMode.CODE && body.raw != null ->
                MarkdownHighlighter(
                    markdown = body.raw,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )

            body.contentFormat == DetailUiState.ContentFormat.YAML && displayMode == DisplayMode.CODE && body.raw != null ->
                YamlHighlighter(
                    yaml = body.raw,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )

            body.raw != null && displayMode == DisplayMode.RAW ->
                TextLines(
                    text = body.raw,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                    showLineNumbers = true,
                )

            body.raw != null && displayMode == DisplayMode.STREAM ->
                TextLines(
                    text = body.raw,
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                    showLineNumbers = true,
                )

            !body.bytes.isNullOrEmpty() && displayMode == DisplayMode.STREAM ->
                HexViewer(
                    bytes = body.bytes,
                    modifier = Modifier.fillMaxHeight().fillMaxWidth().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )

            !body.bytes.isNullOrEmpty() && displayMode == DisplayMode.BYTES ->
                HexViewer(
                    bytes = body.bytes,
                    modifier = Modifier.fillMaxHeight().fillMaxWidth().codeBlock(),
                    contentPadding = PaddingValues(Dimens.Small),
                )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BodyPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    val raw = lorem.take(160)
    LibraryTheme {
        Body(
            body = DetailUiState.Body(
                bytes = raw.encodeToByteArray().toString(),
                raw = raw,
                image = null,
                isTrimmed = false,
                contentFormat = null,
            ),
            displayMode = DisplayMode.RAW,
            onDisplayMode = {},
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BodyJsonPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    val sentence = lorem.take(40).replace("\"", "")
    val raw = """{"title":"$sentence","value":42}"""
    LibraryTheme {
        Body(
            body = DetailUiState.Body(
                bytes = raw.encodeToByteArray().toString(),
                raw = raw,
                image = null,
                isTrimmed = true,
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
private fun BodyEmptyPreview() {
    LibraryTheme {
        Body(
            body = null,
            displayMode = DisplayMode.RAW,
            onDisplayMode = {},
        )
    }
}
