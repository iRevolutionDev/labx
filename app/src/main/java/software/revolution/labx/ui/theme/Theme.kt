package software.revolution.labx.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppDarkColors.Primary,
    onPrimary = Color.White,
    secondary = AppDarkColors.SecondaryBackground,
    onSecondary = AppDarkColors.TextPrimary,
    tertiary = AccentColor,
    background = AppDarkColors.Background,
    onBackground = AppDarkColors.TextPrimary,
    surface = AppDarkColors.CardBackground,
    onSurface = AppDarkColors.TextPrimary,
    surfaceVariant = AppDarkColors.AccentBackground,
    onSurfaceVariant = AppDarkColors.TextSecondary,
    error = AppDarkColors.Error,
    onError = Color.White,
    outline = AppDarkColors.Border
)

private val LightColorScheme = lightColorScheme(
    primary = AppLightColors.Primary,
    onPrimary = Color.White,
    secondary = AppLightColors.SecondaryBackground,
    onSecondary = AppLightColors.TextPrimary,
    tertiary = AccentColor,
    background = AppLightColors.Background,
    onBackground = AppLightColors.TextPrimary,
    surface = AppLightColors.CardBackground,
    onSurface = AppLightColors.TextPrimary,
    surfaceVariant = AppLightColors.AccentBackground,
    onSurfaceVariant = AppLightColors.TextSecondary,
    error = AppLightColors.Error,
    onError = Color.White,
    outline = AppLightColors.Border
)

@Composable
fun LabxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}