package com.example.waterreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Ξυπνά την ώρα της υπενθύμισης. Αν ο χρήστης έχει αποσυνδεθεί, δεν στέλνει τίποτα. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent?) {
        val p = Prefs(ctx)
        if (!p.signedIn) return
        p.ensureToday()
        if (p.drunkMl < p.goalMl) Notifier.show(ctx)
        Scheduler.scheduleNext(ctx)
    }
}

/** Χειρίζεται τα κουμπιά «Ήπια» και «Skip» της ειδοποίησης. */
class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent?) {
        val p = Prefs(ctx)
        p.ensureToday()
        when (intent?.action) {
            ACTION_DRANK -> p.drunkMl = p.drunkMl + GLASS_ML
            ACTION_SKIP -> if (p.skipsUsed < p.maxSkips) p.skipsUsed = p.skipsUsed + 1
        }
        Notifier.dismiss(ctx)
    }
}

/** Ξαναβάζει τον alarm μετά από επανεκκίνηση, ενημέρωση εφαρμογής ή αλλαγή ώρας/ζώνης ώρας. */
class SystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent?) {
        if (!Prefs(ctx).signedIn) return
        Scheduler.scheduleNext(ctx)
    }
}
