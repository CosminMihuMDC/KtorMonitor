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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import ro.cosminmihu.ktor.monitor.ui.VerticalScrollbarBox
import ro.cosminmihu.ktor.monitor.ui.icons.Icons
import ro.cosminmihu.ktor.monitor.ui.icons.filled.ArrowDropDown
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Attribute
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.cosminmihu.ktor.monitor.ui.detail.body.CodeLine
import ro.cosminmihu.ktor.monitor.ui.detail.body.LocalMaxLineNumber

/**
 * Entry point for the XmlTree.
 * Parses the XML string and displays the tree.
 *
 * @param xml The XML content string.
 * @param modifier Modifier for the root layout.
 * @param colors Custom colors for the tree.
 * @param contentPadding Padding for the container.
 * @param onError Callback for parsing errors.
 */
@Composable
internal fun XmlTree(
    xml: String,
    modifier: Modifier = Modifier,
    colors: XmlTreeColors = XmlTreeDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    initialExpanded: Boolean = false,
    verticalScroll: Boolean = true,
    onError: (Throwable) -> Unit = {}
) {
    var rootElement by remember(xml) { mutableStateOf<Element?>(null) }
    var lineNumbers by remember(xml) { mutableStateOf<Map<Node, IntArray>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(xml) {
        try {
            val document: Document = withContext(Dispatchers.Default) { Ksoup.parseXml(xml, "") }
            rootElement = document
            lineNumbers = withContext(Dispatchers.Default) {
                buildXmlLineNumbers(document.children())
            }
            error = null
        } catch (e: Exception) {
            error = e.message
            onError(e)
        }
    }

    val rootElements = rootElement?.children()
    if (error != null || rootElements.isNullOrEmpty()) return

    val maxLine = remember(lineNumbers) {
        var m = 0
        for (arr in lineNumbers.values) {
            if (arr.size >= 1 && arr[0] > m) m = arr[0]
            if (arr.size >= 2 && arr[1] > m) m = arr[1]
        }
        m
    }

    SelectionContainer {
        val scrollState = rememberScrollState()
        val hScrollState = rememberScrollState()
        VerticalScrollbarBox(scrollState, modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .then(if (verticalScroll) Modifier.verticalScroll(scrollState) else Modifier)
                    .horizontalScroll(hScrollState)
                    .padding(contentPadding),
            ) {
                CompositionLocalProvider(LocalMaxLineNumber provides maxLine) {
                    rootElements.forEach { child ->
                        XmlNodeView(
                            node = child,
                            colors = colors,
                            depth = 0,
                            isInitiallyExpanded = initialExpanded,
                            lineNumbers = lineNumbers,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pre-computes a stable `[opening, closing]` line number for every node that
 * will ever be rendered. Closing line is `0` for self-closing elements and
 * text nodes. Numbers are kept across collapse/expand toggles so the gutter
 * behaves like code folding.
 */
private fun buildXmlLineNumbers(roots: List<Node>): Map<Node, IntArray> {
    val map = HashMap<Node, IntArray>()
    var counter = 0
    fun visit(node: Node) {
        when (node) {
            is Element -> {
                val opening = ++counter
                val children = node.childNodes()
                if (children.isEmpty()) {
                    map[node] = intArrayOf(opening, 0)
                } else {
                    children.forEach(::visit)
                    val closing = ++counter
                    map[node] = intArrayOf(opening, closing)
                }
            }
            is TextNode -> {
                if (node.text().trim().isNotEmpty()) {
                    map[node] = intArrayOf(++counter, 0)
                }
            }
            else -> Unit
        }
    }
    roots.forEach(::visit)
    return map
}

/**
 * Recursive component to render a single XML node (Element or Text).
 */
@Composable
private fun XmlNodeView(
    node: Node,
    colors: XmlTreeColors,
    depth: Int,
    isInitiallyExpanded: Boolean,
    lineNumbers: Map<Node, IntArray>,
) {
    val indentation = 16.dp // Indentation per level
    val numbers = lineNumbers[node]
    val openingLine = numbers?.getOrNull(0) ?: 0
    val closingLine = numbers?.getOrNull(1) ?: 0

    when (node) {
        is Element -> {
            val hasChildren = node.childNodeSize() > 0
            // If it has children, we track expanded state.
            // Empty tags <tag /> are not expandable.
            var isExpanded by remember { mutableStateOf(isInitiallyExpanded) }
            val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 0f else -90f)

            Column {
                CodeLine(
                    lineNumber = openingLine,
                    modifier = Modifier
                        .clickable(enabled = hasChildren) { isExpanded = !isExpanded },
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = (indentation * depth), top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Expand/Collapse Icon
                        Box(modifier = Modifier.size(24.dp)) {
                            if (hasChildren) {
                                Image(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Expand/Collapse",
                                    colorFilter = ColorFilter.tint(colors.arrowColor),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .rotate(arrowRotation)
                                )
                            }
                        }

                        // The opening tag: <TagName attr="val">
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = colors.tagColor)) {
                                    append("<${node.tagName()}")
                                }

                                // Attributes
                                node.attributes().forEach { attr: Attribute ->
                                    append(" ")
                                    withStyle(SpanStyle(color = colors.attributeKeyColor)) {
                                        append(attr.key)
                                    }
                                    withStyle(SpanStyle(color = colors.symbolColor)) {
                                        append("=")
                                    }
                                    withStyle(SpanStyle(color = colors.attributeValueColor)) {
                                        append("\"${attr.value}\"")
                                    }
                                }

                                withStyle(SpanStyle(color = colors.tagColor)) {
                                    if (!hasChildren) append("/>") else append(">")
                                }

                                // Summary text for collapsed state (optional optimization)
                                if (hasChildren && !isExpanded) {
                                    withStyle(SpanStyle(color = colors.symbolColor)) {
                                        append(" ... ")
                                    }
                                    withStyle(SpanStyle(color = colors.tagColor)) {
                                        append("</${node.tagName()}>")
                                    }
                                }
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            softWrap = false,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Children (Recursive Call)
                AnimatedVisibility(
                    visible = isExpanded && hasChildren,
                ) {
                    Column {
                        node.childNodes().forEach { child ->
                            XmlNodeView(
                                node = child,
                                colors = colors,
                                depth = depth + 1,
                                isInitiallyExpanded = isInitiallyExpanded,
                                lineNumbers = lineNumbers,
                            )
                        }

                        // Closing tag: </TagName> (Only visible if expanded)
                        CodeLine(
                            lineNumber = closingLine,
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(
                                        start = (indentation * depth) + 24.dp + 4.dp,
                                        bottom = 2.dp
                                    )
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = colors.tagColor)) {
                                            append("</${node.tagName()}>")
                                        }
                                    },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        is TextNode -> {
            val text = node.text().trim()
            if (text.isNotEmpty()) {
                CodeLine(
                    lineNumber = openingLine,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(
                                start = (indentation * depth) + 24.dp + 4.dp,
                                top = 1.dp,
                                bottom = 1.dp
                            )
                    ) {
                        Text(
                            text = text,
                            color = colors.textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Data class defining the color palette for the XmlTree.
 */
internal data class XmlTreeColors(
    val tagColor: Color,
    val attributeKeyColor: Color,
    val attributeValueColor: Color,
    val textColor: Color,
    val arrowColor: Color,
    val symbolColor: Color
)

internal object XmlTreeDefaults {
    @Composable
    fun colors(
        tagColor: Color = Color(0xFFE91E63),
        attributeKeyColor: Color = Color(0xFF9C27B0),
        attributeValueColor: Color = Color(0xFF4CAF50),
        textColor: Color = LocalTextStyle.current.color,
        arrowColor: Color = Color(0xFF2E86C1),
        symbolColor: Color = Color(0xFF2E86C1),
    ) = XmlTreeColors(
        tagColor = tagColor,
        attributeKeyColor = attributeKeyColor,
        attributeValueColor = attributeValueColor,
        textColor = textColor,
        arrowColor = arrowColor,
        symbolColor = symbolColor
    )
}