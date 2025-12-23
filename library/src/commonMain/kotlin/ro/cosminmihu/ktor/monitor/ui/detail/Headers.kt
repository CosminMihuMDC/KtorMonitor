package ro.cosminmihu.ktor.monitor.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import ro.cosminmihu.ktor.monitor.ui.Dimens

@Composable
internal fun Headers(headers: Map<String, List<String>>) {
    Column {
        headers.forEach {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(it.key)
                    }
                    append(": ")
                    append(it.value.joinToString(", "))
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}