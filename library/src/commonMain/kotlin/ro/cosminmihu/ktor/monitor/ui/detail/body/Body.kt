package ro.cosminmihu.ktor.monitor.ui.detail.body

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.svg.SvgDecoder
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontree.defaultLightColors
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.detail.DetailUiState
import ro.cosminmihu.ktor.monitor.ui.detail.formater.Css
import ro.cosminmihu.ktor.monitor.ui.detail.formater.FormUrlEncoded
import ro.cosminmihu.ktor.monitor.ui.detail.formater.Text
import ro.cosminmihu.ktor.monitor.ui.detail.formater.XmlTree
import ro.cosminmihu.ktor.monitor.ui.detail.noBody
import ro.cosminmihu.ktor.monitor.ui.detail.transaction.TransactionSection
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_body

@Composable
internal fun Body(
    body: DetailUiState.Body?,
    displayMode: DisplayMode,
    onDisplayMode: (DisplayMode) -> Unit,
) {
    TransactionSection(title = stringResource(Res.string.ktor_body)) {
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
            body.image != null && displayMode == DisplayMode.IMAGE ->
                AsyncImage(
                    model = body.image,
                    imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
                        .components { add(SvgDecoder.Factory()) }
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                )

            body.contentFormat == DetailUiState.ContentFormat.CSS && displayMode == DisplayMode.CODE ->
                Css(
                    css = body.raw ?: "",
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                )

            body.contentFormat == DetailUiState.ContentFormat.FORM_URLENCODED && displayMode == DisplayMode.CODE ->
                FormUrlEncoded(
                    body = body.raw ?: "",
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                )

            body.contentFormat == DetailUiState.ContentFormat.JSON && displayMode == DisplayMode.CODE ->
                JsonTree(
                    json = body.raw ?: "",
                    onLoading = {},
                    colors = defaultLightColors.copy(
                        symbolColor = Color(0xFF2E86C1),
                        iconColor = Color(0xFF2E86C1),
                    ),
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                )

            body.contentFormat == DetailUiState.ContentFormat.XML && displayMode == DisplayMode.CODE ->
                XmlTree(
                    xml = body.raw ?: "",
                    modifier = Modifier.fillMaxHeight().codeBlock(),
                )

            body.raw != null && displayMode == DisplayMode.RAW ->
                Text(text = body.raw)

            !body.bytes.isNullOrEmpty() && displayMode == DisplayMode.BYTES ->
                Text(text = body.bytes)
        }
    }
}
