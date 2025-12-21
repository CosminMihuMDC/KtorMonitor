package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_back
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_copy_as_curl
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_copy_as_text
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_copy_as_wget
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_copy_url
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_more
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_request
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_response
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_summary

private const val PAGE_COUNT = 3

private const val PAGE_INDEX_SUMMARY = 0
private const val PAGE_INDEX_REQUEST = 1
private const val PAGE_INDEX_RESPONSE = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailScreen(
    uiState: DetailUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onShare: (DetailUiState.ShareType) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { PAGE_COUNT }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = stringResource(Res.string.ktor_back),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    title = {
                        PrimaryTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            divider = {},
                        ) {
                            val tabs = listOf(
                                Res.string.ktor_summary,
                                Res.string.ktor_request,
                                Res.string.ktor_response
                            )

                            tabs.forEachIndexed { index, stringRes ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                    }
                                ) {
                                    Text(
                                        text = stringResource(stringRes),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        modifier = Modifier.padding(vertical = Dimens.MediumLarge)
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        var menuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = stringResource(Res.string.ktor_more),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DetailUiState.ShareType.entries.forEach {
                                    DropdownMenuItem(
                                        enabled = uiState.call != null,
                                        text = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(Dimens.Small)
                                            ) {
                                                val type = stringResource(
                                                    when (it) {
                                                        DetailUiState.ShareType.Url -> Res.string.ktor_copy_url
                                                        DetailUiState.ShareType.Curl -> Res.string.ktor_copy_as_curl
                                                        DetailUiState.ShareType.Wget -> Res.string.ktor_copy_as_wget
                                                        DetailUiState.ShareType.Text -> Res.string.ktor_copy_as_text
                                                    }
                                                )
                                                Icon(
                                                    imageVector =
                                                        when (it) {
                                                            DetailUiState.ShareType.Url -> Icons.Default.Link
                                                            DetailUiState.ShareType.Curl -> Icons.Default.Laptop
                                                            DetailUiState.ShareType.Wget -> Icons.Default.Downloading
                                                            DetailUiState.ShareType.Text -> Icons.AutoMirrored.Filled.Article
                                                        },
                                                    contentDescription = type,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(text = type)
                                            }
                                        },
                                        onClick = {
                                            onShare(it)
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                )
                HorizontalDivider()
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxWidth()
        ) {

            if (uiState.call == null || uiState.summary == null) {
                return@Column
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    PAGE_INDEX_SUMMARY -> SummaryScreen(
                        summary = uiState.summary,
                        modifier = Modifier.fillMaxSize()
                    )

                    PAGE_INDEX_REQUEST -> RequestScreen(
                        request = uiState.call.request,
                        modifier = Modifier.fillMaxSize()
                    )

                    PAGE_INDEX_RESPONSE -> ResponseScreen(
                        response = uiState.call.response,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
