package ro.cosminmihu.ktor.monitor.ui.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ro.cosminmihu.ktor.monitor.ui.icons.Icons

internal val Icons.Filled.Markdown: ImageVector
    get() {
        if (_Markdown != null) return _Markdown!!

        _Markdown = ImageVector.Builder(
            name = "Markdown",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveToRelative(640f, -360f)
                lineToRelative(120f, -120f)
                lineToRelative(-42f, -43f)
                lineToRelative(-48f, 48f)
                verticalLineToRelative(-125f)
                horizontalLineToRelative(-60f)
                verticalLineToRelative(125f)
                lineToRelative(-48f, -48f)
                lineToRelative(-42f, 43f)
                lineToRelative(120f, 120f)
                close()
                moveTo(160f, 800f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(80f, 720f)
                verticalLineToRelative(-480f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(160f, 160f)
                horizontalLineToRelative(640f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(880f, 240f)
                verticalLineToRelative(480f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(800f, 800f)
                horizontalLineTo(160f)
                close()
                moveToRelative(0f, -80f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(-480f)
                horizontalLineTo(160f)
                verticalLineToRelative(480f)
                close()
                moveToRelative(0f, 0f)
                verticalLineToRelative(-480f)
                verticalLineToRelative(480f)
                close()
                moveToRelative(60f, -120f)
                horizontalLineToRelative(60f)
                verticalLineToRelative(-180f)
                horizontalLineToRelative(40f)
                verticalLineToRelative(120f)
                horizontalLineToRelative(60f)
                verticalLineToRelative(-120f)
                horizontalLineToRelative(40f)
                verticalLineToRelative(180f)
                horizontalLineToRelative(60f)
                verticalLineToRelative(-200f)
                quadToRelative(0f, -17f, -11.5f, -28.5f)
                reflectiveQuadTo(440f, 360f)
                horizontalLineTo(260f)
                quadToRelative(-17f, 0f, -28.5f, 11.5f)
                reflectiveQuadTo(220f, 400f)
                verticalLineToRelative(200f)
                close()
            }
        }.build()

        return _Markdown!!
    }

private var _Markdown: ImageVector? = null