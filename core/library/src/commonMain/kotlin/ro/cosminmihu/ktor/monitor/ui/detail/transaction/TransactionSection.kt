package ro.cosminmihu.ktor.monitor.ui.detail.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import ro.cosminmihu.ktor.monitor.ui.Dimens

@Composable
internal fun TransactionSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var show by rememberSaveable { mutableStateOf(true) }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(end = Dimens.Medium),
                fontStyle = FontStyle.Italic,
            )

            HorizontalDivider(modifier = Modifier.weight(1f))

            IconButton(onClick = { show = !show }) {
                Icon(
                    imageVector = when (show) {
                        true -> Icons.Default.KeyboardArrowUp
                        false -> Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(show) {
            Column {
                content()
            }
        }
    }
}