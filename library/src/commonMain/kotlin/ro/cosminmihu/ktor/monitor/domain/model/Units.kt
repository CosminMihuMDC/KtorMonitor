package ro.cosminmihu.ktor.monitor.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.format.optional
import kotlinx.datetime.toLocalDateTime
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun Long.formatTime() = Instant.fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .format(
        LocalDateTime.Format {
            hour(); char(':'); minute(); char(':'); second()
            optional { char('.'); secondFraction(maxLength = 1) }
        }
    )

@OptIn(ExperimentalTime::class)
internal fun Long.formatDateTimeTime() = Instant.fromEpochMilliseconds(this)
    .format(
        DateTimeComponents.Format {
            dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            char(',')
            char(' ')
            year(); char(' '); monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); day()
            char(' ')
            hour(); char(':'); minute(); char(':'); second()
            optional { char('.'); secondFraction() }
            char(' ')
        }
    )

internal fun Long.sizeAsText(): String {
    if (this < 1024) return "$this B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
    val exp = (log(this.toDouble(), 1024.0)).toInt()
    val size = this / 1024.0.pow(exp.toDouble())
    val roundedSize = (round(size * 100) / 100) // Round to 2 decimal places
    return "$roundedSize ${units[exp]}"
}

internal fun String.toColor(): Color {
    return if (this.startsWith("#")) {
        val hex = this.removePrefix("#")
        when (hex.length) {
            6 -> {
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                Color(r, g, b)
            }

            8 -> {
                val a = hex.substring(0, 2).toInt(16)
                val r = hex.substring(2, 4).toInt(16)
                val g = hex.substring(4, 6).toInt(16)
                val b = hex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }

            3 -> {
                val r = hex.substring(0, 1).toInt(16) * 17
                val g = hex.substring(1, 2).toInt(16) * 17
                val b = hex.substring(2, 3).toInt(16) * 17
                Color(r, g, b)
            }

            else -> throw IllegalArgumentException("Invalid hex color format")
        }
    } else {
        throw IllegalArgumentException("Invalid hex color format")
    }
}

internal fun String.shellEscape(): String = this
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("$", "\\$")
