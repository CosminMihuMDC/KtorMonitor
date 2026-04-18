package ro.cosminmihu.ktor.monitor.ui.detail.headers

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_no_body
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_no_headers

@Composable
internal fun NoHeaders() {
    Text(
        text = stringResource(Res.string.ktor_no_headers),
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(
            vertical = Dimens.Small,
            horizontal = Dimens.Medium
        ),
    )
}