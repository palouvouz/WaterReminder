package com.example.waterreminder

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.time.LocalDate

@Composable
fun HomeScreen(prefs: Prefs, tick: Int, onSettings: () -> Unit, onLogout: () -> Unit) {
    val ctx = LocalContext.current
    prefs.ensureToday()
    var drunk by remember(tick) { mutableStateOf(prefs.drunkMl) }
    val goal = prefs.goalMl

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        Scheduler.scheduleNext(ctx)
    }
    val needsExact = Build.VERSION.SDK_INT >= 31 &&
        !ctx.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    val next = Scheduler.nextTrigger(prefs)
    val nextTime = next?.let { hm(it.hour * 60 + it.minute) } ?: "—"
    val nextDay = next?.let { if (it.toLocalDate() == LocalDate.now()) "σήμερα" else "αύριο ή αργότερα" } ?: "καμία προγραμματισμένη"
    val skipsLeft = (prefs.maxSkips - prefs.skipsUsed).coerceAtLeast(0)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Σήμερα", color = Muted, style = MaterialTheme.typography.bodyLarge)
        Text(fmtAmount(drunk, prefs.useLiters), color = Ink, style = MaterialTheme.typography.headlineLarge)
        Text("από ${fmtAmount(goal, prefs.useLiters)}", color = Muted, style = MaterialTheme.typography.bodyLarge)

        ProgressView(drunk, goal, Modifier.fillMaxWidth().height(250.dp))

        GlassCard {
            Row {
                Column(Modifier.weight(1f)) {
                    Text("Επόμενη υπενθύμιση", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    Text(nextTime, color = Ink, style = MaterialTheme.typography.headlineMedium)
                    Text(nextDay, color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Skip", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    Text("$skipsLeft", color = Ink, style = MaterialTheme.typography.headlineMedium)
                    Text("απομένουν", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        GlassButton("Ήπια ένα ποτήρι · $GLASS_ML ml", Modifier.fillMaxWidth(), primary = true) {
            prefs.drunkMl = prefs.drunkMl + GLASS_ML
            drunk = prefs.drunkMl
        }
        if (needsExact) {
            Spacer(Modifier.height(12.dp))
            GlassButton("Επίτρεψε ακριβείς ειδοποιήσεις", Modifier.fillMaxWidth()) {
                ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassButton("Ρυθμίσεις", Modifier.weight(1f), onClick = onSettings)
            GlassButton("Αποσύνδεση", Modifier.weight(1f), onClick = onLogout)
        }
        Spacer(Modifier.height(24.dp))
    }
}
