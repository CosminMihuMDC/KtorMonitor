package ro.cosminmihu.ktor.monitor.ui.detail.body

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString

@Composable
internal fun TextBody(
    text: AnnotatedString,
    modifier: Modifier = Modifier
) {
    SelectionContainer(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}