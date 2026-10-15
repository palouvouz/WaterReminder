package com.example.waterreminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SetupScreen(prefs: Prefs, canCancel: Boolean, onFinish: (Boolean) -> Unit) {
    var liters by remember { mutableStateOf(prefs.useLiters) }
    var amount by remember {
        mutableStateOf(fmtNum(if (prefs.useLiters) prefs.goalMl / 1000.0 else prefs.goalMl / GLASS_ML.toDouble()))
    }
    var start by remember { mutableStateOf(prefs.startMin) }
    var end by remember { mutableStateOf(prefs.endMin) }
    var student by remember { mutableStateOf(prefs.student) }
    var sStart by remember { mutableStateOf(prefs.schoolStart) }
    var sEnd by remember { mutableStateOf(prefs.schoolEnd) }
    var skips by remember { mutableStateOf(prefs.maxSkips.toString()) }
    var mode by remember { mutableStateOf(prefs.mode) }
    var error by remember { mutableStateOf<String?>(null) }

    fun parsedMl(useLiters: Boolean): Int? {
        val v = amount.replace(',', '.').toDoubleOrNull() ?: return null
        if (v.isNaN() || v <= 0 || v > 50) return null
        return (v * if (useLiters) 1000 else GLASS_ML).toInt()
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Ρυθμίσεις", color = Ink, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))

        GlassCard {
            SectionTitle("Ημερήσιος στόχος")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("Λίτρα", liters, Modifier.weight(1f)) {
                    if (!liters) { val ml = parsedMl(false); liters = true; if (ml != null) amount = fmtNum(ml / 1000.0) }
                }
                Chip("Ποτήρια", !liters, Modifier.weight(1f)) {
                    if (liters) { val ml = parsedMl(true); liters = false; if (ml != null) amount = fmtNum(ml / GLASS_ML.toDouble()) }
                }
            }
            Spacer(Modifier.height(12.dp))
            AppField(
                amount, { amount = it },
                if (liters) "Λίτρα ανά ημέρα" else "Ποτήρια ανά ημέρα (1 = $GLASS_ML ml)",
                KeyboardType.Decimal
            )
        }

        Spacer(Modifier.height(16.dp))
        GlassCard {
            SectionTitle("Ώρες ειδοποιήσεων")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeChip("Έναρξη", start, Modifier.weight(1f)) { start = it }
                TimeChip("Λήξη", end, Modifier.weight(1f)) { end = it }
            }
        }

        Spacer(Modifier.height(16.dp))
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Είμαι μαθητής", color = Ink, style = MaterialTheme.typography.titleMedium)
                    Text("Καμία ειδοποίηση στο σχολείο, Δευ–Παρ", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    student, { student = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Ink, checkedTrackColor = Blue,
                        uncheckedThumbColor = Muted, uncheckedTrackColor = Color.White.copy(alpha = 0.12f),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
            if (student) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeChip("Από", sStart, Modifier.weight(1f)) { sStart = it }
                    TimeChip("Έως", sEnd, Modifier.weight(1f)) { sEnd = it }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        GlassCard {
            SectionTitle("Ειδοποιήσεις")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Δόνηση", "Ήχος", "Push").forEachIndexed { i, label ->
                    Chip(label, mode == i, Modifier.weight(1f)) { mode = i }
                }
            }
            Spacer(Modifier.height(12.dp))
            AppField(skips, { skips = it.filter(Char::isDigit).take(2) }, "Skip ανά ημέρα", KeyboardType.Number)
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Danger, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(24.dp))
        GlassButton("Αποθήκευση", Modifier.fillMaxWidth(), primary = true) {
            val ml = parsedMl(liters)
            when {
                ml == null -> error = "Βάλε έναν έγκυρο στόχο (έως 50)"
                start >= end -> error = "Η ώρα έναρξης πρέπει να είναι πριν τη λήξη"
                student && sStart >= sEnd -> error = "Έλεγξε τις σχολικές ώρες"
                else -> {
                    prefs.useLiters = liters; prefs.goalMl = ml
                    prefs.startMin = start; prefs.endMin = end
                    prefs.student = student; prefs.schoolStart = sStart; prefs.schoolEnd = sEnd
                    prefs.maxSkips = skips.toIntOrNull() ?: 0
                    prefs.mode = mode
                    prefs.setupDone = true
                    onFinish(true)
                }
            }
        }
        if (canCancel) {
            Spacer(Modifier.height(12.dp))
            GlassButton("Ακύρωση", Modifier.fillMaxWidth()) { onFinish(false) }
        }
        Spacer(Modifier.height(24.dp))
        CreditsText()
        Spacer(Modifier.height(16.dp))
    }
}
