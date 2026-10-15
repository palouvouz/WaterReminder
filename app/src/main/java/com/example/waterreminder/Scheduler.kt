package com.example.waterreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.ceil

object Scheduler {

    /** Λεπτά της ημέρας (0..1439) όλων των υπενθυμίσεων για την ημερομηνία. */
    fun slotsFor(p: Prefs, date: LocalDate): IntArray {
        val count = ceil(p.goalMl / GLASS_ML.toDouble()).toInt()
        val weekday = date.dayOfWeek.value <= 5
        return Native.computeSlots(p.startMin, p.endMin, p.schoolStart, p.schoolEnd, p.student && weekday, count)
    }

    fun nextTrigger(p: Prefs): LocalDateTime? {
        val now = LocalDateTime.now()
        val nowMin = now.hour * 60 + now.minute
        for (offset in 0..7) {
            val d = now.toLocalDate().plusDays(offset.toLong())
            val slots = slotsFor(p, d)
            val s = if (offset == 0) slots.firstOrNull { it > nowMin } else slots.firstOrNull()
            if (s != null) return d.atTime(s / 60, s % 60)
        }
        return null
    }

    private fun pending(ctx: Context): PendingIntent = PendingIntent.getBroadcast(
        ctx, 0, Intent(ctx, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun scheduleNext(ctx: Context) {
        val p = Prefs(ctx)
        if (!p.setupDone) return
        val trigger = nextTrigger(p) ?: return
        val millis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val am = ctx.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending(ctx))
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending(ctx))
        }
    }

    fun cancel(ctx: Context) {
        ctx.getSystemService(AlarmManager::class.java).cancel(pending(ctx))
    }
}
