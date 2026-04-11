package ro.cosminmihu.ktor.monitor.ui.detail.transaction

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_error

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