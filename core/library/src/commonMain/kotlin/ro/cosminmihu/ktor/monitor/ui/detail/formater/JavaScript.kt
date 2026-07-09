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
import ro.cosminmihu.ktor.monitor.ui.VerticalScrollbarBox
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
import androidx.compose.ui.text.AnnotatedString
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

// --------------------------------------------------------------------------------
// PUBLIC API
// --------------------------------------------------------------------------------

/**
 * Renders JavaScript source code with simple syntax highlighting and a
 * left-side line-number gutter. Each source line is displayed as a [CodeLine].
 * Brace-delimited blocks (`{ ... }`) can be folded by tapping the chevron on
 * the opening line.
 *
 * @param code The JavaScript content string.
 * @param modifier Modifier for the root layout.
 * @param colors Custom colors for the highlighter.
 * @param contentPadding Padding for the container.
 */
@Composable
internal fun JavaScript(
    code: String,
    modifier: Modifier = Modifier,
    colors: JsCodeColors = JsCodeDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var highlighted by remember(code, colors) { mutableStateOf(JsHighlighted.EMPTY) }

    LaunchedEffect(code, colors) {
        highlighted = withContext(Dispatchers.Default) {
            highlightJavaScript(beautifyJavaScript(code), colors)
        }
    }

    val lines = highlighted.lines
    val folds = highlighted.folds
    if (lines.isEmpty()) return

    val collapsed = remember(highlighted) { mutableStateMapOf<Int, Boolean>() }

    val visibleIndices by remember(highlighted) {
        derivedStateOf { computeVisibleIndices(lines.size, folds, collapsed) }
    }

    val listState = rememberLazyListState()
    VerticalScrollbarBox(listState, modifier) {
        SelectionContainer {
            CompositionLocalProvider(LocalMaxLineNumber provides lines.size) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                ) {
                    itemsIndexed(
                        items = visibleIndices,
                        key = { _, lineIndex -> lineIndex },
                        contentType = { _, _ -> "js-line" },
                    ) { _, lineIndex ->
                        JsCodeRow(
                            lineIndex = lineIndex,
                            line = lines[lineIndex],
                            foldClosing = folds[lineIndex],
                            isCollapsed = collapsed[lineIndex] == true,
                            colors = colors,
                            onToggle = { collapsed[lineIndex] = !(collapsed[lineIndex] == true) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JsCodeRow(
    lineIndex: Int,
    line: AnnotatedString,
    foldClosing: Int?,
    isCollapsed: Boolean,
    colors: JsCodeColors,
    onToggle: () -> Unit,
) {
    val isFoldOpener = foldClosing != null
    val arrowRotation by animateFloatAsState(targetValue = if (!isCollapsed) 0f else -90f)

    CodeLine(
        lineNumber = lineIndex + 1,
        modifier = if (isFoldOpener) {
            Modifier.clickable(onClick = onToggle)
        } else {
            Modifier
        },
    ) {
        Row(
            modifier = Modifier
                .padding(top = 1.dp, bottom = 1.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.size(20.dp)) {
                if (isFoldOpener) {
                    Image(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colors.punctuationColor),
                        modifier = Modifier.fillMaxSize().rotate(arrowRotation),
                    )
                }
            }
            Text(
                text = if (isCollapsed) {
                    buildAnnotatedString {
                        append(line)
                        withStyle(SpanStyle(color = colors.commentColor)) { append(" ... ") }
                        withStyle(SpanStyle(color = colors.punctuationColor)) { append("}") }
                    }
                } else {
                    line
                },
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                softWrap = false,
            )
        }
    }
}

private fun computeVisibleIndices(
    totalLines: Int,
    folds: Map<Int, Int>,
    collapsed: SnapshotStateMap<Int, Boolean>,
): List<Int> {
    if (totalLines == 0) return emptyList()
    val out = ArrayList<Int>(totalLines)
    var i = 0
    while (i < totalLines) {
        out += i
        val close = folds[i]
        if (close != null && collapsed[i] == true) {
            i = close + 1
        } else {
            i++
        }
    }
    return out
}

// --------------------------------------------------------------------------------
// COLORS
// --------------------------------------------------------------------------------

internal data class JsCodeColors(
    val keywordColor: Color,
    val literalColor: Color,
    val stringColor: Color,
    val numberColor: Color,
    val commentColor: Color,
    val regexColor: Color,
    val operatorColor: Color,
    val punctuationColor: Color,
    val identifierColor: Color,
    val functionColor: Color,
)

internal object JsCodeDefaults {
    @Composable
    fun colors(
        keywordColor: Color = Color(0xFFE91E63),
        literalColor: Color = Color(0xFFE91E63),
        stringColor: Color = Color(0xFF4CAF50),
        numberColor: Color = Color(0xFFFF9800),
        commentColor: Color = LocalTextStyle.current.color.copy(alpha = 0.55f),
        regexColor: Color = Color(0xFFDA70D6),
        operatorColor: Color = Color(0xFF2E86C1),
        punctuationColor: Color = Color(0xFF2E86C1),
        identifierColor: Color = LocalTextStyle.current.color,
        functionColor: Color = Color(0xFF9C27B0),
    ) = JsCodeColors(
        keywordColor = keywordColor,
        literalColor = literalColor,
        stringColor = stringColor,
        numberColor = numberColor,
        commentColor = commentColor,
        regexColor = regexColor,
        operatorColor = operatorColor,
        punctuationColor = punctuationColor,
        identifierColor = identifierColor,
        functionColor = functionColor,
    )
}

// --------------------------------------------------------------------------------
// TOKENIZER + HIGHLIGHTER
// --------------------------------------------------------------------------------

private val JS_KEYWORDS = setOf(
    "break", "case", "catch", "class", "const", "continue", "debugger", "default",
    "delete", "do", "else", "export", "extends", "finally", "for", "function",
    "if", "import", "in", "instanceof", "let", "new", "of", "return", "super",
    "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with",
    "yield", "async", "await", "static", "get", "set",
)

private val JS_LITERALS = setOf("true", "false", "null", "undefined", "NaN", "Infinity")

private val JS_PUNCTUATION = setOf('{', '}', '(', ')', '[', ']', ',', ';', '.', ':')

private fun highlightJavaScript(code: String, colors: JsCodeColors): JsHighlighted {
    val tokens = tokenizeJavaScript(code)
    val lines = mutableListOf<AnnotatedString>()
    var current = AnnotatedString.Builder()
    var currentLine = 0
    val openStack = ArrayDeque<Int>()
    val folds = HashMap<Int, Int>()

    fun appendToken(text: String, color: Color) {
        current.withStyle(SpanStyle(color = color)) { append(text) }
    }

    for (token in tokens) {
        // Track folds at token boundary (before splitting on newlines).
        if (token.kind == JsTokenKind.PUNCTUATION) {
            when (token.text) {
                "{" -> openStack.addLast(currentLine)
                "}" -> {
                    val open = openStack.removeLastOrNull()
                    if (open != null && open != currentLine) folds[open] = currentLine
                }
            }
        }

        val parts = token.text.split('\n')
        parts.forEachIndexed { idx, part ->
            if (part.isNotEmpty()) {
                when (token.kind) {
                    JsTokenKind.WHITESPACE -> current.append(part)
                    JsTokenKind.KEYWORD -> appendToken(part, colors.keywordColor)
                    JsTokenKind.LITERAL -> appendToken(part, colors.literalColor)
                    JsTokenKind.STRING -> appendToken(part, colors.stringColor)
                    JsTokenKind.NUMBER -> appendToken(part, colors.numberColor)
                    JsTokenKind.COMMENT -> appendToken(part, colors.commentColor)
                    JsTokenKind.REGEX -> appendToken(part, colors.regexColor)
                    JsTokenKind.OPERATOR -> appendToken(part, colors.operatorColor)
                    JsTokenKind.PUNCTUATION -> appendToken(part, colors.punctuationColor)
                    JsTokenKind.FUNCTION -> appendToken(part, colors.functionColor)
                    JsTokenKind.IDENTIFIER -> appendToken(part, colors.identifierColor)
                }
            }
            if (idx < parts.lastIndex) {
                lines += current.toAnnotatedString()
                current = AnnotatedString.Builder()
                currentLine++
            }
        }
    }
    lines += current.toAnnotatedString()
    // Drop trailing empty line if input ended with a newline
    if (lines.isNotEmpty() && lines.last().text.isEmpty() && code.endsWith('\n')) {
        lines.removeAt(lines.lastIndex)
    }
    if (lines.isEmpty()) lines += AnnotatedString("")
    return JsHighlighted(lines = lines, folds = folds)
}

internal data class JsHighlighted(
    val lines: List<AnnotatedString>,
    val folds: Map<Int, Int>,
) {
    companion object {
        val EMPTY = JsHighlighted(emptyList(), emptyMap())
    }
}

private enum class JsTokenKind {
    WHITESPACE, KEYWORD, LITERAL, STRING, NUMBER, COMMENT, REGEX,
    OPERATOR, PUNCTUATION, IDENTIFIER, FUNCTION,
}

private data class JsToken(val text: String, val kind: JsTokenKind)

private fun tokenizeJavaScript(input: String): List<JsToken> {
    val tokens = mutableListOf<JsToken>()
    val len = input.length
    var i = 0
    // Tracks whether a `/` should start a regex literal vs. be a division operator.
    var regexAllowed = true

    fun emit(text: String, kind: JsTokenKind) {
        tokens += JsToken(text, kind)
        if (kind != JsTokenKind.WHITESPACE && kind != JsTokenKind.COMMENT) {
            regexAllowed = when (kind) {
                JsTokenKind.OPERATOR -> true
                JsTokenKind.PUNCTUATION -> text !in setOf(")", "]")
                JsTokenKind.KEYWORD -> text !in setOf("this", "super")
                else -> false
            }
        }
    }

    while (i < len) {
        val c = input[i]

        // Whitespace
        if (c.isWhitespace()) {
            val start = i
            while (i < len && input[i].isWhitespace()) i++
            tokens += JsToken(input.substring(start, i), JsTokenKind.WHITESPACE)
            continue
        }

        // Line comment
        if (c == '/' && i + 1 < len && input[i + 1] == '/') {
            val start = i
            while (i < len && input[i] != '\n') i++
            tokens += JsToken(input.substring(start, i), JsTokenKind.COMMENT)
            continue
        }

        // Block comment
        if (c == '/' && i + 1 < len && input[i + 1] == '*') {
            val start = i
            i += 2
            while (i < len && !(input[i] == '*' && i + 1 < len && input[i + 1] == '/')) i++
            if (i < len) i += 2
            tokens += JsToken(input.substring(start, i), JsTokenKind.COMMENT)
            continue
        }

        // String literals (single, double, template)
        if (c == '\'' || c == '"' || c == '`') {
            val quote = c
            val start = i
            i++
            while (i < len) {
                val ch = input[i]
                if (ch == '\\' && i + 1 < len) {
                    i += 2
                    continue
                }
                if (ch == quote) { i++; break }
                if (quote != '`' && ch == '\n') break
                i++
            }
            emit(input.substring(start, i), JsTokenKind.STRING)
            continue
        }

        // Regex literal
        if (c == '/' && regexAllowed) {
            val start = i
            i++
            var inClass = false
            while (i < len) {
                val ch = input[i]
                if (ch == '\\' && i + 1 < len) { i += 2; continue }
                if (ch == '[') inClass = true
                else if (ch == ']') inClass = false
                else if (ch == '/' && !inClass) { i++; break }
                else if (ch == '\n') break
                i++
            }
            // Flags
            while (i < len && input[i].isLetter()) i++
            emit(input.substring(start, i), JsTokenKind.REGEX)
            continue
        }

        // Number
        if (c.isDigit() || (c == '.' && i + 1 < len && input[i + 1].isDigit())) {
            val start = i
            if (c == '0' && i + 1 < len && (input[i + 1] == 'x' || input[i + 1] == 'X')) {
                i += 2
                while (i < len && (input[i].isDigit() || input[i] in 'a'..'f' || input[i] in 'A'..'F')) i++
            } else {
                while (i < len && (input[i].isDigit() || input[i] == '.' || input[i] == 'e' || input[i] == 'E' ||
                            ((input[i] == '+' || input[i] == '-') && i > start && (input[i - 1] == 'e' || input[i - 1] == 'E')))
                ) i++
            }
            if (i < len && input[i] == 'n') i++ // BigInt suffix
            emit(input.substring(start, i), JsTokenKind.NUMBER)
            continue
        }

        // Identifier / keyword / literal
        if (c.isLetter() || c == '_' || c == '$') {
            val start = i
            while (i < len && (input[i].isLetterOrDigit() || input[i] == '_' || input[i] == '$')) i++
            val text = input.substring(start, i)
            val kind = when {
                text in JS_KEYWORDS -> JsTokenKind.KEYWORD
                text in JS_LITERALS -> JsTokenKind.LITERAL
                else -> {
                    // Detect call-like identifier: followed by '(' (skipping whitespace)
                    var j = i
                    while (j < len && input[j].isWhitespace()) j++
                    if (j < len && input[j] == '(') JsTokenKind.FUNCTION else JsTokenKind.IDENTIFIER
                }
            }
            emit(text, kind)
            continue
        }

        // Punctuation
        if (c in JS_PUNCTUATION) {
            emit(c.toString(), JsTokenKind.PUNCTUATION)
            i++
            continue
        }

        // Operators (greedy, longest first)
        val opLengths = intArrayOf(4, 3, 2, 1)
        var matched = false
        for (l in opLengths) {
            if (i + l <= len) {
                val candidate = input.substring(i, i + l)
                if (candidate in JS_OPERATORS) {
                    emit(candidate, JsTokenKind.OPERATOR)
                    i += l
                    matched = true
                    break
                }
            }
        }
        if (!matched) {
            // Unknown char — emit as identifier to avoid losing input
            emit(c.toString(), JsTokenKind.IDENTIFIER)
            i++
        }
    }
    return tokens
}

private val JS_OPERATORS = setOf(
    ">>>=", "...", "===", "!==", ">>>", "**=", "<<=", ">>=", "&&=", "||=", "??=",
    "==", "!=", "<=", ">=", "&&", "||", "??", "?.", "=>", "++", "--", "<<", ">>",
    "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "**",
    "+", "-", "*", "/", "%", "=", "<", ">", "!", "~", "&", "|", "^", "?",
)

// --------------------------------------------------------------------------------
// BEAUTIFIER
// --------------------------------------------------------------------------------

/**
 * Light-weight JavaScript pretty-printer. Re-formats a (possibly minified)
 * source so that braces / semicolons start a new indented line. Strings,
 * template literals, regex and comments are passed through verbatim.
 */
private fun beautifyJavaScript(input: String): String {
    if (input.isEmpty()) return input
    val sb = StringBuilder(input.length + 64)
    var indent = 0
    val indentUnit = "  "

    fun newline() {
        // Trim trailing spaces on the current line.
        while (sb.isNotEmpty() && (sb.last() == ' ' || sb.last() == '\t')) sb.deleteAt(sb.lastIndex)
        sb.append('\n')
        repeat(indent) { sb.append(indentUnit) }
    }

    fun lastNonSpace(): Char? {
        for (k in sb.lastIndex downTo 0) {
            val ch = sb[k]
            if (ch != ' ' && ch != '\t') return ch
        }
        return null
    }

    val len = input.length
    var i = 0
    var regexAllowed = true

    while (i < len) {
        val c = input[i]

        // Preserve line + block comments verbatim.
        if (c == '/' && i + 1 < len && input[i + 1] == '/') {
            if (sb.isNotEmpty() && lastNonSpace() != null && sb.last() != '\n' && sb.last() != ' ') sb.append(' ')
            val start = i
            while (i < len && input[i] != '\n') i++
            sb.append(input, start, i)
            continue
        }
        if (c == '/' && i + 1 < len && input[i + 1] == '*') {
            val start = i
            i += 2
            while (i < len && !(input[i] == '*' && i + 1 < len && input[i + 1] == '/')) i++
            if (i < len) i += 2
            sb.append(input, start, i)
            continue
        }

        // Strings / template literals — copy verbatim.
        if (c == '\'' || c == '"' || c == '`') {
            val quote = c
            val start = i
            i++
            while (i < len) {
                val ch = input[i]
                if (ch == '\\' && i + 1 < len) { i += 2; continue }
                if (ch == quote) { i++; break }
                if (quote != '`' && ch == '\n') break
                i++
            }
            sb.append(input, start, i)
            regexAllowed = false
            continue
        }

        // Regex literal.
        if (c == '/' && regexAllowed) {
            val start = i
            i++
            var inClass = false
            while (i < len) {
                val ch = input[i]
                if (ch == '\\' && i + 1 < len) { i += 2; continue }
                if (ch == '[') inClass = true
                else if (ch == ']') inClass = false
                else if (ch == '/' && !inClass) { i++; break }
                else if (ch == '\n') break
                i++
            }
            while (i < len && input[i].isLetter()) i++
            sb.append(input, start, i)
            regexAllowed = false
            continue
        }

        when (c) {
            '\n', '\r' -> {
                // Collapse existing newlines; we'll re-emit our own.
                while (i < len && (input[i] == '\n' || input[i] == '\r')) i++
                if (sb.isNotEmpty() && sb.last() != '\n') {
                    val last = lastNonSpace()
                    // Avoid breaking lines mid-expression unless we just closed a statement.
                    if (last == ';' || last == '{' || last == '}') newline() else sb.append(' ')
                }
            }
            ' ', '\t' -> {
                if (sb.isNotEmpty() && sb.last() != ' ' && sb.last() != '\n') sb.append(' ')
                i++
            }
            '{' -> {
                if (sb.isNotEmpty() && sb.last() != ' ' && sb.last() != '\n' && sb.last() != '(' && sb.last() != '[') {
                    sb.append(' ')
                }
                sb.append('{')
                indent++
                newline()
                i++
                regexAllowed = true
            }
            '}' -> {
                indent = maxOf(0, indent - 1)
                if (sb.isNotEmpty() && sb.last() != '\n') newline()
                sb.append('}')
                i++
                // Look ahead: if followed by `else`, `catch`, `finally`, `,`, `)`, `]`, `;`, keep on same line group.
                var j = i
                while (j < len && input[j].isWhitespace()) j++
                val next = if (j < len) input[j] else ' '
                regexAllowed = next != '.' && next != '('
                if (next == ';' || next == ',' || next == ')' || next == ']' || next == '.') {
                    // continue on same line; the trailing punctuation will follow naturally.
                } else if (next != '\u0000') {
                    // Start of a new statement after closing brace.
                    if (j < len) newline()
                }
            }
            ';' -> {
                sb.append(';')
                i++
                // Skip any trailing whitespace.
                while (i < len && (input[i] == ' ' || input[i] == '\t')) i++
                // Don't break inside `for(;;)` heads.
                if (insideForHeader(sb)) {
                    if (i < len && input[i] != ')') sb.append(' ')
                } else if (i < len && input[i] != '}' && input[i] != '\n' && input[i] != '\r') {
                    newline()
                }
                regexAllowed = true
            }
            ',' -> {
                sb.append(", ")
                i++
                // Skip duplicate whitespace.
                while (i < len && (input[i] == ' ' || input[i] == '\t')) i++
                regexAllowed = true
            }
            else -> {
                sb.append(c)
                regexAllowed = when (c) {
                    '(', '[', '=', '+', '-', '*', '/', '%', '&', '|', '^', '!',
                    '~', '?', ':', '<', '>' -> true
                    else -> false
                }
                i++
            }
        }
    }

    // Trim trailing blank lines.
    while (sb.isNotEmpty() && (sb.last() == '\n' || sb.last() == ' ')) sb.deleteAt(sb.lastIndex)
    return sb.toString()
}

/** Heuristic: are we currently inside a `for(...)` header (so `;` should NOT line-break)? */
private fun insideForHeader(sb: StringBuilder): Boolean {
    var depth = 0
    for (k in sb.lastIndex downTo 0) {
        when (sb[k]) {
            ')' -> depth++
            '(' -> {
                if (depth == 0) {
                    // Check the keyword right before this '('.
                    var j = k - 1
                    while (j >= 0 && sb[j] == ' ') j--
                    if (j >= 2 && sb[j] == 'r' && sb[j - 1] == 'o' && sb[j - 2] == 'f') {
                        val prev = if (j - 3 >= 0) sb[j - 3] else ' '
                        return !prev.isLetterOrDigit() && prev != '_' && prev != '$'
                    }
                    return false
                }
                depth--
            }
            '\n' -> if (depth == 0) return false
        }
    }
    return false
}



