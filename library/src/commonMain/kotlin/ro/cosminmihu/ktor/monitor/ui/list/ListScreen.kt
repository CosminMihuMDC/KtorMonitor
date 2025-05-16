package ro.cosminmihu.ktor.monitor.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.Loading
import ro.cosminmihu.ktor.monitor.ui.notification.NotificationPermissionBanner
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_clean
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_filter
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_ic_launcher
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_library_name

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListScreen(
    uiState: ListUiState,
    setSearchQuery: (String) -> Unit,
    clearSearchQuery: () -> Unit,
    deleteCalls: () -> Unit,
    onCallClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSearchBar by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.ktor_library_name),
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        Image(
                            imageVector = vectorResource(Res.drawable.ktor_ic_launcher),
                            contentDescription = stringResource(Res.string.ktor_library_name),
                            modifier = Modifier.size(Dimens.ExtraExtraLarge)
                        )
                    },
                    actions = {
                        if (uiState.isEmpty && !showSearchBar) return@TopAppBar

                        IconButton(
                            onClick = {
                                showSearchBar = !showSearchBar
                                if (!showSearchBar) {
                                    clearSearchQuery()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = when (showSearchBar) {
                                    true -> Icons.Filled.SearchOff
                                    else -> Icons.Filled.Search
                                },
                                contentDescription = stringResource(Res.string.ktor_filter),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(
                            onClick = {
                                deleteCalls()
                                clearSearchQuery()
                                showSearchBar = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(Res.string.ktor_clean),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )

                AnimatedVisibility(visible = showSearchBar) {
                    SearchField(
                        onSearch = setSearchQuery,
                        onClear = clearSearchQuery
                    )
                }

                HorizontalDivider()
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxWidth()) {

            AnimatedVisibility(
                visible = uiState.showNotification,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                NotificationPermissionBanner()
            }

            when {
                uiState.isLoading -> {
                    Loading.Medium(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                uiState.isEmpty -> {
                    ListEmptyState(modifier = Modifier.fillMaxSize())
                }

                uiState.calls != null -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        itemsIndexed(
                            items = uiState.calls,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            CallItem(
                                call = item,
                                modifier = Modifier.animateItem().clickable { onCallClick(item.id) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ListScreenPreview() {
    ListScreen(
        modifier = Modifier,
        uiState = ListUiState(),
        setSearchQuery = {},
        clearSearchQuery = {},
        deleteCalls = {},
        onCallClick = {})
}

