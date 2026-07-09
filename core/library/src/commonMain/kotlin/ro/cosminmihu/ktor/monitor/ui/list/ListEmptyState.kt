package ro.cosminmihu.ktor.monitor.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import ro.cosminmihu.ktor.monitor.ui.icons.Icons
import ro.cosminmihu.ktor.monitor.ui.icons.filled.ClearAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_list_empty
import androidx.compose.ui.tooling.preview.Preview
import ro.cosminmihu.ktor.monitor.ui.preview.UI_MODE_NIGHT_YES
import ro.cosminmihu.ktor.monitor.ui.theme.LibraryTheme

@Composable
internal fun ListEmptyState(modifier: Modifier = Modifier) {
    val string = stringResource(Res.string.ktor_list_empty)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = string },
        ) {
            Icon(
                imageVector = Icons.Default.ClearAll,
                contentDescription = null,
                modifier = Modifier.size(Dimens.ExtraLarge)
            )
            Text(
                text = string,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = Dimens.Medium)
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ListEmptyStatePreview() {
    LibraryTheme {
        ListEmptyState()
    }
}
