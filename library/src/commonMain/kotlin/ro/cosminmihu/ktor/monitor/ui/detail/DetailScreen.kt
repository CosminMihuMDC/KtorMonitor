package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_back
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
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { PAGE_COUNT }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
                            modifier = Modifier
                                .fillMaxWidth()
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
                                        modifier = Modifier.padding(vertical = Dimens.Small)
                                    )
                                }
                            }
                        }
                    },
                    actions = {
//                        IconButton(onClick = {}, enabled = false) {
//                            Icon(
//                                imageVector = Icons.Filled.MoreVert,
//                                contentDescription = stringResource(Res.string.ktor_more),
//                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
//                            )
//                        }
                    },
                    scrollBehavior = scrollBehavior
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