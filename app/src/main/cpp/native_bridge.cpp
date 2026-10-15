#include <jni.h>

#include "schedule.h"

extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_waterreminder_Native_computeSlots(
        JNIEnv *env, jobject /*thiz*/,
        jint start, jint end, jint schoolStart, jint schoolEnd,
        jboolean useSchool, jint count) {
    const std::vector<int> slots = water::computeSlots(
            start, end, schoolStart, schoolEnd, useSchool == JNI_TRUE, count);

    jintArray result = env->NewIntArray(static_cast<jsize>(slots.size()));
    if (result == nullptr) return nullptr;  // OutOfMemoryError έχει ήδη ρίξει η JVM
    if (!slots.empty()) {
        env->SetIntArrayRegion(result, 0, static_cast<jsize>(slots.size()), slots.data());
    }
    return result;
}
