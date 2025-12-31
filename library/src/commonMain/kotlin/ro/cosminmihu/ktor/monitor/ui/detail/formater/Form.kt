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
import io.ktor.http.decodeURLPart

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
            rootNodes = FormParser.parse(body)
            error = null
        } catch (e: Exception) {
            error = e.message
            onError(e)
        }
    }

    if (error != null || rootNodes.isEmpty()) return

    SelectionContainer {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
        ) {
            rootNodes.forEach { node ->
                FormNodeView(
                    node = node,
                    colors = colors,
                    depth = 0,
                    isInitiallyExpanded = initialExpanded
                )
            }
        }
    }
}

// --------------------------------------------------------------------------------
// UI COMPONENTS
// --------------------------------------------------------------------------------

@Composable
private fun FormNodeView(
    node: FormNode,
    colors: FormTreeColors,
    depth: Int,
    isInitiallyExpanded: Boolean
) {
    val indentation = 20.dp

    val children = when (node) {
        is FormNode.Parent -> node.children
        is FormNode.Leaf -> emptyList()
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
            // Expand Icon
            Box(modifier = Modifier.size(24.dp)) {
                if (hasChildren) {
                    Image(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand/Collapse",
                        colorFilter = ColorFilter.tint(colors.arrowColor),
                        modifier = Modifier.fillMaxSize().rotate(arrowRotation)
                    )
                }
            }

            // Node Content
            Text(
                text = buildAnnotatedString {
                    // Key
                    withStyle(SpanStyle(color = colors.keyColor)) {
                        append(node.key)
                    }

                    when (node) {
                        is FormNode.Parent -> {
                            // Visual hint for structure
                            withStyle(SpanStyle(color = colors.punctuationColor)) {
                                append(if (node.isArray) " [" else " {")
                            }
                            if (!isExpanded) {
                                withStyle(SpanStyle(color = colors.commentColor)) {
                                    append(" ... ")
                                }
                                withStyle(SpanStyle(color = colors.punctuationColor)) {
                                    append(if (node.isArray) "]" else "}")
                                }
                            }
                        }

                        is FormNode.Leaf -> {
                            withStyle(SpanStyle(color = colors.punctuationColor)) {
                                append(" = ")
                            }
                            withStyle(SpanStyle(color = colors.valueColor)) {
                                append(node.value)
                            }
                        }
                    }
                },
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Children Recursive Render
        AnimatedVisibility(visible = isExpanded && hasChildren) {
            Column {
                children.forEach { child ->
                    FormNodeView(child, colors, depth + 1, isInitiallyExpanded)
                }

                // Closing brackets for parents
                if (node is FormNode.Parent) {
                    Row(modifier = Modifier.padding(start = (indentation * depth) + 24.dp + 4.dp)) {
                        Text(
                            text = if (node.isArray) "]" else "}",
                            color = colors.punctuationColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------
// DATA MODELS & COLORS
// --------------------------------------------------------------------------------

internal sealed class FormNode {
    abstract val key: String

    data class Leaf(override val key: String, val value: String) : FormNode()
    data class Parent(
        override val key: String,
        val children: List<FormNode>,
        val isArray: Boolean = false
    ) : FormNode()
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
        keyColor: Color = Color(0xFFFF9800),     // Orange
        valueColor: Color = Color(0xFF4CAF50),   // Green
        punctuationColor: Color = Color.Gray,
        arrowColor: Color = Color.Gray,
        commentColor: Color = Color.LightGray
    ) = FormTreeColors(
        keyColor = keyColor,
        valueColor = valueColor,
        punctuationColor = punctuationColor,
        arrowColor = arrowColor,
        commentColor = commentColor
    )
}

// --------------------------------------------------------------------------------
// PARSER LOGIC (Kotlin Idiomatic)
// --------------------------------------------------------------------------------

internal object FormParser {
    fun parse(body: String): List<FormNode> {
        if (body.isBlank()) return emptyList()

        // 1. Split and Decode plain pairs
        val pairs = body.split("&").mapNotNull { part ->
            val eqIndex = part.indexOf('=')
            if (eqIndex == -1) return@mapNotNull null // Skip invalid parts

            val rawKey = part.substring(0, eqIndex)
            val rawValue = part.substring(eqIndex + 1)

            try {
                // Use Kotlin's Charsets.UTF_8
                val key = rawKey.decodeURLPart()
                val value = rawValue.decodeURLPart()
                key to value
            } catch (_: Exception) {
                null
            }
        }

        // 2. Build Tree Hierarchy
        return buildTree(pairs)
    }

    private fun buildTree(pairs: List<Pair<String, String>>): List<FormNode> {
        // Map<Key, Any> where Any is String (Leaf) or MutableMap (Parent)
        val rootMap = mutableMapOf<String, Any>()

        for ((fullKey, value) in pairs) {
            // Split "user[address][city]" -> ["user", "address", "city"]
            val parts = fullKey.split("[", "]").filter { it.isNotEmpty() }

            if (parts.isEmpty()) continue

            var currentLevel = rootMap

            // Traverse down to the second-to-last key
            for (i in 0 until parts.size - 1) {
                val part = parts[i]

                if (!currentLevel.containsKey(part) || currentLevel[part] !is MutableMap<*, *>) {
                    currentLevel[part] = mutableMapOf<String, Any>()
                }
                @Suppress("UNCHECKED_CAST")
                currentLevel = currentLevel[part] as MutableMap<String, Any>
            }

            // Handle the leaf value
            val leafKey = parts.last()

            // Overwrite logic (simple "last one wins" or "array if implicit")
            currentLevel[leafKey] = value
        }

        // 3. Convert Map to Nodes recursively
        return mapToNodes(rootMap)
    }

    private fun mapToNodes(map: Map<String, Any>): List<FormNode> {
        return map.map { (key, value) ->
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                val childrenMap = value as Map<String, Any>
                val children = mapToNodes(childrenMap)

                // Heuristic: If keys are integers (0, 1, 2...), treat as Array
                val allKeysAreInts = children.all { it.key.toIntOrNull() != null }
                FormNode.Parent(key, children, isArray = allKeysAreInts)
            } else {
                FormNode.Leaf(key, value.toString())
            }
        }
    }
}