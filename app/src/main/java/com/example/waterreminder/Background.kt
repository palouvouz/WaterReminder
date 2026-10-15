package com.example.waterreminder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

enum class Mood { Welcome, Settings, Today }

private fun DrawScope.glow(color: Color, x: Float, y: Float, radius: Float) {
    val center = Offset(size.width * x, size.height * y)
    drawCircle(Brush.radialGradient(listOf(color, Color.Transparent), center = center, radius = radius), radius = radius, center = center)
}

/** Κάθε οθόνη έχει δικό της φόντο: σκούρο βάθος με απαλές φωτεινές περιοχές. */
@Composable
fun AppBackground(mood: Mood, content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize().drawBehind {
            val baseTop = if (mood == Mood.Today) Color(0xFF0B1124) else Color(0xFF0A0A0F)
            drawRect(Brush.verticalGradient(listOf(baseTop, Color(0xFF0A0A0F))))
            val r = size.width
            when (mood) {
                Mood.Welcome -> {
                    glow(Color(0x660666EB), 0.95f, 0.05f, r * 0.95f)
                    glow(Color(0x2E00B3A4), 0.0f, 1.0f, r * 0.8f)
                }
                Mood.Settings -> {
                    glow(Color(0x4D00B3A4), 0.05f, 0.0f, r * 0.9f)
                    glow(Color(0x300666EB), 1.0f, 0.9f, r * 0.8f)
                }
                Mood.Today -> {
                    glow(Color(0x800666EB), 0.5f, 0.22f, r * 1.1f)
                }
            }
        },
        content = content
    )
}
