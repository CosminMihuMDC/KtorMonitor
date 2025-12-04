package ro.cosminmihu.ktor.monitor.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_in_progress
import ro.cosminmihu.ktor.monitor.ui.theme.LibraryTheme

internal object Loading {

    @Composable
    internal fun Small(modifier: Modifier = Modifier) {
        val infiniteTransition = rememberInfiniteTransition()
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
            ),
        )

        Icon(
            imageVector = Icons.Filled.HourglassTop,
            contentDescription = stringResource(Res.string.ktor_in_progress),
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = modifier.rotate(rotation),
        )
    }

    @Composable
    internal fun Medium(modifier: Modifier = Modifier) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Small()
            Text(
                text = stringResource(Res.string.ktor_in_progress),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(start = Dimens.Small),
            )
        }
    }
}

@Preview
@Composable
private fun SmallLoadingPreview() {
    LibraryTheme {
        Loading.Small()
    }
}

@Preview
@Composable
private fun MediumLoadingPreview() {
    LibraryTheme {
        Loading.Medium()
    }
}

