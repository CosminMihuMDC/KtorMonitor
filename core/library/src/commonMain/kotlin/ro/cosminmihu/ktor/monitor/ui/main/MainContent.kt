package ro.cosminmihu.ktor.monitor.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ro.cosminmihu.ktor.monitor.ui.detail.DetailRoute
import ro.cosminmihu.ktor.monitor.ui.list.ListRoute
import ro.cosminmihu.ktor.monitor.ui.nav.Nav
import ro.cosminmihu.ktor.monitor.ui.nav.backStackConfig
import ro.cosminmihu.ktor.monitor.ui.nav.rememberListDetailSceneStrategy

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun MainContent(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(backStackConfig, Nav.List)
    var selectionCount by rememberSaveable { mutableStateOf<Int?>(null) }
    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val isTwoPane = directive.maxHorizontalPartitions > 1
    val listDetailStrategy = rememberListDetailSceneStrategy(directive)

    Surface(modifier = modifier) {
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            sceneStrategies = listOf(listDetailStrategy),
            entryProvider = entryProvider {
                entry<Nav.List>(
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = {
                            DetailRoute(
                                id = null,
                                selectionCount = selectionCount,
                                showBack = false,
                                onBack = {},
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    )
                ) {
                    ListRoute(
                        onSelectionCountChange = { selectionCount = it },
                        onClick = { id ->
                            backStack.removeAll { it is Nav.Detail }
                            backStack.add(Nav.Detail(id))
                        }
                    )
                }
                entry<Nav.Detail>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { destination ->
                    DetailRoute(
                        id = destination.id,
                        selectionCount = selectionCount,
                        showBack = !isTwoPane,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}