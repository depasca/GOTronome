#include <jni.h>
#include "MetronomeEngine.h"

static MetronomeEngine engine;

void startMetronome(JNIEnv* env, jobject thiz, int beatsPerMinute, int beatsPerMeasure) {
    engine.start(beatsPerMinute, beatsPerMeasure);
}
void stopMetronome(JNIEnv* env, jobject thiz) {
    engine.stop();
}
double getCurrentTimeSeconds(JNIEnv* env, jobject thiz) {
    return engine.getCurrentTimeSeconds();
}
int getCurrentBeat(JNIEnv* env, jobject thiz) {
    return engine.getCurrentBeat();
}

bool getIsPLaying(JNIEnv* env, jobject thiz) {
    return engine.getIisPlaying();
}

extern "C" JNICALL
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    if (vm->AttachCurrentThread(&env, NULL) != JNI_OK) {
        std::cerr << "Failed to attach thread to JVM" << std::endl;
        return JNI_ERR;
    }
    // Find your class. JNI_OnLoad is called from the correct class loader context for this to work.
    jclass c = env->FindClass("com/pdp/gotronome/Metronome");
    if (c == nullptr) return JNI_ERR;
    jobject dummyObj = env->NewObject(c,
                                      env->GetMethodID(c, "<init>", "()V"));
    engine.setJavaVM(vm, dummyObj);

    // Register your class' native methods.
    static const JNINativeMethod methods[] = {
            {"startMetronome", "(II)V", reinterpret_cast<void*>(startMetronome)},
            {"stopMetronome", "()V", reinterpret_cast<void*>(stopMetronome)},
            {"getCurrentTimeSeconds", "()D", reinterpret_cast<void*>(getCurrentTimeSeconds)},
            {"getCurrentBeat", "()I", reinterpret_cast<void*>(getCurrentBeat)},
            {"getIsPLaying", "()Z", reinterpret_cast<void*>(getIsPLaying)},
    };
    int rc = env->RegisterNatives(c, methods, sizeof(methods)/sizeof(JNINativeMethod));
    if (rc != JNI_OK) return rc;

//    jmethodID m = env->GetStaticMethodID(c, "onNativeBeat", "(I)V");
//    engine.initialize(vm, c, m);

    return JNI_VERSION_1_6;
}