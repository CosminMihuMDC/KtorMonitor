package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
internal fun Css(
    css: String,
    modifier: Modifier = Modifier,
    colors: CssTreeColors = CssTreeDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    initialExpanded: Boolean = false,
    onError: (Throwable) -> Unit = {}
) {
    var nodes by remember(css) { mutableStateOf<List<CssNode>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(css) {
        try {
            nodes = withContext(Dispatchers.Default) {
                CssParser(css).parse().assignLineNumbers()
            }
            error = null
        } catch (e: Exception) {
            error = e.message
            onError(e)
        }
    }

    if (error != null || nodes.isEmpty()) return

    val maxLine = remember(nodes) { nodes.maxLine() }
    val collapsed = remember(nodes) { mutableStateMapOf<Int, Boolean>() }
    if (!initialExpanded) {
        remember(nodes) {
            nodes.forEachRule { collapsed[it.openingLine] = true }
        }
    }

    val rows by remember(nodes) {
        derivedStateOf { flattenCss(nodes, collapsed) }
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
                            contentType = { _, row -> row.kind.name },
                        ) { _, row ->
                            CssRowView(
                                row = row,
                                colors = colors,
                                onToggle = { id -> collapsed[id] = !(collapsed[id] == true) },
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
private fun CssRowView(
    row: CssRow,
    colors: CssTreeColors,
    onToggle: (Int) -> Unit,
) {
    val indentation = 20.dp

    when (row.kind) {
        CssRowKind.SELECTOR -> {
            val arrowRotation by animateFloatAsState(targetValue = if (!row.isCollapsed) 0f else -90f)
            CodeLine(
                lineNumber = row.lineNumber,
                modifier = Modifier
                    .clickable(enabled = row.hasChildren) { if (row.hasChildren) onToggle(row.id) },
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth, top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(modifier = Modifier.size(24.dp)) {
                        if (row.hasChildren) {
                            Image(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colors.arrowColor),
                                modifier = Modifier.fillMaxSize().rotate(arrowRotation),
                            )
                        }
                    }
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.selectorColor)) {
                                append(row.text.trim())
                            }
                            withStyle(SpanStyle(color = colors.punctuationColor)) { append(" {") }
                            if (row.isCollapsed && row.hasChildren) {
                                withStyle(SpanStyle(color = colors.commentColor)) { append(" ... }") }
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

        CssRowKind.DECLARATION -> {
            CodeLine(
                lineNumber = row.lineNumber,
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth + 24.dp + 4.dp, top = 1.dp, bottom = 1.dp),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.propertyColor)) { append(row.text.trim()) }
                            withStyle(SpanStyle(color = colors.punctuationColor)) { append(": ") }
                            withStyle(SpanStyle(color = colors.valueColor)) { append(row.value.orEmpty().trim()) }
                            withStyle(SpanStyle(color = colors.punctuationColor)) { append(";") }
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        softWrap = false,
                    )
                }
            }
        }

        CssRowKind.CLOSE -> {
            CodeLine(
                lineNumber = row.lineNumber,
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth + 24.dp + 4.dp),
                ) {
                    Text(
                        text = "}",
                        color = colors.punctuationColor,
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

private enum class CssRowKind { SELECTOR, DECLARATION, CLOSE }

private data class CssRow(
    val id: Int,
    val kind: CssRowKind,
    val depth: Int,
    val lineNumber: Int,
    val text: String,
    val value: String? = null,
    val hasChildren: Boolean = false,
    val isCollapsed: Boolean = false,
)

private fun flattenCss(
    roots: List<CssNode>,
    collapsed: SnapshotStateMap<Int, Boolean>,
): List<CssRow> {
    val out = ArrayList<CssRow>()
    fun visit(node: CssNode, depth: Int) {
        when (node) {
            is CssNode.Rule -> {
                val hasChildren = node.children.isNotEmpty()
                val isCollapsed = hasChildren && collapsed[node.openingLine] == true
                out += CssRow(
                    id = node.openingLine,
                    kind = CssRowKind.SELECTOR,
                    depth = depth,
                    lineNumber = node.openingLine,
                    text = node.selector,
                    hasChildren = hasChildren,
                    isCollapsed = isCollapsed,
                )
                if (hasChildren && !isCollapsed) {
                    node.children.forEach { visit(it, depth + 1) }
                    out += CssRow(
                        id = -node.closingLine,
                        kind = CssRowKind.CLOSE,
                        depth = depth,
                        lineNumber = node.closingLine,
                        text = "",
                    )
                }
            }
            is CssNode.Declaration -> out += CssRow(
                id = node.line,
                kind = CssRowKind.DECLARATION,
                depth = depth,
                lineNumber = node.line,
                text = node.property,
                value = node.value,
            )
            is CssNode.Comment -> Unit
        }
    }
    roots.forEach { visit(it, 0) }
    return out
}

private fun List<CssNode>.forEachRule(action: (CssNode.Rule) -> Unit) {
    fun visit(node: CssNode) {
        if (node is CssNode.Rule) {
            action(node)
            node.children.forEach(::visit)
        }
    }
    forEach(::visit)
}

// --------------------------------------------------------------------------------
// DATA & COLORS
// --------------------------------------------------------------------------------

internal sealed class CssNode {
    data class Rule(
        val selector: String,
        val children: List<CssNode>,
        val openingLine: Int = 0,
        val closingLine: Int = 0,
    ) : CssNode()

    data class Declaration(
        val property: String,
        val value: String,
        val line: Int = 0,
    ) : CssNode()

    data class Comment(val text: String) : CssNode()
}

private fun List<CssNode>.assignLineNumbers(): List<CssNode> {
    var counter = 0
    fun walk(node: CssNode): CssNode = when (node) {
        is CssNode.Rule -> {
            val opening = ++counter
            val newChildren = node.children.map(::walk)
            val closing = ++counter
            node.copy(
                openingLine = opening,
                closingLine = closing,
                children = newChildren,
            )
        }
        is CssNode.Declaration -> node.copy(line = ++counter)
        is CssNode.Comment -> node
    }
    return map(::walk)
}

internal data class CssTreeColors(
    val selectorColor: Color,
    val propertyColor: Color,
    val valueColor: Color,
    val punctuationColor: Color,
    val commentColor: Color,
    val arrowColor: Color
)

internal object CssTreeDefaults {
    @Composable
    fun colors(
        selectorColor: Color = Color(0xFFFFC107),
        propertyColor: Color = Color(0xFF03A9F4),
        valueColor: Color = Color(0xFF8BC34A),
        punctuationColor: Color = Color.Gray,
        commentColor: Color = Color.DarkGray,
        arrowColor: Color = Color.Gray
    ) = CssTreeColors(
        selectorColor = selectorColor,
        propertyColor = propertyColor,
        valueColor = valueColor,
        punctuationColor = punctuationColor,
        commentColor = commentColor,
        arrowColor = arrowColor
    )
}

// --------------------------------------------------------------------------------
// CSS PARSER
// --------------------------------------------------------------------------------

private class CssParser(private val input: String) {
    private var pos = 0
    private val len = input.length

    fun parse(): List<CssNode> {
        val nodes = mutableListOf<CssNode>()
        skipWhitespace()
        while (pos < len) {
            nodes.add(parseRule())
            skipWhitespace()
        }
        return nodes
    }

    private fun parseRule(): CssNode {
        val start = pos
        while (pos < len && input[pos] != '{') {
            pos++
        }

        val selector = input.substring(start, pos).trim()
        val children = mutableListOf<CssNode>()

        if (match('{')) {
            consume('{')

            while (pos < len && !match('}')) {
                skipWhitespace()
                if (pos >= len) break

                var lookaheadStart = pos
                var isNestedRule = false

                while (lookaheadStart < len && input[lookaheadStart] != ';' && input[lookaheadStart] != '}') {
                    if (input[lookaheadStart] == '{') {
                        isNestedRule = true
                        break
                    }
                    lookaheadStart++
                }

                if (isNestedRule) {
                    children.add(parseRule())
                } else {
                    val decl = parseDeclaration()
                    if (decl != null) children.add(decl)
                }

                skipWhitespace()
            }
            consume('}')
        }

        return CssNode.Rule(selector, children)
    }

    private fun parseDeclaration(): CssNode? {
        val start = pos
        while (pos < len && input[pos] != ':' && input[pos] != '}') {
            pos++
        }

        if (match('}')) return null

        val property = input.substring(start, pos).trim()
        if (property.isEmpty()) return null

        consume(':')

        val valStart = pos
        while (pos < len && input[pos] != ';' && input[pos] != '}') {
            pos++
        }

        val value = input.substring(valStart, pos).trim()
        if (match(';')) consume(';')

        return CssNode.Declaration(property, value)
    }

    private fun skipWhitespace() {
        while (pos < len) {
            if (input[pos].isWhitespace()) {
                pos++
            } else if (input.startsWith("/*", pos)) {
                pos += 2
                while (pos < len && !input.startsWith("*/", pos)) {
                    pos++
                }
                pos += 2
            } else {
                break
            }
        }
    }

    private fun match(c: Char): Boolean = pos < len && input[pos] == c
    private fun consume(c: Char) {
        if (match(c)) pos++
    }
}

private fun List<CssNode>.maxLine(): Int {
    var m = 0
    fun visit(node: CssNode) {
        when (node) {
            is CssNode.Rule -> {
                m = max(m, max(node.openingLine, node.closingLine))
                node.children.forEach(::visit)
            }
            is CssNode.Declaration -> m = max(m, node.line)
            is CssNode.Comment -> Unit
        }
    }
    forEach(::visit)
    return m
}
