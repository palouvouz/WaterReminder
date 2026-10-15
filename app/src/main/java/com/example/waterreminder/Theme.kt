package com.example.waterreminder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Bg = Color(0xFF0A0A0F)
val Ink = Color(0xFFFFFFFF)
val Muted = Color(0xFF9BA0AE)
val Blue = Color(0xFF0666EB)
val BlueSoft = Color(0xFF4C8DF6)
val Teal = Color(0xFF00B3A4)
val Danger = Color(0xFFFF6B6B)

private val Sans = FontFamily.SansSerif

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Blue, onPrimary = Ink,
            background = Bg, onBackground = Ink,
            surface = Bg, onSurface = Ink,
            error = Danger
        ),
        typography = Typography(
            headlineLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 36.sp, letterSpacing = (-0.8).sp),
            headlineMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 30.sp, letterSpacing = (-0.6).sp),
            titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
            bodyLarge = TextStyle(fontFamily = Sans, fontSize = 16.sp),
            bodyMedium = TextStyle(fontFamily = Sans, fontSize = 14.sp),
            labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        ),
        content = content
    )
}

/**
 * Επιφάνεια τύπου glass: ημιδιαφανής διαβάθμιση και λεπτό περίγραμμα που σβήνει προς τα κάτω.
 * Το Compose δεν θολώνει το φόντο πίσω από ένα στοιχείο, γι' αυτό το blur προσομοιώνεται
 * με διαφάνεια και περίγραμμα.
 */
fun Modifier.glass(shape: Shape, top: Color, bottom: Color): Modifier = this
    .clip(shape)
    .background(Brush.verticalGradient(listOf(top, bottom)))
    .border(
        1.dp,
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))),
        shape
    )
