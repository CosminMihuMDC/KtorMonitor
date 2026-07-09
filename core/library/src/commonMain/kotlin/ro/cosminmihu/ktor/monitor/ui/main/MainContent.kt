package ro.cosminmihu.ktor.monitor.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
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
    val listDetailStrategy = rememberListDetailSceneStrategy()

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
                                onBack = {},
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    )
                ) {
                    ListRoute(
                        onClick = { id ->
                            backStack.removeAll { it is Nav.Detail }
                            backStack.add(Nav.Detail(id))
                        },
                        onClear = {
                            backStack.removeAll { it is Nav.Detail }
                        }
                    )
                }
                entry<Nav.Detail>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { destination ->
                    DetailRoute(
                        id = destination.id,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}