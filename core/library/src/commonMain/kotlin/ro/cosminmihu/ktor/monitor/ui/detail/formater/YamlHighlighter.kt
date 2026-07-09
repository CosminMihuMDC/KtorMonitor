package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.ui.BothScrollbarsBox
import ro.cosminmihu.ktor.monitor.ui.detail.body.CodeLine
import ro.cosminmihu.ktor.monitor.ui.detail.body.LocalMaxLineNumber

@Composable
internal fun YamlHighlighter(
    yaml: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var lines by remember(yaml) { mutableStateOf<List<YamlLine>>(emptyList()) }

    LaunchedEffect(yaml) {
        lines = withContext(Dispatchers.Default) {
            parseYamlLines(yaml)
        }
    }

    if (lines.isEmpty()) return

    val listState = rememberLazyListState()
    val hScrollState = rememberScrollState()
    BothScrollbarsBox(listState, hScrollState, modifier) {
        Box(Modifier.fillMaxSize().horizontalScroll(hScrollState)) {
            SelectionContainer {
                CompositionLocalProvider(LocalMaxLineNumber provides lines.size) {
                    LazyColumn(
                        state = listState,
                        contentPadding = contentPadding,
                    ) {
                        itemsIndexed(
                            items = lines,
                            key = { index, _ -> index },
                            contentType = { _, _ -> "yaml-line" },
                        ) { index, line ->
                            YamlLineView(lineNumber = index + 1, line = line)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YamlLineView(lineNumber: Int, line: YamlLine) {
    CodeLine(lineNumber = lineNumber) {
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 1.dp, bottom = 1.dp),
        ) {
            Text(
                text = buildAnnotatedString {
                    val indent = " ".repeat(line.indentLevel * 2)
                    append(indent)

                    when (line) {
                        is YamlLine.KeyValue -> {
                            withStyle(SpanStyle(color = YamlHighlighterColors.keyColor)) {
                                append(line.key)
                            }
                            withStyle(SpanStyle(color = YamlHighlighterColors.punctuation)) {
                                append(": ")
                            }
                            withStyle(SpanStyle(color = YamlHighlighterColors.valueColor)) {
                                append(line.value)
                            }
                        }
                        is YamlLine.ListItem -> {
                            withStyle(SpanStyle(color = YamlHighlighterColors.punctuation)) {
                                append("- ")
                            }
                            withStyle(SpanStyle(color = YamlHighlighterColors.valueColor)) {
                                append(line.text)
                            }
                        }
                        is YamlLine.Comment -> {
                            withStyle(SpanStyle(color = YamlHighlighterColors.comment)) {
                                append("# ${line.text}")
                            }
                        }
                    }
                },
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                softWrap = false,
            )
        }
    }
}

private sealed interface YamlLine {
    val indentLevel: Int

    data class KeyValue(
        override val indentLevel: Int,
        val key: String,
        val value: String,
    ) : YamlLine

    data class ListItem(
        override val indentLevel: Int,
        val text: String,
    ) : YamlLine

    data class Comment(
        override val indentLevel: Int,
        val text: String,
    ) : YamlLine
}

private object YamlHighlighterColors {
    val keyColor = androidx.compose.ui.graphics.Color(0xFF9C27B0)
    val valueColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
    val punctuation = androidx.compose.ui.graphics.Color(0xFF2E86C1)
    val comment = androidx.compose.ui.graphics.Color(0xFFAAAAAA)
}

private fun parseYamlLines(yaml: String): List<YamlLine> {
    return yaml.split("\n").mapNotNull { rawLine ->
        val line = rawLine.trimEnd()
        if (line.isBlank()) return@mapNotNull null

        val indent = line.takeWhile { it == ' ' }.length / 2
        val trimmed = line.trim()

        when {
            trimmed.startsWith("#") -> {
                YamlLine.Comment(indent, trimmed.drop(1).trim())
            }
            trimmed.startsWith("- ") -> {
                YamlLine.ListItem(indent, trimmed.drop(2).trim())
            }
            trimmed.contains(":") -> {
                val colonIdx = trimmed.indexOf(':')
                val key = trimmed.substring(0, colonIdx).trim()
                val value = if (colonIdx + 1 < trimmed.length) {
                    trimmed.substring(colonIdx + 1).trim()
                } else {
                    ""
                }
                YamlLine.KeyValue(indent, key, value)
            }
            else -> YamlLine.Comment(indent, trimmed)
        }
    }
}

