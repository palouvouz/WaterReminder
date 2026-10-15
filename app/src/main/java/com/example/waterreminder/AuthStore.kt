package com.example.waterreminder

import android.content.Context
import kotlin.math.ceil
import kotlin.math.min

/**
 * Τοπική κατάσταση που χρειάζεται η σύνδεση. ΔΕΝ αποθηκεύονται tokens ή κωδικοί:
 * το Firebase SDK κρατά μόνο του τη συνεδρία στον ιδιωτικό χώρο της εφαρμογής.
 *   - email που περιμένει σύνδεσμο σύνδεσης (απαιτείται από το Firebase για επαλήθευση στην ίδια συσκευή)
 *   - μετρητές προσπαθειών για προσωρινό κλείδωμα στην πλευρά του πελάτη (βελτίωση εμπειρίας,
 *     όχι ουσιαστική προστασία: το πραγματικό rate limiting γίνεται από τους servers του Firebase)
 */
object AuthStore {
    private const val FREE_ATTEMPTS = 5
    private const val MAX_LOCK_SECONDS = 900L
    private const val EMAIL_COOLDOWN_SECONDS = 60L

    private fun sp(ctx: Context) =
        ctx.applicationContext.getSharedPreferences("auth_guard", Context.MODE_PRIVATE)

    // ---- email που περιμένει σύνδεσμο σύνδεσης ----
    fun savePendingEmail(ctx: Context, email: String) = sp(ctx).edit().putString("pendingEmail", email).apply()
    fun pendingEmail(ctx: Context): String? = sp(ctx).getString("pendingEmail", null)
    fun clearPendingEmail(ctx: Context) = sp(ctx).edit().remove("pendingEmail").apply()

    // ---- κλείδωμα μετά από αποτυχημένες προσπάθειες σύνδεσης ----
    fun lockRemainingSeconds(ctx: Context): Int =
        secondsUntil(sp(ctx).getLong("lockedUntil", 0L))

    fun registerFailure(ctx: Context) {
        val s = sp(ctx)
        val fails = s.getInt("fails", 0) + 1
        val editor = s.edit().putInt("fails", fails)
        if (fails >= FREE_ATTEMPTS) {
            val step = min(fails - FREE_ATTEMPTS, 5)
            val seconds = min(30L shl step, MAX_LOCK_SECONDS)
            editor.putLong("lockedUntil", System.currentTimeMillis() + seconds * 1000)
        }
        editor.apply()
    }

    fun lockFor(ctx: Context, seconds: Long) =
        sp(ctx).edit().putLong("lockedUntil", System.currentTimeMillis() + seconds * 1000).apply()

    fun registerSuccess(ctx: Context) =
        sp(ctx).edit().putInt("fails", 0).putLong("lockedUntil", 0L).apply()

    // ---- αναμονή ανάμεσα σε αποστολές email (σύνδεσμοι, επαναφορά κωδικού) ----
    fun emailCooldownSeconds(ctx: Context): Int =
        secondsUntil(sp(ctx).getLong("emailNextAllowed", 0L))

    fun markEmailSent(ctx: Context) =
        sp(ctx).edit().putLong("emailNextAllowed", System.currentTimeMillis() + EMAIL_COOLDOWN_SECONDS * 1000).apply()

    private fun secondsUntil(millis: Long): Int {
        val remaining = millis - System.currentTimeMillis()
        return if (remaining > 0) ceil(remaining / 1000.0).toInt() else 0
    }
}
