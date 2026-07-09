package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import ro.cosminmihu.ktor.monitor.ui.BothScrollbarsBox
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import ro.cosminmihu.ktor.monitor.ui.icons.Icons
import ro.cosminmihu.ktor.monitor.ui.icons.filled.ArrowDropDown
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.ui.detail.body.CodeLine
import ro.cosminmihu.ktor.monitor.ui.detail.body.LocalMaxLineNumber
import kotlin.math.max

// --------------------------------------------------------------------------------
// PUBLIC API
// --------------------------------------------------------------------------------

@Composable
internal fun FormUrlEncoded(
    body: String,
    modifier: Modifier = Modifier,
    colors: FormTreeColors = FormTreeDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    initialExpanded: Boolean = true,
    onError: (Throwable) -> Unit = {}
) {
    var rootNodes by remember(body) { mutableStateOf<List<FormNode>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(body) {
        try {
            rootNodes = withContext(Dispatchers.Default) { FormParser.parse(body) }
                .assignLineNumbers()
            error = null
        } catch (e: Exception) {
            error = e.message
            onError(e)
        }
    }

    if (error != null || rootNodes.isEmpty()) return

    val maxLine = remember(rootNodes) { rootNodes.maxLine() }
    val collapsed = remember(rootNodes) { mutableStateMapOf<Int, Boolean>() }
    if (!initialExpanded) {
        // Default-collapse all parents on first build.
        remember(rootNodes) {
            rootNodes.forEachParent { collapsed[it.openingLine] = true }
        }
    }

    val rows by remember(rootNodes) {
        derivedStateOf { flattenForm(rootNodes, collapsed) }
    }

    val hScrollState = rememberScrollState()
    val listState = rememberLazyListState()
    BothScrollbarsBox(listState, hScrollState, modifier) {
        Box(Modifier.fillMaxSize().horizontalScroll(hScrollState)) {
            SelectionContainer {
                CompositionLocalProvider(LocalMaxLineNumber provides maxLine) {
                    LazyColumn(
                        state = listState,
                        contentPadding = contentPadding,
                    ) {
                        itemsIndexed(
                            items = rows,
                            key = { _, row -> row.id },
                            contentType = { _, row -> if (row.kind == FormRowKind.CLOSE) "form-close" else "form-row" },
                        ) { _, row ->
                            FormRowView(
                                row = row,
                                colors = colors,
                                onToggle = { id ->
                                    collapsed[id] = !(collapsed[id] == true)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------
// UI ROW
// --------------------------------------------------------------------------------

@Composable
private fun FormRowView(
    row: FormRow,
    colors: FormTreeColors,
    onToggle: (Int) -> Unit,
) {
    val indentation = 20.dp

    when (row.kind) {
        FormRowKind.OPEN -> {
            val arrowRotation by animateFloatAsState(targetValue = if (!row.isCollapsed) 0f else -90f)
            CodeLine(
                lineNumber = row.lineNumber,
                modifier = Modifier.clickable { onToggle(row.id) },
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(modifier = Modifier.size(24.dp)) {
                        Image(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colors.arrowColor),
                            modifier = Modifier.fillMaxSize().rotate(arrowRotation),
                        )
                    }
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.keyColor)) { append(row.key) }
                            withStyle(SpanStyle(color = colors.punctuationColor)) {
                                append(if (row.isArray) " [" else " {")
                            }
                            if (row.isCollapsed) {
                                withStyle(SpanStyle(color = colors.commentColor)) { append(" ... ") }
                                withStyle(SpanStyle(color = colors.punctuationColor)) {
                                    append(if (row.isArray) "]" else "}")
                                }
                            }
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        softWrap = false,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }

        FormRowKind.CLOSE -> {
            CodeLine(lineNumber = row.lineNumber) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth + 24.dp + 4.dp),
                ) {
                    Text(
                        text = if (row.isArray) "]" else "}",
                        color = colors.punctuationColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        softWrap = false,
                    )
                }
            }
        }

        FormRowKind.LEAF -> {
            CodeLine(lineNumber = row.lineNumber) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth + 24.dp + 4.dp, top = 1.dp, bottom = 1.dp),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.keyColor)) { append(row.key) }
                            withStyle(SpanStyle(color = colors.punctuationColor)) { append(" = ") }
                            withStyle(SpanStyle(color = colors.valueColor)) { append(row.value.orEmpty()) }
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------
// FLATTENING
// --------------------------------------------------------------------------------

private enum class FormRowKind { OPEN, CLOSE, LEAF }

private data class FormRow(
    val id: Int,
    val kind: FormRowKind,
    val depth: Int,
    val lineNumber: Int,
    val key: String,
    val value: String? = null,
    val isArray: Boolean = false,
    val isCollapsed: Boolean = false,
)

private fun flattenForm(
    roots: List<FormNode>,
    collapsed: SnapshotStateMap<Int, Boolean>,
): List<FormRow> {
    val out = ArrayList<FormRow>()
    fun visit(node: FormNode, depth: Int) {
        when (node) {
            is FormNode.Leaf -> out += FormRow(
                id = node.openingLine,
                kind = FormRowKind.LEAF,
                depth = depth,
                lineNumber = node.openingLine,
                key = node.key,
                value = node.value,
            )
            is FormNode.Parent -> {
                val isCollapsed = collapsed[node.openingLine] == true
                out += FormRow(
                    id = node.openingLine,
                    kind = FormRowKind.OPEN,
                    depth = depth,
                    lineNumber = node.openingLine,
                    key = node.key,
                    isArray = node.isArray,
                    isCollapsed = isCollapsed,
                )
                if (!isCollapsed) {
                    node.children.forEach { visit(it, depth + 1) }
                    out += FormRow(
                        // Use closingLine as id so it stays unique against opener.
                        id = -node.closingLine,
                        kind = FormRowKind.CLOSE,
                        depth = depth,
                        lineNumber = node.closingLine,
                        key = node.key,
                        isArray = node.isArray,
                    )
                }
            }
        }
    }
    roots.forEach { visit(it, 0) }
    return out
}

private fun List<FormNode>.forEachParent(action: (FormNode.Parent) -> Unit) {
    fun visit(node: FormNode) {
        if (node is FormNode.Parent) {
            action(node)
            node.children.forEach(::visit)
        }
    }
    forEach(::visit)
}

// --------------------------------------------------------------------------------
// DATA MODELS & COLORS
// --------------------------------------------------------------------------------

internal sealed class FormNode {
    abstract val key: String
    abstract val openingLine: Int

    data class Leaf(
        override val key: String,
        val value: String,
        override val openingLine: Int = 0,
    ) : FormNode()

    data class Parent(
        override val key: String,
        val children: List<FormNode>,
        val isArray: Boolean = false,
        override val openingLine: Int = 0,
        val closingLine: Int = 0,
    ) : FormNode()
}

private fun List<FormNode>.assignLineNumbers(): List<FormNode> {
    var counter = 0
    fun walk(node: FormNode): FormNode = when (node) {
        is FormNode.Leaf -> node.copy(openingLine = ++counter)
        is FormNode.Parent -> {
            val opening = ++counter
            val newChildren = node.children.map(::walk)
            val closing = ++counter
            node.copy(openingLine = opening, closingLine = closing, children = newChildren)
        }
    }
    return map(::walk)
}

internal data class FormTreeColors(
    val keyColor: Color,
    val valueColor: Color,
    val punctuationColor: Color,
    val arrowColor: Color,
    val commentColor: Color
)

internal object FormTreeDefaults {
    @Composable
    fun colors(
        keyColor: Color = Color(0xFFFF9800),
        valueColor: Color = Color(0xFF4CAF50),
        punctuationColor: Color = Color.Gray,
        arrowColor: Color = Color.Gray,
        commentColor: Color = Color.LightGray,
    ) = FormTreeColors(
        keyColor = keyColor,
        valueColor = valueColor,
        punctuationColor = punctuationColor,
        arrowColor = arrowColor,
        commentColor = commentColor,
    )
}

// --------------------------------------------------------------------------------
// PARSER LOGIC
// --------------------------------------------------------------------------------

internal object FormParser {
    fun parse(body: String): List<FormNode> {
        if (body.isBlank()) return emptyList()

        val pairs = body.split("&").mapNotNull { part ->
            val eqIndex = part.indexOf('=')
            if (eqIndex == -1) return@mapNotNull null

            val rawKey = part.substring(0, eqIndex)
            val rawValue = part.substring(eqIndex + 1)

            try {
                val key = rawKey.decodeUrlEncoded()
                val value = rawValue.decodeUrlEncoded()
                key to value
            } catch (_: Exception) {
                null
            }
        }

        return buildTree(pairs)
    }

    private fun buildTree(pairs: List<Pair<String, String>>): List<FormNode> {
        val rootMap = mutableMapOf<String, Any>()

        for ((fullKey, value) in pairs) {
            val parts = fullKey.split("[", "]").filter { it.isNotEmpty() }
            if (parts.isEmpty()) continue

            var currentLevel = rootMap

            for (i in 0 until parts.size - 1) {
                val part = parts[i]

                if (!currentLevel.containsKey(part) || currentLevel[part] !is MutableMap<*, *>) {
                    currentLevel[part] = mutableMapOf<String, Any>()
                }
                @Suppress("UNCHECKED_CAST")
                currentLevel = currentLevel[part] as MutableMap<String, Any>
            }

            val leafKey = parts.last()
            currentLevel[leafKey] = value
        }

        return mapToNodes(rootMap)
    }

    private fun mapToNodes(map: Map<String, Any>): List<FormNode> {
        return map.map { (key, value) ->
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                val childrenMap = value as Map<String, Any>
                val children = mapToNodes(childrenMap)
                val allKeysAreInts = children.all { it.key.toIntOrNull() != null }
                FormNode.Parent(key, children, isArray = allKeysAreInts)
            } else {
                FormNode.Leaf(key, value.toString())
            }
        }
    }
}

private fun String.decodeUrlEncoded(): String {
    val sb = StringBuilder(length)
    var i = 0
    while (i < length) {
        val c = this[i]
        when {
            c == '+' -> {
                sb.append(' ')
                i++
            }
            c == '%' && i + 2 < length -> {
                val hex = substring(i + 1, i + 3)
                val code = hex.toIntOrNull(16)
                if (code != null) {
                    sb.append(code.toChar())
                    i += 3
                } else {
                    sb.append(c)
                    i++
                }
            }
            else -> {
                sb.append(c)
                i++
            }
        }
    }
    return sb.toString()
}

private fun List<FormNode>.maxLine(): Int {
    var m = 0
    fun visit(node: FormNode) {
        when (node) {
            is FormNode.Parent -> {
                m = max(m, max(node.openingLine, node.closingLine))
                node.children.forEach(::visit)
            }
            is FormNode.Leaf -> m = max(m, node.openingLine)
        }
    }
    forEach(::visit)
    return m
}
