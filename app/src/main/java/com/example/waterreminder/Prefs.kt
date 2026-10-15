package com.example.waterreminder

import android.content.Context
import java.time.LocalDate

const val GLASS_ML = 250

class Prefs(ctx: Context) {
    private val sp = ctx.applicationContext.getSharedPreferences("water", Context.MODE_PRIVATE)
    private fun getI(k: String, d: Int) = sp.getInt(k, d)
    private fun putI(k: String, v: Int) = sp.edit().putInt(k, v).apply()
    private fun getB(k: String, d: Boolean) = sp.getBoolean(k, d)
    private fun putB(k: String, v: Boolean) = sp.edit().putBoolean(k, v).apply()

    var setupDone: Boolean get() = getB("setupDone", false); set(v) = putB("setupDone", v)

    /**
     * Τοπική ένδειξη ότι υπάρχει συνδεδεμένος χρήστης. Την ορίζει ο AuthStateListener (MainActivity.kt).
     * Οι αποδέκτες ειδοποιήσεων τη διαβάζουν αντί να ρωτούν το Firebase, γιατί σε ψυχρή εκκίνηση
     * του process το FirebaseAuth.currentUser μπορεί να είναι προσωρινά null πριν φορτώσει η συνεδρία.
     */
    var signedIn: Boolean get() = getB("signedIn", false); set(v) = putB("signedIn", v)
    var useLiters: Boolean get() = getB("useLiters", true); set(v) = putB("useLiters", v)
    var student: Boolean get() = getB("student", false); set(v) = putB("student", v)

    var goalMl: Int get() = getI("goalMl", 2000); set(v) = putI("goalMl", v)
    var startMin: Int get() = getI("startMin", 480); set(v) = putI("startMin", v)
    var endMin: Int get() = getI("endMin", 1320); set(v) = putI("endMin", v)
    var schoolStart: Int get() = getI("schoolStart", 8 * 60 + 15); set(v) = putI("schoolStart", v)
    var schoolEnd: Int get() = getI("schoolEnd", 14 * 60 + 14); set(v) = putI("schoolEnd", v)
    var maxSkips: Int get() = getI("maxSkips", 4); set(v) = putI("maxSkips", v)

    /** 0 = δόνηση, 1 = ήχος, 2 = μόνο push */
    var mode: Int get() = getI("mode", 0); set(v) = putI("mode", v)

    var drunkMl: Int get() = getI("drunkMl", 0); set(v) = putI("drunkMl", v)
    var skipsUsed: Int get() = getI("skipsUsed", 0); set(v) = putI("skipsUsed", v)
    private var dayKey: String
        get() = sp.getString("dayKey", "") ?: ""
        set(v) = sp.edit().putString("dayKey", v).apply()

    private var ownerUid: String
        get() = sp.getString("ownerUid", "") ?: ""
        set(v) = sp.edit().putString("ownerUid", v).apply()

    /** Μηδενίζει τα ημερήσια στοιχεία όταν αλλάξει η ημερομηνία. */
    fun ensureToday() {
        val k = LocalDate.now().toString()
        if (dayKey != k) {
            dayKey = k
            drunkMl = 0
            skipsUsed = 0
        }
    }

    /**
     * Δένει τις τοπικές ρυθμίσεις με τον λογαριασμό. Αν συνδεθεί άλλος χρήστης στην ίδια
     * συσκευή, οι ρυθμίσεις του προηγούμενου σβήνονται ώστε να μη διαρρεύσουν.
     */
    fun adoptUser(uid: String) {
        val current = ownerUid
        if (current.isEmpty()) {
            ownerUid = uid
        } else if (current != uid) {
            sp.edit().clear().putString("ownerUid", uid).apply()
        }
    }
}
