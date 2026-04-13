package ro.cosminmihu.ktor.monitor.ui.detail.body

import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val MIN_HEIGHT = 180.dp
private val MAX_HEIGHT = 480.dp

@Composable
internal fun Modifier.transactionBodyBounds(): Modifier = heightIn(MIN_HEIGHT, MAX_HEIGHT)