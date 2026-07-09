package ro.cosminmihu.ktor.monitor.ui.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import ro.cosminmihu.ktor.monitor.ui.icons.Icons
import ro.cosminmihu.ktor.monitor.ui.icons.filled.ArrowDropDown
import ro.cosminmihu.ktor.monitor.ui.icons.filled.Language
import ro.cosminmihu.ktor.monitor.ui.icons.filled.Tag
import ro.cosminmihu.ktor.monitor.ui.icons.filled.Timer
import ro.cosminmihu.ktor.monitor.ui.icons.filled.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.ktor.http.HttpMethod
import org.jetbrains.compose.resources.stringResource
import ro.cosminmihu.ktor.monitor.domain.model.ContentType
import ro.cosminmihu.ktor.monitor.ui.Dimens
import ro.cosminmihu.ktor.monitor.ui.resources.Res
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_filter_content_type
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_filter_duration
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_filter_host
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_filter_method
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_filter_response_code
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_filter_sort_response_size
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_filter_sort_size_asc
import ro.cosminmihu.ktor.monitor.ui.resources.ktor_filter_sort_size_desc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterChipsRow(
    filter: ListUiState.Filter,
    availableMethods: Set<String>,
    availableHosts: Set<String>,
    availableContentTypes: Set<ContentType>,
    onToggleMethod: (String) -> Unit,
    onToggleResponseCodeRange: (ListUiState.Filter.ResponseCodeRange) -> Unit,
    onSetSizeSort: (ListUiState.Filter.SizeSort?) -> Unit,
    onToggleHost: (String) -> Unit,
    onToggleDuration: (ListUiState.Filter.DurationRange) -> Unit,
    onToggleContentType: (ContentType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Dimens.Small),
        horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Chip 1: Host
        if (availableHosts.isNotEmpty()) {
            var hostMenuExpanded by remember { mutableStateOf(false) }
            val hostLabel = when {
                filter.hosts.isEmpty() -> stringResource(Res.string.ktor_filter_host)
                else -> filter.hosts.sorted().joinToString(", ")
            }
            Box {
                FilterChip(
                    selected = filter.hosts.isNotEmpty(),
                    onClick = { hostMenuExpanded = true },
                    label = { Text(hostLabel) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                        )
                    },
                )
                DropdownMenu(
                    expanded = hostMenuExpanded,
                    onDismissRequest = { hostMenuExpanded = false },
                ) {
                    availableHosts.sorted().forEach { host ->
                        DropdownMenuItem(
                            text = { Text(host) },
                            leadingIcon = {
                                Checkbox(
                                    checked = host in filter.hosts,
                                    onCheckedChange = null,
                                )
                            },
                            onClick = { onToggleHost(host) },
                        )
                    }
                }
            }
        }

        // Chip 2: Method
        if (availableMethods.isNotEmpty()) {
            var methodMenuExpanded by remember { mutableStateOf(false) }
            val methodLabel = when {
                filter.methods.isEmpty() -> stringResource(Res.string.ktor_filter_method)
                else -> {
                    val methodOrder = HttpMethod.DefaultMethods.map { it.value }
                    filter.methods.sortedWith(compareBy {
                        methodOrder.indexOf(it.uppercase()).takeIf { i -> i >= 0 } ?: Int.MAX_VALUE
                    }).joinToString(", ")
                }
            }
            Box {
                FilterChip(
                    selected = filter.methods.isNotEmpty(),
                    onClick = { methodMenuExpanded = true },
                    label = { Text(methodLabel) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                        )
                    },
                )
                DropdownMenu(
                    expanded = methodMenuExpanded,
                    onDismissRequest = { methodMenuExpanded = false },
                ) {
                    val methodOrder = HttpMethod.DefaultMethods.map { it.value }
                    availableMethods.sortedWith(compareBy {
                        methodOrder.indexOf(it.uppercase()).takeIf { i -> i >= 0 } ?: Int.MAX_VALUE
                    }).forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method) },
                            leadingIcon = {
                                Checkbox(
                                    checked = method in filter.methods,
                                    onCheckedChange = null,
                                )
                            },
                            onClick = { onToggleMethod(method) },
                        )
                    }
                }
            }
        }

        // Chip 3: Response code range
        var codeMenuExpanded by remember { mutableStateOf(false) }
        val codeLabel = when {
            filter.responseCodeRanges.isEmpty() -> stringResource(Res.string.ktor_filter_response_code)
            else -> filter.responseCodeRanges.sortedBy { it.ordinal }.joinToString(", ") { it.label }
        }
        Box {
            FilterChip(
                selected = filter.responseCodeRanges.isNotEmpty(),
                onClick = { codeMenuExpanded = true },
                label = { Text(codeLabel) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Tag,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenu(
                expanded = codeMenuExpanded,
                onDismissRequest = { codeMenuExpanded = false },
            ) {
                ListUiState.Filter.ResponseCodeRange.entries.forEach { range ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                range.label,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        },
                        leadingIcon = {
                            Checkbox(
                                checked = range in filter.responseCodeRanges,
                                onCheckedChange = null,
                            )
                        },
                        onClick = { onToggleResponseCodeRange(range) },
                    )
                }
            }
        }

        // Chip 4: Duration
        var durationMenuExpanded by remember { mutableStateOf(false) }
        val durationLabel = when {
            filter.durations.isEmpty() -> stringResource(Res.string.ktor_filter_duration)
            else -> filter.durations.sortedBy { it.ordinal }.joinToString(", ") { it.label }
        }
        Box {
            FilterChip(
                selected = filter.durations.isNotEmpty(),
                onClick = { durationMenuExpanded = true },
                label = { Text(durationLabel) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenu(
                expanded = durationMenuExpanded,
                onDismissRequest = { durationMenuExpanded = false },
            ) {
                ListUiState.Filter.DurationRange.entries.forEach { range ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                range.label,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        },
                        leadingIcon = {
                            Checkbox(
                                checked = range in filter.durations,
                                onCheckedChange = null,
                            )
                        },
                        onClick = { onToggleDuration(range) },
                    )
                }
            }
        }

        // Chip 5: Content-Type
        if (availableContentTypes.isNotEmpty()) {
            var contentTypeMenuExpanded by remember { mutableStateOf(false) }
            val contentTypeLabel = when {
                filter.contentTypes.isEmpty() -> stringResource(Res.string.ktor_filter_content_type)
                else -> filter.contentTypes.sortedBy { it.ordinal }.joinToString(", ") { it.contentName }
            }
            Box {
                FilterChip(
                    selected = filter.contentTypes.isNotEmpty(),
                    onClick = { contentTypeMenuExpanded = true },
                    label = { Text(contentTypeLabel) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Tag,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                        )
                    },
                )
                DropdownMenu(
                    expanded = contentTypeMenuExpanded,
                    onDismissRequest = { contentTypeMenuExpanded = false },
                ) {
                    availableContentTypes.sortedBy { it.ordinal }.forEach { ct ->
                        DropdownMenuItem(
                            text = { Text(ct.contentName) },
                            leadingIcon = {
                                Checkbox(
                                    checked = ct in filter.contentTypes,
                                    onCheckedChange = null,
                                )
                            },
                            onClick = { onToggleContentType(ct) },
                        )
                    }
                }
            }
        }

        // Chip 6: Sort by size
        var sortMenuExpanded by remember { mutableStateOf(false) }
        val sortLabel = when (filter.sizeSort) {
            ListUiState.Filter.SizeSort.ASC -> stringResource(Res.string.ktor_filter_sort_size_asc)
            ListUiState.Filter.SizeSort.DESC -> stringResource(Res.string.ktor_filter_sort_size_desc)
            null -> stringResource(Res.string.ktor_filter_sort_response_size)
        }
        Box {
            FilterChip(
                selected = filter.sizeSort != null,
                onClick = { sortMenuExpanded = true },
                label = { Text(sortLabel) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(Res.string.ktor_filter_sort_size_asc),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    },
                    leadingIcon = {
                        RadioButton(
                            selected = filter.sizeSort == ListUiState.Filter.SizeSort.ASC,
                            onClick = null,
                        )
                    },
                    onClick = {
                        onSetSizeSort(if (filter.sizeSort == ListUiState.Filter.SizeSort.ASC) null else ListUiState.Filter.SizeSort.ASC)
                        sortMenuExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(Res.string.ktor_filter_sort_size_desc),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    },
                    leadingIcon = {
                        RadioButton(
                            selected = filter.sizeSort == ListUiState.Filter.SizeSort.DESC,
                            onClick = null,
                        )
                    },
                    onClick = {
                        onSetSizeSort(if (filter.sizeSort == ListUiState.Filter.SizeSort.DESC) null else ListUiState.Filter.SizeSort.DESC)
                        sortMenuExpanded = false
                    },
                )
            }
        }
    }
}
