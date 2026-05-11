package com.prathamchikitse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary            = PrimaryRed,
    onPrimary          = OnPrimary,
    primaryContainer   = PrimaryRedDark,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary          = GeneralTeal,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary           = InfoBlue,
    background         = BackgroundDark,
    onBackground       = TextPrimaryDark,
    surface            = SurfaceDark,
    onSurface          = TextPrimaryDark,
    surfaceVariant     = SurfaceVariantDark,
    onSurfaceVariant   = TextSecondaryDark,
    error              = CriticalRed,
    onError            = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary            = PrimaryRedDark,
    onPrimary          = OnPrimary,
    primaryContainer   = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary          = GeneralTeal,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary           = InfoBlue,
    background         = BackgroundLight,
    onBackground       = TextPrimaryLight,
    surface            = SurfaceLight,
    onSurface          = TextPrimaryLight,
    surfaceVariant     = SurfaceVariantLight,
    onSurfaceVariant   = TextSecondaryLight,
    error              = CriticalRed,
    onError            = Color.White
)

// Custom extension for emergency-specific colors
data class EmergencyColors(
    val criticalColor: Color,
    val moderateColor: Color,
    val generalColor: Color,
    val dosColor: Color,
    val dontsColor: Color,
    val sosColor: Color
)

val LocalEmergencyColors = staticCompositionLocalOf {
    EmergencyColors(
        criticalColor = CriticalRed,
        moderateColor = ModerateOrange,
        generalColor  = GeneralTeal,
        dosColor      = DosGreen,
        dontsColor    = DontsRed,
        sosColor      = SOSRed
    )
}

val MaterialTheme.emergencyColors: EmergencyColors
    @Composable get() = LocalEmergencyColors.current

@Composable
fun PrathamChikitseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalEmergencyColors provides EmergencyColors(
            criticalColor = if (darkTheme) CriticalRedLight else CriticalRed,
            moderateColor = if (darkTheme) ModerateOrangeLight else ModerateOrange,
            generalColor  = if (darkTheme) GeneralTealLight else GeneralTeal,
            dosColor      = if (darkTheme) DosGreenLight else DosGreen,
            dontsColor    = if (darkTheme) DontsRedLight else DontsRed,
            sosColor      = SOSRed
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AppTypography,
            content     = content
        )
    }
}
