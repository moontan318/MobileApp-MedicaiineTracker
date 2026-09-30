package com.medtracker.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.medtracker.core.knowledge.Severity

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E6B5C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA8E6CF),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4A635C),
    tertiary = Color(0xFF416277),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8CD5BC),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005144),
    onPrimaryContainer = Color(0xFFA8F2D8),
    secondary = Color(0xFFB1CCC3),
    tertiary = Color(0xFFA8CBE3),
)

@Composable
fun MedTrackerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@Immutable
data class SeverityColors(val container: Color, val content: Color)

/** Fixed traffic-light colours so severity reads the same regardless of dynamic colour. */
@Composable
fun severityColors(severity: Severity): SeverityColors {
    val dark = isSystemInDarkTheme()
    return when (severity) {
        Severity.MAJOR -> if (dark) SeverityColors(Color(0xFF93000A), Color(0xFFFFDAD6))
        else SeverityColors(Color(0xFFFFDAD6), Color(0xFF93000A))
        Severity.MODERATE -> if (dark) SeverityColors(Color(0xFF7A4B00), Color(0xFFFFDDB3))
        else SeverityColors(Color(0xFFFFE0B2), Color(0xFF6B3F00))
        Severity.MINOR -> if (dark) SeverityColors(Color(0xFF5E5A00), Color(0xFFF3EE8A))
        else SeverityColors(Color(0xFFFFF7C2), Color(0xFF4F4A00))
        Severity.INFO -> if (dark) SeverityColors(Color(0xFF004A77), Color(0xFFCFE5FF))
        else SeverityColors(Color(0xFFD6ECFF), Color(0xFF00497A))
    }
}
