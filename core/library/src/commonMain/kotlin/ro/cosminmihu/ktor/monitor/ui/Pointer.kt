package ro.cosminmihu.ktor.monitor.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

/** Hand cursor for clickable elements; falls back to the default cursor when [enabled] is false. */
internal fun Modifier.handCursor(enabled: Boolean = true): Modifier =
    pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)

/**
 * [IconButton] with a plain tooltip (hover, long-press and keyboard focus) and a hand cursor.
 * Keep the `contentDescription` on the icon: the tooltip never replaces it.
 */
@Composable
internal fun TooltipIconButton(
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        tooltip = { PlainTooltip { Text(tooltip) } },
        state = rememberTooltipState(),
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier.handCursor(),
            content = content,
        )
    }
}
