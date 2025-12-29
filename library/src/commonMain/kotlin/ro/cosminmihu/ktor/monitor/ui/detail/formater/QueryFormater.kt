package ro.cosminmihu.ktor.monitor.ui.detail.formater

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

internal fun formatQueryString(query: String) = buildAnnotatedString {
    val params = query.split("&").map { it.split("=") }.filter { it.size == 2 }
    params.forEachIndexed { index, (key, value) ->
        withStyle(style = SpanStyle(color = Color(0xFF2E86C1))) {
            append(key)
        }
        append(": ")
        withStyle(style = SpanStyle(color = Color(0xFF8E44AD))) {
            append(value)
        }
        if (index < params.lastIndex) {
            append("\n")
        }
    }
}
