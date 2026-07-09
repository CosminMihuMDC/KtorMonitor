package ro.cosminmihu.ktor.monitor.ui.nav

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun rememberListDetailSceneStrategy() =
    androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy<NavKey>(
        paneExpansionState = rememberPaneExpansionState(
            anchors = listOf(
                PaneExpansionAnchor.Proportion(0.25f),
                PaneExpansionAnchor.Proportion(0.3f),
                PaneExpansionAnchor.Proportion(0.35f),
                PaneExpansionAnchor.Proportion(0.4f),
                PaneExpansionAnchor.Proportion(0.45f),
                PaneExpansionAnchor.Proportion(0.5f),
                PaneExpansionAnchor.Proportion(0.55f),
                PaneExpansionAnchor.Proportion(0.6f),
                PaneExpansionAnchor.Proportion(0.65f),
                PaneExpansionAnchor.Proportion(0.7f),
            )
        ),
        paneExpansionDragHandle = { state ->
            val interactionSource = remember { MutableInteractionSource() }
            VerticalDragHandle(
                modifier = Modifier.paneExpansionDraggable(
                    state = state,
                    minTouchTargetSize = LocalMinimumInteractiveComponentSize.current,
                    interactionSource = interactionSource,
                ),
                interactionSource = interactionSource
            )
        }
    )