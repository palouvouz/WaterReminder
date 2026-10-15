package com.example.waterreminder

/** Γέφυρα προς τον κώδικα C++ (cpp/native_bridge.cpp). */
object Native {
    init { System.loadLibrary("waternative") }

    external fun computeSlots(
        start: Int, end: Int, schoolStart: Int, schoolEnd: Int,
        useSchool: Boolean, count: Int
    ): IntArray
}
