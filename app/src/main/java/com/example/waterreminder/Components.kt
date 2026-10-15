package com.example.waterreminder

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlassButton(
    text: String, modifier: Modifier = Modifier, primary: Boolean = false,
    enabled: Boolean = true, onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    val top = if (primary) Blue.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.14f)
    val bottom = if (primary) Blue.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.04f)
    Box(
        modifier
            .height(54.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .glass(shape, top, bottom)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Ink, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun Chip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val top = if (selected) Blue.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.10f)
    val bottom = if (selected) Blue.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.03f)
    Box(
        modifier.height(44.dp).glass(shape, top, bottom).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Ink, fontSize = 15.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth()
            .glass(RoundedCornerShape(24.dp), Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.03f))
            .padding(20.dp),
        content = content
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(text, color = Ink, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
fun AppField(
    value: String, onChange: (String) -> Unit, label: String,
    keyboard: KeyboardType = KeyboardType.Text, password: Boolean = false
) {
    val fill = Color.White.copy(alpha = 0.06f)
    OutlinedTextField(
        value = value, onValueChange = onChange, singleLine = true,
        label = { Text(label) },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = fill, unfocusedContainerColor = fill,
            focusedBorderColor = BlueSoft, unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
            focusedTextColor = Ink, unfocusedTextColor = Ink,
            focusedLabelColor = Muted, unfocusedLabelColor = Muted,
            cursorColor = BlueSoft
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TimeChip(label: String, minutes: Int, modifier: Modifier = Modifier, onChange: (Int) -> Unit) {
    val ctx = LocalContext.current
    Chip("$label  ${hm(minutes)}", false, modifier) {
        TimePickerDialog(
            ctx, android.R.style.Theme_DeviceDefault_Dialog_Alert,
            { _, h, m -> onChange(h * 60 + m) }, minutes / 60, minutes % 60, true
        ).show()
    }
}

/** Αναφορά δημιουργίας της εφαρμογής. */
@Composable
fun CreditsText() {
    Text(
        "Η ιδέα είναι του δημιουργού της εφαρμογής. Vibe coded με την Anthropic (Claude), debug με την OpenAI.",
        color = Muted, fontSize = 12.sp
    )
}
