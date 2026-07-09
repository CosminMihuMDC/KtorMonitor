package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
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
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.svg.SvgDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.ui.detail.body.CodeLine
import ro.cosminmihu.ktor.monitor.ui.detail.body.LocalMaxLineNumber

// --------------------------------------------------------------------------------
// PUBLIC API
// --------------------------------------------------------------------------------

/**
 * Renders a multipart body (e.g. `multipart/form-data`) as a tree of parts.
 * Each part shows its Content-Disposition (name / filename), Content-Type and
 * body preview, and can be collapsed. Image parts are rendered inline.
 *
 * The boundary is auto-detected from the first line that starts with `--`.
 *
 * @param bytes Optional space-separated decimal byte string of the raw body
 *   (from `DetailUiState.Body.bytes`). When provided, binary image parts are
 *   extracted at the byte level and shown as inline previews.
 */
@Composable
internal fun Multipart(
    body: String,
    bytes: String? = null,
    modifier: Modifier = Modifier,
    colors: MultipartColors = MultipartDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    initialExpanded: Boolean = true,
    onError: (Throwable) -> Unit = {}
) {
    var parts by remember(body) { mutableStateOf<List<MultipartPart>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(body, bytes) {
        try {
            parts = withContext(Dispatchers.Default) {
                val rawParts = parseMultipart(body)
                val bodyBytes = bytes?.let { parseBodyBytesString(it) }
                if (bodyBytes != null) enrichPartsWithImageBytes(rawParts, body, bodyBytes)
                else rawParts
            }
            error = null
        } catch (e: Exception) {
            error = e.message
            onError(e)
        }
    }

    if (error != null || parts.isEmpty()) return

    val rows = remember(parts) { parts.assignLineNumbers() }
    val maxLine = remember(rows) { rows.maxOfOrNull { it.maxLine } ?: 0 }

    val collapsed = remember(rows) { mutableStateMapOf<Int, Boolean>() }
    if (!initialExpanded) {
        remember(rows) { rows.forEach { collapsed[it.openingLine] = true } }
    }

    val visible by remember(rows) {
        derivedStateOf { flattenMultipart(rows, collapsed) }
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
                            items = visible,
                            key = { _, row -> row.id },
                            contentType = { _, row -> row.kind.name },
                        ) { _, row ->
                            MultipartRowView(
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
private fun MultipartRowView(
    row: MultipartRow,
    colors: MultipartColors,
    onToggle: (Int) -> Unit,
) {
    val indentation = 16.dp

    when (row.kind) {
        MultipartRowKind.IMAGE -> {
            CodeLine(
                lineNumber = row.lineNumber,
            ) {
                Box(
                    modifier = Modifier
                        .padding(
                            start = indentation * row.depth + 24.dp + 4.dp,
                            top = 4.dp,
                            bottom = 4.dp,
                        ),
                ) {
                    val context = LocalPlatformContext.current
                    val imageLoader = remember(context) {
                        ImageLoader.Builder(context)
                            .components { add(SvgDecoder.Factory()) }
                            .build()
                    }
                    AsyncImage(
                        model = row.imageBytes,
                        imageLoader = imageLoader,
                        contentDescription = null,
                        modifier = Modifier.heightIn(max = 200.dp),
                    )
                }
            }
        }

        MultipartRowKind.PART_OPEN -> {
            val arrowRotation by animateFloatAsState(targetValue = if (!row.isCollapsed) 0f else -90f)
            CodeLine(
                lineNumber = row.lineNumber,
                modifier = Modifier
                    .clickable { onToggle(row.id) },
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
                            withStyle(SpanStyle(color = colors.partColor)) { append("Part ${row.partIndex}") }
                            if (!row.partName.isNullOrBlank()) {
                                withStyle(SpanStyle(color = colors.symbolColor)) { append(" — ") }
                                withStyle(SpanStyle(color = colors.keyColor)) { append(row.partName) }
                            }
                            if (!row.partFilename.isNullOrBlank()) {
                                withStyle(SpanStyle(color = colors.symbolColor)) { append(" (") }
                                withStyle(SpanStyle(color = colors.fileColor)) { append(row.partFilename) }
                                withStyle(SpanStyle(color = colors.symbolColor)) { append(")") }
                            }
                            if (row.isCollapsed) {
                                withStyle(SpanStyle(color = colors.commentColor)) { append("  …") }
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

        MultipartRowKind.HEADER -> {
            CodeLine(
                lineNumber = row.lineNumber,
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth + 24.dp + 4.dp, top = 1.dp, bottom = 1.dp),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.headerColor)) { append(row.text.orEmpty()) }
                            withStyle(SpanStyle(color = colors.symbolColor)) { append(": ") }
                            withStyle(SpanStyle(color = colors.valueColor)) { append(row.value.orEmpty()) }
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        softWrap = false,
                    )
                }
            }
        }

        MultipartRowKind.BODY_LINE -> {
            CodeLine(
                lineNumber = row.lineNumber,
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = indentation * row.depth + 24.dp + 4.dp, top = 1.dp, bottom = 1.dp),
                ) {
                    Text(
                        text = row.text.orEmpty(),
                        color = colors.valueColor,
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

private enum class MultipartRowKind { PART_OPEN, HEADER, BODY_LINE, IMAGE }

private data class MultipartRow(
    val id: Int,
    val kind: MultipartRowKind,
    val depth: Int,
    val lineNumber: Int,
    val partIndex: Int = 0,
    val partName: String? = null,
    val partFilename: String? = null,
    val text: String? = null,
    val value: String? = null,
    val isCollapsed: Boolean = false,
    val imageBytes: ByteArray? = null,
)

private data class NumberedPart(
    val part: MultipartPart,
    val openingLine: Int,
    val headerLines: List<Int>,
    val bodyLines: List<Int>,
) {
    val maxLine: Int
        get() = maxOf(openingLine, headerLines.maxOrNull() ?: 0, bodyLines.maxOrNull() ?: 0)
}

private fun List<MultipartPart>.assignLineNumbers(): List<NumberedPart> {
    var counter = 0
    return map { part ->
        val opening = ++counter
        val headerLines = part.headers.map { ++counter }
        val bodyLines = when {
            part.imageBytes != null -> listOf(++counter)  // single image row
            part.bodyPreview.isEmpty() -> emptyList()
            else -> part.bodyPreview.map { ++counter }
        }
        NumberedPart(part, opening, headerLines, bodyLines)
    }
}

private fun flattenMultipart(
    parts: List<NumberedPart>,
    collapsed: SnapshotStateMap<Int, Boolean>,
): List<MultipartRow> {
    val out = ArrayList<MultipartRow>()
    parts.forEachIndexed { index, p ->
        val isCollapsed = collapsed[p.openingLine] == true
        out += MultipartRow(
            id = p.openingLine,
            kind = MultipartRowKind.PART_OPEN,
            depth = 0,
            lineNumber = p.openingLine,
            partIndex = index + 1,
            partName = p.part.name,
            partFilename = p.part.filename,
            isCollapsed = isCollapsed,
        )
        if (isCollapsed) return@forEachIndexed

        p.part.headers.forEachIndexed { hi, header ->
            out += MultipartRow(
                id = p.headerLines[hi],
                kind = MultipartRowKind.HEADER,
                depth = 1,
                lineNumber = p.headerLines[hi],
                text = header.first,
                value = header.second,
            )
        }

        if (p.part.imageBytes != null) {
            // Render the image inline instead of raw text body lines.
            val imgLine = p.bodyLines.firstOrNull() ?: return@forEachIndexed
            out += MultipartRow(
                id = imgLine + BODY_ID_OFFSET,
                kind = MultipartRowKind.IMAGE,
                depth = 1,
                lineNumber = imgLine,
                imageBytes = p.part.imageBytes,
            )
        } else {
            p.part.bodyPreview.forEachIndexed { bi, line ->
                out += MultipartRow(
                    id = p.bodyLines[bi] + BODY_ID_OFFSET,
                    kind = MultipartRowKind.BODY_LINE,
                    depth = 1,
                    lineNumber = p.bodyLines[bi],
                    text = line,
                )
            }
        }
    }
    return out
}

private const val BODY_ID_OFFSET = 1_000_000

// --------------------------------------------------------------------------------
// PARSER
// --------------------------------------------------------------------------------

internal data class MultipartPart(
    val headers: List<Pair<String, String>>,
    val name: String?,
    val filename: String?,
    val contentType: String? = null,
    val bodyPreview: List<String>,
    val imageBytes: ByteArray? = null,
)

private const val MAX_BODY_LINES = 20
private const val MAX_BODY_LINE_LEN = 240

private fun parseMultipart(input: String): List<MultipartPart> {
    if (input.isBlank()) return emptyList()

    // Detect boundary: the first line starting with "--".
    val lines = input.split("\r\n", "\n")
    val first = lines.firstOrNull { it.startsWith("--") } ?: return emptyList()
    val boundary = first.removePrefix("--").trimEnd('-').trim()
    if (boundary.isEmpty()) return emptyList()

    val delimiter = "--$boundary"
    val closeDelimiter = "--$boundary--"

    val parts = mutableListOf<MultipartPart>()
    var i = 0
    var insidePart = false
    val current = StringBuilder()

    fun flush() {
        if (!insidePart) return
        val raw = current.toString().trimEnd('\r', '\n')
        parts += parsePart(raw)
        current.clear()
        insidePart = false
    }

    while (i < lines.size) {
        val line = lines[i]
        when {
            line == closeDelimiter -> {
                flush()
                break
            }
            line == delimiter -> {
                flush()
                insidePart = true
            }
            insidePart -> {
                if (current.isNotEmpty()) current.append('\n')
                current.append(line)
            }
        }
        i++
    }
    flush()
    return parts
}

private fun parsePart(raw: String): MultipartPart {
    val headerEnd = raw.indexOf("\n\n").let { if (it == -1) raw.indexOf("\r\n\r\n") else it }
    val (headerBlock, body) = if (headerEnd == -1) {
        raw to ""
    } else {
        raw.substring(0, headerEnd) to raw.substring(headerEnd).trimStart('\r', '\n')
    }

    val headers = headerBlock.lines()
        .mapNotNull { line ->
            val idx = line.indexOf(':')
            if (idx <= 0) null else line.substring(0, idx).trim() to line.substring(idx + 1).trim()
        }

    val disposition = headers.firstOrNull { it.first.equals("Content-Disposition", true) }?.second.orEmpty()
    val name = extractParam(disposition, "name")
    val filename = extractParam(disposition, "filename")
    val contentType = headers.firstOrNull { it.first.equals("Content-Type", true) }?.second

    val bodyPreview = if (body.isEmpty()) emptyList()
    else body.split('\n')
        .take(MAX_BODY_LINES)
        .map { if (it.length > MAX_BODY_LINE_LEN) it.substring(0, MAX_BODY_LINE_LEN) + "…" else it }
        .let { previews ->
            val total = body.split('\n').size
            if (total > MAX_BODY_LINES) previews + "… (${total - MAX_BODY_LINES} more lines)"
            else previews
        }

    return MultipartPart(
        headers = headers,
        name = name,
        filename = filename,
        contentType = contentType,
        bodyPreview = bodyPreview,
    )
}

private fun extractParam(header: String, name: String): String? {
    val pattern = Regex("""${Regex.escape(name)}\s*=\s*"?([^";]+)"?""", RegexOption.IGNORE_CASE)
    return pattern.find(header)?.groupValues?.getOrNull(1)?.trim()
}

// --------------------------------------------------------------------------------
// BYTE-LEVEL IMAGE EXTRACTION
// --------------------------------------------------------------------------------

/**
 * Parses a space-separated decimal byte string (produced by `ByteArray.toBytesString`) back
 * into a [ByteArray].  Returns `null` if the string is blank or unparseable.
 */
internal fun parseBodyBytesString(bytesStr: String): ByteArray? {
    if (bytesStr.isBlank()) return null
    return try {
        val tokens = bytesStr.split(' ')
        ByteArray(tokens.size) { i -> tokens[i].toInt().toByte() }
    } catch (_: Exception) {
        null
    }
}

/**
 * Enriches [parts] with [MultipartPart.imageBytes] by locating each part's body at the byte
 * level inside [bodyBytes].  Both text-based (SVG) and binary (PNG, JPEG, …) images are
 * handled correctly.
 */
private fun enrichPartsWithImageBytes(
    parts: List<MultipartPart>,
    body: String,
    bodyBytes: ByteArray,
): List<MultipartPart> {
    // Detect boundary from the decoded string (same as parseMultipart).
    val lines = body.split("\r\n", "\n")
    val first = lines.firstOrNull { it.startsWith("--") } ?: return parts
    val boundary = first.removePrefix("--").trimEnd('-').trim()
    if (boundary.isEmpty()) return parts

    val boundaryBytes = "--$boundary".encodeToByteArray()
    val positions = findAllOccurrences(bodyBytes, boundaryBytes)

    return parts.mapIndexed { index, part ->
        val contentType = part.contentType ?: return@mapIndexed part
        if (!contentType.startsWith("image/", ignoreCase = true)) return@mapIndexed part

        val partStart = positions.getOrNull(index)
        val nextPartStart = positions.getOrNull(index + 1)

        val imageBytes = extractPartBodyBytes(bodyBytes, boundaryBytes, partStart, nextPartStart)
        part.copy(imageBytes = imageBytes)
    }
}

private fun extractPartBodyBytes(
    bodyBytes: ByteArray,
    boundaryBytes: ByteArray,
    partStart: Int?,
    nextPartStart: Int?,
): ByteArray? {
    if (partStart == null) return null

    // Skip past "--boundary\r\n"
    var pos = partStart + boundaryBytes.size
    if (pos < bodyBytes.size - 1 &&
        bodyBytes[pos] == 0x0D.toByte() && bodyBytes[pos + 1] == 0x0A.toByte()
    ) pos += 2 // consume \r\n

    val partEnd = if (nextPartStart != null) {
        // Trim the trailing \r\n before the next boundary.
        (nextPartStart - 2).coerceAtLeast(pos)
    } else {
        bodyBytes.size
    }

    val partSlice = bodyBytes.copyOfRange(pos, partEnd)

    // Find the double-CRLF (or double-LF) header separator.
    val doubleCrlf = byteArrayOf(0x0D, 0x0A, 0x0D, 0x0A)
    val doubleLf = byteArrayOf(0x0A, 0x0A)
    val headerEnd = findSequence(partSlice, doubleCrlf)
    val bodyOffset = when {
        headerEnd != null -> headerEnd + doubleCrlf.size
        else -> (findSequence(partSlice, doubleLf) ?: return null) + doubleLf.size
    }

    if (bodyOffset >= partSlice.size) return null
    return partSlice.copyOfRange(bodyOffset, partSlice.size)
}

private fun findAllOccurrences(haystack: ByteArray, needle: ByteArray): List<Int> {
    val result = mutableListOf<Int>()
    var i = 0
    outer@ while (i <= haystack.size - needle.size) {
        for (j in needle.indices) {
            if (haystack[i + j] != needle[j]) { i++; continue@outer }
        }
        result += i
        i += needle.size
    }
    return result
}

private fun findSequence(haystack: ByteArray, needle: ByteArray): Int? {
    for (i in 0..(haystack.size - needle.size)) {
        var match = true
        for (j in needle.indices) {
            if (haystack[i + j] != needle[j]) { match = false; break }
        }
        if (match) return i
    }
    return null
}

// --------------------------------------------------------------------------------
// COLORS
// --------------------------------------------------------------------------------

internal data class MultipartColors(
    val partColor: Color,
    val keyColor: Color,
    val fileColor: Color,
    val headerColor: Color,
    val valueColor: Color,
    val symbolColor: Color,
    val arrowColor: Color,
    val commentColor: Color,
)

internal object MultipartDefaults {
    @Composable
    fun colors(
        partColor: Color = Color(0xFFE91E63),
        keyColor: Color = Color(0xFFFF9800),
        fileColor: Color = Color(0xFF9C27B0),
        headerColor: Color = Color(0xFF03A9F4),
        valueColor: Color = LocalTextStyle.current.color,
        symbolColor: Color = Color(0xFF2E86C1),
        arrowColor: Color = Color(0xFF2E86C1),
        commentColor: Color = LocalTextStyle.current.color.copy(alpha = 0.55f),
    ) = MultipartColors(
        partColor = partColor,
        keyColor = keyColor,
        fileColor = fileColor,
        headerColor = headerColor,
        valueColor = valueColor,
        symbolColor = symbolColor,
        arrowColor = arrowColor,
        commentColor = commentColor,
    )
}
