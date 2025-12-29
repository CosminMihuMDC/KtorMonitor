package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

internal fun formatXml(xml: ByteArray): AnnotatedString = buildAnnotatedString {
    try {
        val prettyXml = prettyXml(xml.decodeToString())

        val regex = Regex("""<[^>]+>|[^<\n]+|\n""") // Matches XML tags, content, and newlines

        regex.findAll(prettyXml).forEach { match ->
            val part = match.value
            when {
                part.startsWith("<") && part.endsWith(">") -> {
                    // XML Tags: Style them with blue color
                    withStyle(style = SpanStyle(color = Color(0xFF2E86C1))) {
                        append(part)
                    }
                }

                part == "\n" -> {
                    // Keep the newline intact
                    append(part)
                }

                else -> {
                    // Text content outside tags.
                    append(part)
                }
            }
        }
    } catch (e: Exception) {
        // Handle errors in XML parsing
        withStyle(style = SpanStyle(color = Color.Red)) {
            append("Invalid XML: ${e.message}")
        }
    }
}

private fun prettyXml(xml: String): String {
    var indentLevel = 0
    val indentString = "    " // Indentation string (4 spaces)
    val builder = StringBuilder()

    // Process the XML string as a sequence of tags and text
    val regex = """(<[^>]+>|[^<]+)""".toRegex() // Match tags or text content

    regex.findAll(xml).forEach { matchResult ->
        val match = matchResult.value.trim()

        // If it's an opening tag, self-closing tag, or a closing tag
        when {
            match.startsWith("</") -> {
                indentLevel-- // Decrease indentation level for closing tags
                builder.append("\n")
                builder.append(indentString.repeat(indentLevel))
                builder.append(match)
            }

            match.startsWith("<") && match.endsWith("/>") -> {
                builder.append("\n")
                builder.append(indentString.repeat(indentLevel))
                builder.append(match)
            }

            match.startsWith("<") -> {
                builder.append("\n")
                builder.append(indentString.repeat(indentLevel))
                builder.append(match)
                indentLevel++ // Increase indentation level for opening tags
            }

            else -> {
                builder.append(match)
            }
        }
    }

    return builder.toString().trimIndent()
}
