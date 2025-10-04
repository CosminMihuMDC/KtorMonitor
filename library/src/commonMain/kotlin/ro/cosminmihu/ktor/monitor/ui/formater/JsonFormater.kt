package ro.cosminmihu.ktor.monitor.ui.formater

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val jsonSerializer = Json { prettyPrint = true }

internal fun formatJson(json: ByteArray): AnnotatedString = buildAnnotatedString {
    try {
        // Parse and pretty-print JSON
        val jsonElement = Json.parseToJsonElement(json.decodeToString())
        val prettyJson = jsonSerializer.encodeToString(JsonElement.serializer(), jsonElement)

        // Regex to match JSON parts: strings, numbers (including decimals), punctuation
        val regex = Regex("""("(\\.|[^"\\])*"|-?\d+(\.\d+)?([eE][+-]?\d+)?|true|false|null|[{}\[\]:,]|\n|\s+)""")

        var isKey = true // Track if the current string is a key

        regex.findAll(prettyJson).forEach { match ->
            val part = match.value
            when {
                part.startsWith("\"") -> { // Keys or string values
                    if (isKey) {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF2E86C1), // Blue for keys
                            )
                        ) {
                            append(part)
                        }
                        isKey = false // Switch to value mode after key
                    } else {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF8E44AD), // Purple for string values
                            )
                        ) {
                            append(part)
                        }
                    }
                }

                part == ":" -> { // Colon separates keys and values
                    withStyle(
                        style = SpanStyle(
                            color = Color.Gray, // Gray for punctuation

                        )
                    ) {
                        append(part)
                    }
                    isKey = false // Next string will be a value
                }

                part.matches(Regex("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?")) -> { // Numbers (int, double, scientific)
                    withStyle(style = SpanStyle(color = Color(0xFF28BE69))) {
                        append(part)
                    }
                    isKey = true // Reset to key mode for the next string
                }

                part in listOf("true", "false", "null") -> { // Boolean and null values
                    withStyle(style = SpanStyle(color = Color(0xFFE67E22))) { // Orange for boolean/null
                        append(part)
                    }
                    isKey = true // Reset to key mode for the next string
                }

                part in "{}[]," -> { // Punctuation
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append(part)
                    }
                    isKey = true // Punctuation resets key tracking
                }

                part == "\n" || part == " " -> { // Preserve newlines and spaces
                    append(part)
                }

                else -> { // Fallback for unexpected cases
                    append(part)
                }
            }
        }
    } catch (_: Exception) {
        append("")
    }
}