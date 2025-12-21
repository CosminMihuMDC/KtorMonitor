package ro.cosminmihu.ktor.monitor.ui.main

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import kotlinx.coroutines.launch
import ro.cosminmihu.ktor.monitor.ui.detail.DetailRoute
import ro.cosminmihu.ktor.monitor.ui.list.ListRoute

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun MainContent(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<String?>()

    BackHandler(navigator.canNavigateBack()) {
        coroutineScope.launch {
            navigator.navigateBack()
        }
    }

    Surface(modifier = modifier) {
        ListDetailPaneScaffold(
            modifier = Modifier.fillMaxSize(),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    ListRoute { id ->
                        coroutineScope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                        }
                    }
                }
            },
            detailPane = {
                val id = navigator.currentDestination?.contentKey
                AnimatedPane {
                    DetailRoute(
                        id = id,
                        onBack = {
                            coroutineScope.launch {
                                navigator.navigateBack()
                            }
                        }
                    )
                }
            },
            paneExpansionState = rememberPaneExpansionState(
                keyProvider = navigator.scaffoldValue,
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
    }
}