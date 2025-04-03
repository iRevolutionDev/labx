package software.revolution.labx.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val PrimaryLight = Color(0xFF4285F4)
val PrimaryDark = Color(0xFF3367D6)

val SecondaryLight = Color(0xFF03DAC5)
val SecondaryDark = Color(0xFF00B3A6)

val BackgroundLight = Color(0xFFFAFAFA)
val BackgroundDark = Color(0xFF121212)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)

val EditorBackgroundLight = Color(0xFFFFFFFF)
val EditorBackgroundDark = Color(0xFF1E1E1E)

val AccentColor = Color(0xFFFFA000)

object AppLightColors {
    val Background = Color(0xFFF7FAFC)
    val CardBackground = Color(0xFFFFFFFF)
    val CardBorder = Color(0xFFE1E7EC)
    val SecondaryBackground = Color(0xFFEDF2F7)
    val AccentBackground = Color(0xFFE6EDF5)
    val Primary = Color(0xFF7C3AED)
    val PrimaryHover = Color(0xFF6D28D9)
    val TextPrimary = Color(0xFF334155)
    val TextSecondary = Color(0xFF64748B)
    val TextMuted = Color(0xFF94A3B8)
    val Border = Color(0xFFE1E7EC)
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFFCD34D)
    val Error = Color(0xFFF43F5E)
    val Info = Color(0xFFBAE6FD)
    val CodeBackground = Color(0xFFF1F5F9)
    val CodeText = Color(0xFF334155)

    val BackgroundGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF7FAFC),
            Color(0xFFF1F5F9)
        ),
        start = Offset(0f, 0f),
        end = Offset(
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY
        )
    )
}

object AppDarkColors {
    val Background = Color(0xFF1A202C)
    val CardBackground = Color(0xFF1a1b26)
    val CardBorder = Color(0xFF2D3748)
    val SecondaryBackground = Color(0xFF2D3748)
    val AccentBackground = Color(0xFF333C4D)
    val Primary = Color(0xFF7C3AED)
    val PrimaryHover = Color(0xFF6D28D9)
    val TextPrimary = Color(0xFFF7FAFC)
    val TextSecondary = Color(0xFFCBD5E1)
    val TextMuted = Color(0xFF94A3B8)
    val Border = Color(0xFF2D3748)
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFFCD34D)
    val Error = Color(0xFFF43F5E)
    val Info = Color(0xFFBAE6FD)
    val CodeBackground = Color(0xFF111827)
    val CodeText = Color(0xFFF7FAFC)

    val BackgroundGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1A1B26),
            Color(0xFF24283B)
        ),
        start = Offset(0f, 0f),
        end = Offset(
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY
        )
    )
}