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
import ro.cosminmihu.ktor.monitor.domain.model.isWhiteSpace

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
    var rootNodes by remember(css) { mutableStateOf<List<CssNode>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(css) {
        try {
            rootNodes = CssParser(css).parse()
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
                        CssNodeView(
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
private fun CssNodeView(
    node: CssNode,
    colors: CssTreeColors,
    depth: Int,
    isInitiallyExpanded: Boolean
) {
    val indentation = 20.dp

    when (node) {
        is CssNode.Rule -> {
            var isExpanded by remember { mutableStateOf(isInitiallyExpanded) }
            val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 0f else -90f)
            val hasChildren = node.children.isNotEmpty()

            Column {
                // Selector Row: "selector {"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = hasChildren) { isExpanded = !isExpanded }
                        .padding(start = (indentation * depth), top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Expand Icon
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

                    // Selector Text
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.selectorColor)) {
                                append(node.selector.trim())
                            }
                            withStyle(SpanStyle(color = colors.punctuationColor)) {
                                append(" {")
                            }
                            if (!isExpanded && hasChildren) {
                                withStyle(SpanStyle(color = colors.commentColor)) {
                                    append(" ... }")
                                }
                            }
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Children (Properties or nested rules)
                AnimatedVisibility(visible = isExpanded && hasChildren) {
                    Column {
                        node.children.forEach { child ->
                            CssNodeView(child, colors, depth + 1, isInitiallyExpanded)
                        }

                        // Closing Brace "}"
                        Row(modifier = Modifier.padding(start = (indentation * depth) + 24.dp + 4.dp)) {
                            Text(
                                text = "}",
                                color = colors.punctuationColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        is CssNode.Declaration -> {
            // Property Row: "key: value;"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = (indentation * depth) + 24.dp + 4.dp,
                        top = 1.dp,
                        bottom = 1.dp
                    )
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.propertyColor)) {
                            append(node.property.trim())
                        }
                        withStyle(SpanStyle(color = colors.punctuationColor)) {
                            append(": ")
                        }
                        withStyle(SpanStyle(color = colors.valueColor)) {
                            append(node.value.trim())
                        }
                        withStyle(SpanStyle(color = colors.punctuationColor)) {
                            append(";")
                        }
                    },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }
        }

        is CssNode.Comment -> {
            // Optional: Render comments if parsing supports them
        }
    }
}

// --------------------------------------------------------------------------------
// DATA & COLORS
// --------------------------------------------------------------------------------

internal sealed class CssNode {
    data class Rule(val selector: String, val children: List<CssNode>) : CssNode()
    data class Declaration(val property: String, val value: String) : CssNode()
    data class Comment(val text: String) : CssNode()
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
        selectorColor: Color = Color(0xFFFFC107), // Amber for selectors
        propertyColor: Color = Color(0xFF03A9F4), // Light Blue for properties
        valueColor: Color = Color(0xFF8BC34A),    // Light Green for values
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
        // Read until '{' to get selector
        while (pos < len && input[pos] != '{') {
            // Basic comment skipping could go here
            pos++
        }

        val selector = input.substring(start, pos).trim()
        val children = mutableListOf<CssNode>()

        if (match('{')) {
            consume('{')

            while (pos < len && !match('}')) {
                skipWhitespace()
                if (pos >= len) break

                // Check if this is a nested rule or a declaration
                // A declaration looks like "key: value;"
                // A nested rule looks like "selector { ... }"

                var lookaheadStart = pos
                var isNestedRule = false

                // Simple lookahead: scan for ':' or '{'
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
        // Read property key
        while (pos < len && input[pos] != ':' && input[pos] != '}') {
            pos++
        }

        if (match('}')) return null // Empty or malformed

        val property = input.substring(start, pos).trim()
        if (property.isEmpty()) return null

        consume(':')

        val valStart = pos
        // Read value until ';' or '}' (end of block)
        while (pos < len && input[pos] != ';' && input[pos] != '}') {
            pos++
        }

        val value = input.substring(valStart, pos).trim()
        if (match(';')) consume(';') // Optional semicolon

        return CssNode.Declaration(property, value)
    }

    private fun skipWhitespace() {
        while (pos < len) {
            if (Char.isWhiteSpace(input[pos])) {
                pos++
            } else if (input.startsWith("/*", pos)) {
                // Skip comment
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

    private fun String.startsWith(prefix: String, startIndex: Int): Boolean =
        this.regionMatches(startIndex, prefix, 0, prefix.length)
}