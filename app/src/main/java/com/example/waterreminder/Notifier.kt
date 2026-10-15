package com.example.waterreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

const val ACTION_DRANK = "com.example.waterreminder.DRANK"
const val ACTION_SKIP = "com.example.waterreminder.SKIP"
private const val NOTIF_ID = 1

object Notifier {
    private fun channelId(mode: Int) = when (mode) {
        0 -> "water_vibrate"
        1 -> "water_sound"
        else -> "water_push"
    }

    fun ensureChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("water_vibrate", "Υπενθύμιση με δόνηση", NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(null, null)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel("water_sound", "Υπενθύμιση με ήχο", NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableVibration(false)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel("water_push", "Μόνο ειδοποίηση", NotificationManager.IMPORTANCE_LOW).apply {
                enableVibration(false)
                setSound(null, null)
            }
        )
    }

    private fun action(ctx: Context, code: Int, act: String) = PendingIntent.getBroadcast(
        ctx, code, Intent(ctx, ActionReceiver::class.java).setAction(act),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun show(ctx: Context) {
        val p = Prefs(ctx)
        ensureChannels(ctx)
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val b = NotificationCompat.Builder(ctx, channelId(p.mode))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Ώρα για νερό")
            .setContentText("Πιες ένα ποτήρι ($GLASS_ML ml)")
            .setContentIntent(open)
            .setAutoCancel(true)
            .addAction(0, "Ήπια", action(ctx, 1, ACTION_DRANK))
        val left = p.maxSkips - p.skipsUsed
        if (left > 0) b.addAction(0, "Skip ($left)", action(ctx, 2, ACTION_SKIP))
        ctx.getSystemService(NotificationManager::class.java).notify(NOTIF_ID, b.build())
    }

    fun dismiss(ctx: Context) {
        ctx.getSystemService(NotificationManager::class.java).cancel(NOTIF_ID)
    }
}
