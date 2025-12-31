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
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Entry point for the JavascriptTree.
 * Parses the JS code into blocks and displays them in a collapsible tree.
 */
@Composable
internal fun Javascript(
    code: String,
    modifier: Modifier = Modifier,
    colors: JsTreeColors = JsTreeDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    initialExpanded: Boolean = true,
    onError: (Throwable) -> Unit = {}
) {
    var rootNodes by remember(code) { mutableStateOf<List<JsNode>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(code) {
        try {
            rootNodes = JsSimpleParser.parse(code)
            error = null
        } catch (e: Exception) {
            error = e.message
            onError(e)
        }
    }

    if (error != null) return

    SelectionContainer {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
        ) {
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

/**
 * Recursive component to render a single JS node (Block or Statement).
 */
@Composable
private fun JsNodeView(
    node: JsNode,
    colors: JsTreeColors,
    depth: Int,
    isInitiallyExpanded: Boolean
) {
    val indentation = 20.dp

    when (node) {
        is JsNode.Block -> {
            var isExpanded by remember { mutableStateOf(isInitiallyExpanded) }
            val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 0f else -90f)

            Column {
                // Header: Arrow + Code Line + {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(start = (indentation * depth), top = 1.dp, bottom = 1.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Expand/Collapse Icon
                    Box(modifier = Modifier.size(24.dp)) {
                        Image(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand/Collapse",
                            colorFilter = ColorFilter.tint(colors.arrowColor),
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(arrowRotation)
                        )
                    }

                    Text(
                        text = buildAnnotatedString {
                            appendHighlightedJs(node.header, colors)
                            withStyle(SpanStyle(color = colors.punctuationColor)) {
                                append(" {")
                            }
                            if (!isExpanded) {
                                withStyle(SpanStyle(color = colors.commentColor)) {
                                    append(" ... }")
                                }
                            }
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 0.dp)
                    )
                }

                // Children (Recursive)
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        node.children.forEach { child ->
                            JsNodeView(
                                node = child,
                                colors = colors,
                                depth = depth + 1,
                                isInitiallyExpanded = isInitiallyExpanded
                            )
                        }
                        // Closing brace }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = (indentation * depth) + 24.dp, bottom = 1.dp)
                        ) {
                            Text(
                                text = "}",
                                color = colors.punctuationColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        is JsNode.Statement -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = (indentation * depth) + 24.dp, // Align with text of parent
                        top = 1.dp,
                        bottom = 1.dp
                    )
            ) {
                Text(
                    text = buildAnnotatedString {
                        appendHighlightedJs(node.content, colors)
                    },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// --- Syntax Highlighting Logic ---

private fun AnnotatedString.Builder.appendHighlightedJs(
    text: String,
    colors: JsTreeColors
) {
    // Regex patterns
    val keywords = setOf("function", "var", "let", "const", "return", "if", "else", "for", "while", "class", "import", "from", "export", "async", "await", "try", "catch", "switch", "case", "default", "throw", "typeof", "delete")
    val keywordPattern = "\\b(${keywords.joinToString("|")})\\b".toRegex()
    val stringPattern = "\".*?\"|'.*?'|`.*?`".toRegex()
    val numberPattern = "\\b\\d+(\\.\\d+)?\\b".toRegex()
    val commentPattern = "//.*|/\\*[\\s\\S]*?\\*/".toRegex()
    // A heuristic for regex literals in highlighting: / followed by non-space until /
    // This is imperfect in highlighting but functional for display.
    val regexPattern = "/[^/\\n]+/[gimuy]*".toRegex()

    var remaining = text

    while (remaining.isNotEmpty()) {
        val commentMatch = commentPattern.find(remaining)
        val stringMatch = stringPattern.find(remaining)
        val keywordMatch = keywordPattern.find(remaining)
        val numberMatch = numberPattern.find(remaining)
        val regexMatch = regexPattern.find(remaining)

        // Find the earliest match
        val matches = listOfNotNull(commentMatch, stringMatch, keywordMatch, numberMatch, regexMatch)
            .sortedBy { it.range.first }

        if (matches.isEmpty()) {
            withStyle(SpanStyle(color = colors.textColor)) { append(remaining) }
            break
        }

        val match = matches.first()
        val start = match.range.first
        val end = match.range.last + 1

        // Append text before match
        if (start > 0) {
            withStyle(SpanStyle(color = colors.textColor)) {
                append(remaining.substring(0, start))
            }
        }

        // Apply style to match
        val content = remaining.substring(start, end)
        val style = when (match) {
            commentMatch -> SpanStyle(color = colors.commentColor)
            stringMatch -> SpanStyle(color = colors.stringColor)
            regexMatch -> SpanStyle(color = colors.stringColor) // Color regex same as strings or different
            keywordMatch -> SpanStyle(color = colors.keywordColor, fontWeight = FontWeight.Bold)
            numberMatch -> SpanStyle(color = colors.numberColor)
            else -> SpanStyle(color = colors.textColor)
        }

        withStyle(style) { append(content) }

        remaining = remaining.substring(end)
    }
}

// --- Simple Parser Data Structure ---

private sealed interface JsNode {
    data class Block(val header: String, val children: List<JsNode>) : JsNode
    data class Statement(val content: String) : JsNode
}

/**
 * A parser that tokenizes code to handle minified strings, proper block nesting, and REGEX literals.
 */
private object JsSimpleParser {

    fun parse(code: String): List<JsNode> {
        val rootNodes = mutableListOf<JsNode>()
        // Stack holds lists of siblings. The top of the stack is the current block we are filling.
        val stack = ArrayDeque<MutableList<JsNode>>()
        stack.add(rootNodes)

        var index = 0
        val buffer = StringBuilder()

        // Track the last significant character to distinguish Regex vs Division
        var lastSignificantChar = ' '

        while (index < code.length) {
            val char = code[index]

            // 1. Handle Strings (skip parsing inside strings)
            if (char == '"' || char == '\'' || char == '`') {
                val quote = char
                buffer.append(char)
                index++
                while (index < code.length) {
                    val nextChar = code[index]
                    buffer.append(nextChar)
                    index++
                    if (nextChar == '\\') {
                        // Skip escaped char
                        if (index < code.length) {
                            buffer.append(code[index])
                            index++
                        }
                    } else if (nextChar == quote) {
                        break
                    }
                }
                lastSignificantChar = quote // Quote is significant
                continue
            }

            // 2. Handle Forward Slash (Comment, Division, or Regex)
            if (char == '/') {
                // Check for Comments first
                val nextChar = if (index + 1 < code.length) code[index + 1] else null

                if (nextChar == '/') {
                    // Line Comment //
                    buffer.append("//")
                    index += 2
                    while (index < code.length) {
                        val c = code[index]
                        if (c == '\n') break
                        buffer.append(c)
                        index++
                    }
                    val content = buffer.toString().trim()
                    if (content.isNotEmpty()) {
                        stack.last().add(JsNode.Statement(content))
                        buffer.clear()
                    }
                    index++ // Skip the \n
                    // Comment doesn't change lastSignificantChar structure usually, but let's leave it.
                    continue
                } else if (nextChar == '*') {
                    // Block Comment /* */
                    buffer.append("/*")
                    index += 2
                    while (index + 1 < code.length) {
                        val c = code[index]
                        buffer.append(c)
                        if (c == '*' && code[index + 1] == '/') {
                            buffer.append('/')
                            index += 2
                            break
                        }
                        index++
                    }
                    continue
                } else {
                    // Not a comment. Is it Division or Regex?
                    // Heuristic: If previous significant char was an operator, keyword start, or punctuation, it's a regex.
                    // If it was a number, identifier, or closing paren, it's division.
                    if (isRegexStart(lastSignificantChar)) {
                        // Handle REGEX Literal
                        buffer.append('/')
                        index++
                        while (index < code.length) {
                            val c = code[index]
                            buffer.append(c)
                            index++
                            if (c == '\\') {
                                // Escape next char (handles \/ inside regex)
                                if (index < code.length) {
                                    buffer.append(code[index])
                                    index++
                                }
                            } else if (c == '/') {
                                // End of Regex
                                break
                            } else if (c == '\n') {
                                // Fail-safe: Regex shouldn't cross lines (except with flags, but keeping it simple)
                                break
                            }
                        }
                        lastSignificantChar = '/'
                        continue
                    } else {
                        // Division Operator
                        buffer.append('/')
                        lastSignificantChar = '/'
                        index++
                        continue
                    }
                }
            }

            // 3. Handle Syntax Structure
            when (char) {
                '{' -> {
                    val header = buffer.toString().trim()
                    buffer.setLength(0)

                    val newChildren = mutableListOf<JsNode>()
                    val newBlock = JsNode.Block(header, newChildren)
                    stack.last().add(newBlock)
                    stack.add(newChildren)
                    lastSignificantChar = '{'
                }
                '}' -> {
                    val content = buffer.toString().trim()
                    if (content.isNotEmpty()) {
                        stack.last().add(JsNode.Statement(content))
                    }
                    buffer.setLength(0)
                    if (stack.size > 1) {
                        stack.removeLast()
                    }
                    lastSignificantChar = '}'
                }
                ';' -> {
                    buffer.append(char)
                    val content = buffer.toString().trim()
                    if (content.isNotEmpty()) {
                        stack.last().add(JsNode.Statement(content))
                    }
                    buffer.setLength(0)
                    lastSignificantChar = ';'
                }
                '\n' -> {
                    val content = buffer.toString().trim()
                    if (content.isNotEmpty()) {
                        stack.last().add(JsNode.Statement(content))
                        buffer.setLength(0)
                    }
                    // Newline acts like a separator but doesn't change operator context per se
                }
                else -> {
                    if (!char.isWhitespace()) {
                        lastSignificantChar = char
                    }
                    buffer.append(char)
                }
            }
            index++
        }

        // Flush remaining
        val remaining = buffer.toString().trim()
        if (remaining.isNotEmpty()) {
            stack.last().add(JsNode.Statement(remaining))
        }

        return rootNodes
    }

    private fun isRegexStart(lastChar: Char): Boolean {
        // If the last significant character was one of these, the next slash is likely a Regex, not division.
        // e.g. x = /.../ , return /.../ , ( /.../ ) , : /.../
        return when (lastChar) {
            '(', ',', '=', ':', '[', '!', '&', '|', '?', '{', '}', ';', ' ' -> true
            else -> false
        }
    }
}

// --- Colors ---

internal data class JsTreeColors(
    val keywordColor: Color,
    val stringColor: Color,
    val numberColor: Color,
    val commentColor: Color,
    val textColor: Color,
    val punctuationColor: Color,
    val arrowColor: Color
)

internal object JsTreeDefaults {
    @Composable
    fun colors(
        keywordColor: Color = Color(0xFFCC7832),
        stringColor: Color = Color(0xFF6A8759),
        numberColor: Color = Color(0xFF6897BB),
        commentColor: Color = Color(0xFF808080),
        textColor: Color = LocalTextStyle.current.color,
        punctuationColor: Color = LocalTextStyle.current.color.copy(alpha = 0.7f),
        arrowColor: Color = Color(0xFF6897BB)
    ) = JsTreeColors(
        keywordColor = keywordColor,
        stringColor = stringColor,
        numberColor = numberColor,
        commentColor = commentColor,
        textColor = textColor,
        punctuationColor = punctuationColor,
        arrowColor = arrowColor
    )
}