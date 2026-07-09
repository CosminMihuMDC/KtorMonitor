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
import androidx.compose.material3.LocalTextStyle
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ro.cosminmihu.ktor.monitor.ui.detail.body.CodeLine
import ro.cosminmihu.ktor.monitor.ui.detail.body.LocalMaxLineNumber
import kotlin.math.max

// --------------------------------------------------------------------------------
// PUBLIC API
// --------------------------------------------------------------------------------

@Composable
internal fun JsonTree(
    json: String,
    modifier: Modifier = Modifier,
    colors: JsonTreeColors = JsonTreeDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    initialExpanded: Boolean = true,
    onError: (Throwable) -> Unit = {}
) {
    var root by remember(json) { mutableStateOf<JsonNode?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(json) {
        try {
            val element = withContext(Dispatchers.Default) {
                jsonParser.parseToJsonElement(json)
            }
            root = withContext(Dispatchers.Default) {
                buildJsonNode(key = null, element = element).assignLineNumbers()
            }
            error = null
        } catch (e: Exception) {
            error = e.message
            onError(e)
        }
    }

    val node = root ?: return
    if (error != null) return

    val maxLine = remember(node) { node.maxLine() }
    val collapsed = remember(node) { mutableStateMapOf<Int, Boolean>() }
    if (!initialExpanded) {
        remember(node) { node.forEachComposite { collapsed[it.openingLine] = true } }
    }

    val rows by remember(node) {
        derivedStateOf { flattenJson(node, collapsed) }
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
                            JsonRowView(
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

private val jsonParser = Json { isLenient = true; ignoreUnknownKeys = true }

// --------------------------------------------------------------------------------
// UI ROW
// --------------------------------------------------------------------------------

@Composable
private fun JsonRowView(
    row: JsonRow,
    colors: JsonTreeColors,
    onToggle: (Int) -> Unit,
) {
    val indentation = 16.dp

    when (row.kind) {
        JsonRowKind.OPEN -> {
            val arrowRotation by animateFloatAsState(targetValue = if (!row.isCollapsed) 0f else -90f)
            val openBrace = if (row.isArray) "[" else "{"
            val closeBrace = if (row.isArray) "]" else "}"
            CodeLine(
                lineNumber = row.lineNumber,
                modifier = Modifier
                    .clickable(enabled = row.hasChildren) { if (row.hasChildren) onToggle(row.id) },
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth, top = 2.dp, bottom = 2.dp),
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
                            if (row.key != null) {
                                withStyle(SpanStyle(color = colors.keyColor)) { append("\"${row.key}\"") }
                                withStyle(SpanStyle(color = colors.symbolColor)) { append(": ") }
                            }
                            withStyle(SpanStyle(color = colors.symbolColor)) { append(openBrace) }
                            if (!row.hasChildren) {
                                withStyle(SpanStyle(color = colors.symbolColor)) {
                                    append(closeBrace)
                                    if (!row.isLast) append(",")
                                }
                            } else if (row.isCollapsed) {
                                withStyle(SpanStyle(color = colors.symbolColor)) {
                                    append(" ... ")
                                    append(closeBrace)
                                    if (!row.isLast) append(",")
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

        JsonRowKind.CLOSE -> {
            val closeBrace = if (row.isArray) "]" else "}"
            CodeLine(
                lineNumber = row.lineNumber,
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth + 24.dp + 4.dp, bottom = 2.dp),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.symbolColor)) {
                                append(closeBrace)
                                if (!row.isLast) append(",")
                            }
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        softWrap = false,
                    )
                }
            }
        }

        JsonRowKind.PRIMITIVE -> {
            CodeLine(
                lineNumber = row.lineNumber,
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth + 24.dp + 4.dp, top = 1.dp, bottom = 1.dp),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            if (row.key != null) {
                                withStyle(SpanStyle(color = colors.keyColor)) { append("\"${row.key}\"") }
                                withStyle(SpanStyle(color = colors.symbolColor)) { append(": ") }
                            }
                            val valueColor = when (row.primitiveKind) {
                                JsonPrimitiveKind.STRING -> colors.stringColor
                                JsonPrimitiveKind.NUMBER -> colors.numberColor
                                JsonPrimitiveKind.BOOLEAN -> colors.booleanColor
                                JsonPrimitiveKind.NULL -> colors.nullColor
                                null -> colors.nullColor
                            }
                            withStyle(SpanStyle(color = valueColor)) {
                                append(row.value.orEmpty())
                            }
                            if (!row.isLast) {
                                withStyle(SpanStyle(color = colors.symbolColor)) { append(",") }
                            }
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

private enum class JsonRowKind { OPEN, CLOSE, PRIMITIVE }

private data class JsonRow(
    val id: Int,
    val kind: JsonRowKind,
    val depth: Int,
    val lineNumber: Int,
    val key: String?,
    val isArray: Boolean = false,
    val hasChildren: Boolean = false,
    val isCollapsed: Boolean = false,
    val isLast: Boolean = true,
    val value: String? = null,
    val primitiveKind: JsonPrimitiveKind? = null,
)

private fun flattenJson(
    root: JsonNode,
    collapsed: SnapshotStateMap<Int, Boolean>,
): List<JsonRow> {
    val out = ArrayList<JsonRow>()
    fun visit(node: JsonNode, depth: Int, isLast: Boolean) {
        when (node) {
            is JsonNode.Composite -> {
                val hasChildren = node.children.isNotEmpty()
                val isCollapsed = hasChildren && collapsed[node.openingLine] == true
                out += JsonRow(
                    id = node.openingLine,
                    kind = JsonRowKind.OPEN,
                    depth = depth,
                    lineNumber = node.openingLine,
                    key = node.key,
                    isArray = node.isArray,
                    hasChildren = hasChildren,
                    isCollapsed = isCollapsed,
                    isLast = isLast,
                )
                if (hasChildren && !isCollapsed) {
                    val lastIdx = node.children.lastIndex
                    node.children.forEachIndexed { idx, child ->
                        visit(child, depth + 1, idx == lastIdx)
                    }
                    out += JsonRow(
                        id = -node.closingLine,
                        kind = JsonRowKind.CLOSE,
                        depth = depth,
                        lineNumber = node.closingLine,
                        key = null,
                        isArray = node.isArray,
                        isLast = isLast,
                    )
                }
            }
            is JsonNode.Primitive -> out += JsonRow(
                id = node.line,
                kind = JsonRowKind.PRIMITIVE,
                depth = depth,
                lineNumber = node.line,
                key = node.key,
                isLast = isLast,
                value = node.displayValue,
                primitiveKind = node.kind,
            )
        }
    }
    visit(root, 0, isLast = true)
    return out
}

private fun JsonNode.forEachComposite(action: (JsonNode.Composite) -> Unit) {
    fun visit(node: JsonNode) {
        if (node is JsonNode.Composite) {
            action(node)
            node.children.forEach(::visit)
        }
    }
    visit(this)
}

// --------------------------------------------------------------------------------
// DATA MODEL
// --------------------------------------------------------------------------------

internal enum class JsonPrimitiveKind { STRING, NUMBER, BOOLEAN, NULL }

internal sealed class JsonNode {
    abstract val key: String?

    data class Composite(
        override val key: String?,
        val isArray: Boolean,
        val children: List<JsonNode>,
        val openingLine: Int = 0,
        val closingLine: Int = 0,
    ) : JsonNode()

    data class Primitive(
        override val key: String?,
        val kind: JsonPrimitiveKind,
        val displayValue: String,
        val line: Int = 0,
    ) : JsonNode()
}

private fun buildJsonNode(key: String?, element: JsonElement): JsonNode = when (element) {
    is JsonObject -> {
        val children = element.entries.map { (k, v) -> buildJsonNode(k, v) }
        JsonNode.Composite(key = key, isArray = false, children = children)
    }
    is JsonArray -> {
        val children = element.map { v -> buildJsonNode(null, v) }
        JsonNode.Composite(key = key, isArray = true, children = children)
    }
    is JsonNull -> JsonNode.Primitive(
        key = key,
        kind = JsonPrimitiveKind.NULL,
        displayValue = "null",
    )
    is JsonPrimitive -> {
        val (kind, display) = when {
            element.isString -> JsonPrimitiveKind.STRING to "\"${element.content}\""
            element.content == "true" || element.content == "false" -> JsonPrimitiveKind.BOOLEAN to element.content
            else -> JsonPrimitiveKind.NUMBER to element.content
        }
        JsonNode.Primitive(key = key, kind = kind, displayValue = display)
    }
}

private fun JsonNode.assignLineNumbers(): JsonNode {
    var counter = 0
    fun walk(node: JsonNode): JsonNode = when (node) {
        is JsonNode.Composite -> {
            val opening = ++counter
            val newChildren = node.children.map(::walk)
            val closing = if (node.children.isEmpty()) 0 else ++counter
            node.copy(
                openingLine = opening,
                closingLine = closing,
                children = newChildren,
            )
        }
        is JsonNode.Primitive -> node.copy(line = ++counter)
    }
    return walk(this)
}

private fun JsonNode.maxLine(): Int {
    var m = 0
    fun visit(node: JsonNode) {
        when (node) {
            is JsonNode.Composite -> {
                m = max(m, max(node.openingLine, node.closingLine))
                node.children.forEach(::visit)
            }
            is JsonNode.Primitive -> m = max(m, node.line)
        }
    }
    visit(this)
    return m
}

// --------------------------------------------------------------------------------
// COLORS
// --------------------------------------------------------------------------------

internal data class JsonTreeColors(
    val keyColor: Color,
    val stringColor: Color,
    val numberColor: Color,
    val booleanColor: Color,
    val nullColor: Color,
    val symbolColor: Color,
    val arrowColor: Color,
)

internal object JsonTreeDefaults {
    @Composable
    fun colors(
        keyColor: Color = Color(0xFF9C27B0),
        stringColor: Color = Color(0xFF4CAF50),
        numberColor: Color = Color(0xFFFF9800),
        booleanColor: Color = Color(0xFFE91E63),
        nullColor: Color = LocalTextStyle.current.color,
        symbolColor: Color = Color(0xFF2E86C1),
        arrowColor: Color = Color(0xFF2E86C1),
    ) = JsonTreeColors(
        keyColor = keyColor,
        stringColor = stringColor,
        numberColor = numberColor,
        booleanColor = booleanColor,
        nullColor = nullColor,
        symbolColor = symbolColor,
        arrowColor = arrowColor,
    )
}
