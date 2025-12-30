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
    onError: (Throwable) -> Unit = {}
) {
    var rootElement by remember(xml) { mutableStateOf<Element?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(xml) {
        try {
            // Parse XML using Ksoup with the XML Parser
            val document: Document = Ksoup.parseXml(xml, "")
            rootElement = document
            error = null
        } catch (e: Exception) {
            error = e.message
            onError(e)
        }
    }

    Box(modifier = modifier.padding(contentPadding)) {
        if (error == null) {
            rootElement?.let { root ->
                // SelectionContainer allows users to copy text
                SelectionContainer {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        // Render children of the document (usually the root tag)
                        root.children().forEach { child ->
                            XmlNodeView(
                                node = child,
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
}

/**
 * Recursive component to render a single XML node (Element or Text).
 */
@Composable
private fun XmlNodeView(
    node: Node,
    colors: XmlTreeColors,
    depth: Int,
    isInitiallyExpanded: Boolean
) {
    val indentation = 16.dp // Indentation per level

    when (node) {
        is Element -> {
            val hasChildren = node.childNodeSize() > 0
            // If it has children, we track expanded state.
            // Empty tags <tag /> are not expandable.
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
                        modifier = Modifier.padding(start = 4.dp)
                    )
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
                                isInitiallyExpanded = isInitiallyExpanded
                            )
                        }

                        // Closing tag: </TagName> (Only visible if expanded)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        is TextNode -> {
            val text = node.text().trim()
            if (text.isNotEmpty()) {
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
                        text = text,
                        color = colors.textColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
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