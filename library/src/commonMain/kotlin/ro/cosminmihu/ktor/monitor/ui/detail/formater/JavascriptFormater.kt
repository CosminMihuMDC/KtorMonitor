package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

internal fun formatJavascript(js: ByteArray) = buildAnnotatedString {
    val keywordColor = Color.Blue
    val stringColor = Color(0xFF007700)
    val commentColor = Color.Gray


    val regex = Regex(
        """(//.*?$|/\*.*?\*/|".*?"|'.*?'|\b(function|var|let|const|if|else|for|while|return|break|continue|switch|case|default|try|catch|finally|throw|new|typeof|instanceof|in|of|this|class|extends|super|import|export|from|as|async|await|yield|void|delete|typeof|instanceof|with|debugger|do|enum|implements|interface|package|private|protected|public|static|yield)\b)""",
        RegexOption.MULTILINE
    )
    var lastIndex = 0

    val input = js.decodeToString()
    for (match in regex.findAll(input)) {
        append(input.substring(lastIndex, match.range.first))
        lastIndex = match.range.last + 1

        when {
            match.value.startsWith("//") || match.value.startsWith("/*") -> {
                withStyle(style = SpanStyle(color = commentColor)) {
                    append(match.value)
                }
            }

            match.value.startsWith("\"") || match.value.startsWith("'") -> {
                withStyle(style = SpanStyle(color = stringColor)) {
                    append(match.value)
                }
            }

            else -> {
                withStyle(style = SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold)) {
                    append(match.value)
                }
            }
        }
    }
    append(input.substring(lastIndex))
}