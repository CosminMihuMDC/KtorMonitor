package ro.cosminmihu.ktor.monitor.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Palette for light theme
internal val GrayBlack = Color(0xFF111111)
internal val GrayDark = Color(0xFF2A2A2A)
internal val GrayMedium = Color(0xFF4A4A4A)
internal val GrayLight = Color(0xFFE6E6E6)
internal val GrayLighter = Color(0xFFF5F5F5)
internal val GrayOutlineLight = Color(0xFF9E9E9E)
internal val GrayOutlineVariant = GrayLight
internal val GrayInverseSurface = GrayDark
internal val GrayInverseOnSurface = GrayLight
internal val GraySurfaceBright = Color.White
internal val GraySurfaceDim = Color(0xFFDDDDDD)
internal val GraySurfaceContainerLowest = Color.White
internal val GraySurfaceContainerLow = GrayLighter
internal val GraySurfaceContainer = GrayLight
internal val GraySurfaceContainerHigh = Color(0xFFDCDCDC)
internal val GraySurfaceContainerHighest = Color(0xFFD0D0D0)
internal val BlackScrim = Color(0x80000000)

// Palette for dark theme
internal val DarkBackground = Color(0xFF000000)
internal val DarkSurface = Color(0xFF121212)
internal val DarkSurfaceVariant = Color(0xFF1F1F1F)
internal val DarkOutline = Color(0xFF6E6E6E)
internal val DarkOutlineVariant = DarkSurfaceVariant
internal val DarkText = Color(0xFFE6E6E6)
internal val DarkAccent = Color(0xFFCCCCCC)
internal val DarkInverseSurface = DarkText
internal val DarkInverseOnSurface = DarkSurface
internal val DarkSurfaceBright = Color(0xFF2C2C2C)
internal val DarkSurfaceDim = Color(0xFF0D0D0D)
internal val DarkSurfaceContainerLowest = DarkBackground
internal val DarkSurfaceContainerLow = Color(0xFF0A0A0A)
internal val DarkSurfaceContainer = Color(0xFF101010)
internal val DarkSurfaceContainerHigh = Color(0xFF161616)
internal val DarkSurfaceContainerHighest = DarkSurfaceVariant
internal val DarkScrim = Color(0x80000000)

internal val lightColorScheme = lightColorScheme(
    primary = GrayDark,
    onPrimary = Color.White,
    primaryContainer = GrayLight,
    onPrimaryContainer = GrayBlack,
    inversePrimary = GrayMedium,
    secondary = GrayMedium,
    onSecondary = Color.White,
    secondaryContainer = GrayLight,
    onSecondaryContainer = GrayBlack,
    tertiary = GrayMedium,
    onTertiary = Color.White,
    tertiaryContainer = GrayLight,
    onTertiaryContainer = GrayBlack,
    background = GrayLighter,
    onBackground = GrayBlack,
    surface = Color.White,
    onSurface = GrayBlack,
    surfaceVariant = GrayLight,
    onSurfaceVariant = GrayDark,
    surfaceTint = GrayDark,
    inverseSurface = GrayInverseSurface,
    inverseOnSurface = GrayInverseOnSurface,
    outline = GrayOutlineLight,
    outlineVariant = GrayOutlineVariant,
    scrim = BlackScrim,
    surfaceBright = GraySurfaceBright,
    surfaceContainer = GraySurfaceContainer,
    surfaceContainerHigh = GraySurfaceContainerHigh,
    surfaceContainerHighest = GraySurfaceContainerHighest,
    surfaceContainerLow = GraySurfaceContainerLow,
    surfaceContainerLowest = GraySurfaceContainerLowest,
    surfaceDim = GraySurfaceDim,
    primaryFixed = GrayDark,
    primaryFixedDim = GrayMedium,
    onPrimaryFixed = Color.White,
    onPrimaryFixedVariant = GrayLight,
    secondaryFixed = GrayMedium,
    secondaryFixedDim = GrayDark,
    onSecondaryFixed = Color.White,
    onSecondaryFixedVariant = GrayLight,
    tertiaryFixed = GrayMedium,
    tertiaryFixedDim = GrayDark,
    onTertiaryFixed = Color.White,
    onTertiaryFixedVariant = GrayLight,
)

internal val darkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkText,
    inversePrimary = DarkText,
    secondary = DarkAccent,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkText,
    tertiary = DarkAccent,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = DarkText,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkAccent,
    surfaceTint = DarkAccent,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,
    surfaceBright = DarkSurfaceBright,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceDim = DarkSurfaceDim,
    primaryFixed = DarkAccent,
    primaryFixedDim = DarkSurfaceVariant,
    onPrimaryFixed = DarkBackground,
    onPrimaryFixedVariant = DarkText,
    secondaryFixed = DarkAccent,
    secondaryFixedDim = DarkSurfaceVariant,
    onSecondaryFixed = DarkBackground,
    onSecondaryFixedVariant = DarkText,
    tertiaryFixed = DarkAccent,
    tertiaryFixedDim = DarkSurfaceVariant,
    onTertiaryFixed = DarkBackground,
    onTertiaryFixedVariant = DarkText,
)