package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.viewmodel.FlowThemeMode
import java.util.Calendar

// MIDNIGHT: Amoled Black & Amber
private val MidnightColorScheme = darkColorScheme(
    primary = ZenAmber,
    secondary = ZenMossGreen,
    tertiary = ZenAmberDim,
    background = ZenBlack,
    surface = ZenStoneDark,
    onPrimary = ZenBlack,
    onSecondary = ZenBlack,
    onTertiary = ZenBlack,
    onBackground = ZenTextPrimary,
    onSurface = ZenTextPrimary,
    surfaceVariant = ZenStoneGrey,
    onSurfaceVariant = ZenTextSecondary,
    outline = ZenStoneLight
)

// DUSK: Calm Gray & Slate Gold
private val DuskColorScheme = darkColorScheme(
    primary = DuskPrimary,
    secondary = DuskSecondary,
    tertiary = ZenAmberDim,
    background = DuskBackground,
    surface = DuskSurface,
    onPrimary = DuskBackground,
    onSecondary = DuskBackground,
    onTertiary = DuskBackground,
    onBackground = DuskTextPrimary,
    onSurface = DuskTextPrimary,
    surfaceVariant = DuskSurfaceVariant,
    onSurfaceVariant = DuskTextSecondary,
    outline = DuskOutline
)

// DAWN: Warm Sand & Clay
private val DawnColorScheme = lightColorScheme(
    primary = DawnPrimary,
    secondary = DawnSecondary,
    tertiary = ZenAmberDim,
    background = DawnBackground,
    surface = DawnSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DawnTextPrimary,
    onSurface = DawnTextPrimary,
    surfaceVariant = DawnSurfaceVariant,
    onSurfaceVariant = DawnTextSecondary,
    outline = DawnOutline
)

@Composable
fun MyApplicationTheme(
    themeMode: FlowThemeMode = FlowThemeMode.CIRCADIAN,
    content: @Composable () -> Unit
) {
    val effectiveMode = remember(themeMode) {
        if (themeMode == FlowThemeMode.CIRCADIAN) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 6..16 -> FlowThemeMode.DAWN       // 6 AM to 4:59 PM is Dawn (Light)
                in 17..20 -> FlowThemeMode.DUSK     // 5 PM to 8:59 PM is Dusk (Slate)
                else -> FlowThemeMode.MIDNIGHT      // 9 PM to 5:59 AM is Midnight (Pure Black)
            }
        } else {
            themeMode
        }
    }

    val colorScheme = when (effectiveMode) {
        FlowThemeMode.DAWN -> DawnColorScheme
        FlowThemeMode.DUSK -> DuskColorScheme
        FlowThemeMode.MIDNIGHT -> MidnightColorScheme
        else -> DawnColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
