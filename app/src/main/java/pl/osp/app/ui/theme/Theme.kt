package pl.osp.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val OspRed = Color(0xFFC8102E)
val OspRedDark = Color(0xFF8B0A1F)
val OspYellow = Color(0xFFFFC72C)
val OspGray = Color(0xFF2A2A2A)

private val Light = lightColorScheme(
    primary = OspRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD8),
    onPrimaryContainer = Color(0xFF410008),
    secondary = OspYellow,
    onSecondary = OspGray,
    tertiary = Color(0xFF4A6363),
    background = Color(0xFFFFFBFA),
    surface = Color(0xFFFFFBFA),
    error = Color(0xFFBA1A1A)
)

private val Dark = darkColorScheme(
    primary = Color(0xFFFFB3AE),
    onPrimary = Color(0xFF690012),
    primaryContainer = OspRedDark,
    onPrimaryContainer = Color(0xFFFFDAD8),
    secondary = OspYellow,
    onSecondary = OspGray,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

private val AppTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp)
)

@Composable
fun OspTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        typography = AppTypography,
        content = content
    )
}
