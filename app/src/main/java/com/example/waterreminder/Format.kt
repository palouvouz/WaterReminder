package com.example.waterreminder

import java.util.Locale

fun hm(min: Int): String = String.format(Locale.US, "%02d:%02d", min / 60, min % 60)

fun fmtNum(d: Double): String =
    if (d == d.toInt().toDouble()) d.toInt().toString() else String.format(Locale.US, "%.1f", d)

fun fmtAmount(ml: Int, liters: Boolean): String =
    if (liters) "${fmtNum(ml / 1000.0)} L" else "${fmtNum(ml / GLASS_ML.toDouble())} ποτήρια"
