package ro.cosminmihu.ktor.monitor.ui.detail.body

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ro.cosminmihu.ktor.monitor.ui.Dimens

@Composable
internal fun Modifier.codeBlock(): Modifier =
    this.background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
        .padding(Dimens.Small)