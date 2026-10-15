package com.example.waterreminder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MeterColors = listOf(Danger, Color(0xFFFFB020), BlueSoft, Teal)

/** Μπάρα ισχύος κωδικού και σύντομη λίστα με ό,τι λείπει. */
@Composable
fun PasswordMeter(check: PasswordCheck) {
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until 4) {
            val color = if (i < check.score) MeterColors[(check.score - 1).coerceIn(0, 3)] else Color.White.copy(alpha = 0.12f)
            Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(color))
        }
    }
    if (check.missing.isNotEmpty()) {
        Text(
            "Απαιτείται: " + check.missing.joinToString(" · "),
            color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
fun OrDivider() {
    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.12f)))
        Text("ή", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp))
        Box(Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.12f)))
    }
}

/** Απλός σύνδεσμος κειμένου με επαρκή περιοχή αφής. */
@Composable
fun LinkText(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text, color = BlueSoft, fontSize = 15.sp,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 12.dp)
    )
}
