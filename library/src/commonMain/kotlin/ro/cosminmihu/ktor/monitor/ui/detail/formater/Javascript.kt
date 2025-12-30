package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ro.cosminmihu.ktor.monitor.domain.model.isWhiteSpace

// --------------------------------------------------------------------------------
// PUBLIC API
// --------------------------------------------------------------------------------

/**
 * Entry point for the Javascript Tree.
 * Parses the JS code and displays it as a collapsible tree.
 */
@Composable
internal fun Javascript(
    js: String,
    modifier: Modifier = Modifier,
    colors: JsTreeColors = JsTreeDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    initialExpanded: Boolean = false,
    onError: (Throwable) -> Unit = {}
) {
    var rootNodes by remember(js) { mutableStateOf<List<JsNode>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(js) {
        try {
            val nodes = withContext(Dispatchers.Default) {
                // Use our custom lightweight parser
                JsParser(js).parse()
            }
            rootNodes = nodes
            error = null
        } catch (e: Exception) {
            error = e.message
            onError(e)
        }
    }

    Box(modifier = modifier.padding(contentPadding)) {
        if (error == null) {
            SelectionContainer {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    rootNodes.forEach { node ->
                        JsNodeView(
                            node = node,
                            colors = colors,
                            depth = 0,
                            isInitiallyExpanded = initialExpanded
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------
// UI COMPONENTS
// --------------------------------------------------------------------------------

@Composable
private fun JsNodeView(
    node: JsNode,
    colors: JsTreeColors,
    depth: Int,
    isInitiallyExpanded: Boolean
) {
    val indentation = 20.dp

    // Determine if this node is expandable (Object or Array with items)
    val children = when (node) {
        is JsNode.JsObject -> node.properties
        is JsNode.JsArray -> node.items
        else -> emptyList()
    }

    val hasChildren = children.isNotEmpty()
    var isExpanded by remember { mutableStateOf(isInitiallyExpanded) }
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 0f else -90f)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = hasChildren) { isExpanded = !isExpanded }
                .padding(start = (indentation * depth), top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 1. Expand Icon
            Box(modifier = Modifier.size(24.dp)) {
                if (hasChildren) {
                    Image(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colors.arrowColor),
                        modifier = Modifier.fillMaxSize().rotate(arrowRotation)
                    )
                }
            }

            // 2. Node Content
            Text(
                text = buildAnnotatedString {
                    // Optional Key (e.g., "name": )
                    if (node.key != null) {
                        withStyle(SpanStyle(color = colors.keyColor)) {
                            append(node.key)
                        }
                        withStyle(SpanStyle(color = colors.punctuationColor)) {
                            append(": ")
                        }
                    }

                    // Value representation
                    when (node) {
                        is JsNode.JsObject -> {
                            withStyle(SpanStyle(color = colors.structureColor)) { append("{") }
                            if (!isExpanded && hasChildren) append(" ... }")
                        }

                        is JsNode.JsArray -> {
                            withStyle(SpanStyle(color = colors.structureColor)) { append("[") }
                            if (!isExpanded && hasChildren) append(" ... ]")
                        }

                        is JsNode.JsValue -> {
                            // Check for quotes for strings
                            val isString =
                                node.rawValue.startsWith("\"") || node.rawValue.startsWith("'")
                            val color =
                                if (isString) colors.stringColor else colors.numberColor
                            withStyle(SpanStyle(color = color)) {
                                append(node.rawValue)
                            }
                        }
                    }

                    // Trailing Comma (visual only)
                    // In a real tree list, we might want to check if it's the last item,
                    // but for simplicity we can omit or add logically if needed.
                },
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // 3. Children (Recursive)
        AnimatedVisibility(visible = isExpanded && hasChildren) {
            Column {
                children.forEach { child ->
                    JsNodeView(child, colors, depth + 1, isInitiallyExpanded)
                }

                // Closing Bracket/Brace
                Row(modifier = Modifier.padding(start = (indentation * depth) + 24.dp + 4.dp)) {
                    Text(
                        text = if (node is JsNode.JsObject) "}" else "]",
                        color = colors.structureColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------
// DATA STRUCTURES & COLORS
// --------------------------------------------------------------------------------

internal sealed class JsNode(open val key: String?) {
    data class JsObject(override val key: String?, val properties: List<JsNode>) :
        JsNode(key)

    data class JsArray(override val key: String?, val items: List<JsNode>) : JsNode(key)
    data class JsValue(override val key: String?, val rawValue: String) : JsNode(key)
}

internal data class JsTreeColors(
    val keyColor: Color,
    val stringColor: Color,
    val numberColor: Color, // Also booleans
    val structureColor: Color, // Brackets, Braces
    val punctuationColor: Color, // Colons
    val arrowColor: Color
)

internal object JsTreeDefaults {
    @Composable
    fun colors(
        keyColor: Color = Color(0xFF9C27B0),     // Purple for keys
        stringColor: Color = Color(0xFF4CAF50),  // Green for strings
        numberColor: Color = Color(0xFF2196F3),  // Blue for numbers/bools
        structureColor: Color = Color.Black,
        punctuationColor: Color = Color.Gray,
        arrowColor: Color = Color.Gray
    ) = JsTreeColors(
        keyColor = keyColor,
        stringColor = stringColor,
        numberColor = numberColor,
        structureColor = structureColor,
        punctuationColor = punctuationColor,
        arrowColor = arrowColor
    )
}

// --------------------------------------------------------------------------------
// LIGHTWEIGHT JS PARSER
// --------------------------------------------------------------------------------

/**
 * A simple recursive parser specifically for JS Data structures.
 * Handles Objects {}, Arrays [], and primitives.
 * Tolerates lax JS syntax (keys without quotes).
 */
private class JsParser(private val input: String) {
    private var pos = 0
    private val len = input.length

    fun parse(): List<JsNode> {
        val roots = mutableListOf<JsNode>()
        skipWhitespace()
        while (pos < len) {
            roots.add(parseValue(null))
            skipWhitespace()
            if (match(',')) pos++ // Skip top-level commas if any
        }
        return roots
    }

    private fun parseValue(key: String?): JsNode {
        skipWhitespace()
        return when {
            match('{') -> parseObject(key)
            match('[') -> parseArray(key)
            else -> parsePrimitive(key)
        }
    }

    private fun parseObject(key: String?): JsNode {
        consume('{')
        val props = mutableListOf<JsNode>()
        while (pos < len && !match('}')) {
            skipWhitespace()
            // Parse Key
            val propKey = parseKey()
            skipWhitespace()
            consume(':')
            // Parse Value
            props.add(parseValue(propKey))
            skipWhitespace()
            if (match(',')) pos++ // consume comma
        }
        consume('}')
        return JsNode.JsObject(key, props)
    }

    private fun parseArray(key: String?): JsNode {
        consume('[')
        val items = mutableListOf<JsNode>()
        while (pos < len && !match(']')) {
            skipWhitespace()
            items.add(parseValue(null))
            skipWhitespace()
            if (match(',')) pos++
        }
        consume(']')
        return JsNode.JsArray(key, items)
    }

    private fun parsePrimitive(key: String?): JsNode {
        val start = pos
        // Handle Strings with quotes
        if (match('"') || match('\'')) {
            val quote = input[pos]
            pos++
            while (pos < len) {
                if (input[pos] == '\\') {
                    pos += 2; continue
                } // escape
                if (input[pos] == quote) {
                    pos++; break
                }
                pos++
            }
        } else {
            // Handle numbers, booleans, null, or bare words
            while (pos < len && !isStopChar(input[pos])) {
                pos++
            }
        }
        val value = input.substring(start, pos).trim()
        return JsNode.JsValue(key, value)
    }

    private fun parseKey(): String {
        val start = pos
        // If quoted key
        if (match('"') || match('\'')) {
            val quote = input[pos]
            pos++
            val keyStart = pos
            while (pos < len && input[pos] != quote) pos++
            val k = input.substring(keyStart, pos)
            pos++ // skip quote
            return k
        } else {
            // Unquoted key
            while (pos < len && input[pos] != ':' && !Char.isWhiteSpace(input[pos])) {
                pos++
            }
            return input.substring(start, pos)
        }
    }

    private fun skipWhitespace() {
        while (pos < len && Char.isWhiteSpace(input[pos])) pos++
    }

    private fun match(c: Char): Boolean = pos < len && input[pos] == c

    private fun consume(c: Char) {
        if (match(c)) pos++
    }

    private fun isStopChar(c: Char): Boolean {
        return c == ',' || c == '}' || c == ']' || Char.isWhiteSpace(c)
    }
}
