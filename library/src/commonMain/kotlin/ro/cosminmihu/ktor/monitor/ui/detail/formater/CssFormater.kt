package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

internal fun formatCSS(css: ByteArray): AnnotatedString {
    val propertyColor = Color.Blue
    val valueColor = Color(0xFF007700)
    val symbolColor = Color.Gray

    return buildAnnotatedString {
        val regex = Regex("""([\w\-]+):\s*([^;]+);|([{}])""")
        var lastIndex = 0

        val input = css.decodeToString()
        for (match in regex.findAll(input)) {
            append(input.substring(lastIndex, match.range.first))
            lastIndex = match.range.last + 1

            match.groups[1]?.let { property ->
                withStyle(style = SpanStyle(color = propertyColor, fontWeight = FontWeight.Bold)) {
                    append(property.value)
                }
                append(": ")
            }
            match.groups[2]?.let { value ->
                withStyle(style = SpanStyle(color = valueColor)) {
                    append(value.value)
                }
                append(";")
            }
            match.groups[3]?.let { symbol ->
                withStyle(style = SpanStyle(color = symbolColor)) {
                    append(symbol.value)
                }
            }
        }
        append(input.substring(lastIndex))
    }
}
