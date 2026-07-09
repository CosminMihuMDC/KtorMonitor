package ro.cosminmihu.ktor.monitor.ui.detail.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import ro.cosminmihu.ktor.monitor.ui.icons.Icons
import ro.cosminmihu.ktor.monitor.ui.icons.filled.ContentCopy
import ro.cosminmihu.ktor.monitor.ui.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import ro.cosminmihu.ktor.monitor.core.ClipboardManager
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_copy

@Composable
internal fun TransactionSection(
    title: String,
    modifier: Modifier = Modifier,
    copyText: (() -> String)? = null,
    contentEnter: EnterTransition = fadeIn() + expandVertically(),
    contentExit: ExitTransition = fadeOut() + shrinkVertically(),
    content: @Composable () -> Unit
) {
    var show by rememberSaveable { mutableStateOf(true) }
    val clipboard = koinInject<ClipboardManager>()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(end = Dimens.Medium),
                fontStyle = FontStyle.Italic,
            )

            HorizontalDivider(modifier = Modifier.weight(1f))

            if (copyText != null) {
                AnimatedVisibility(
                    visible = show,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                ) {
                    TextButton(
                        onClick = { scope.launch { clipboard.setText(copyText()) } },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.Medium),
                        )
                        Spacer(modifier = Modifier.width(Dimens.ExtraSmall))
                        Text(text = stringResource(Res.string.ktor_copy))
                    }
                }
            }

            IconButton(onClick = { show = !show }) {
                val rotation by animateFloatAsState(targetValue = if (show) 180f else 0f)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                )
            }
        }

        AnimatedVisibility(
            visible = show,
            enter = contentEnter,
            exit = contentExit,
        ) {
            Column {
                content()
            }
        }
    }
}