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

int getPlayingState(JNIEnv* env, jobject thiz) {
    return engine.getPlayingState();
}

void setSilentMeasuresEnabled(JNIEnv* env, jobject thiz, bool val) {
    engine.setSilentMeasuresEnabled(val);
}

void setNumSilentMeasures(JNIEnv* env, jobject thiz, int numSilentMeasures) {
    engine.setNumSilentMeasures(numSilentMeasures);
}

void setCountInEnabled(JNIEnv* env, jobject thiz, bool val) {
    engine.setCountInEnabled(val);
}

void setAccentPattern(JNIEnv* env, jobject thiz, jintArray pattern) {
    jsize len = env->GetArrayLength(pattern);
    jint* elems = env->GetIntArrayElements(pattern, nullptr);
    engine.setAccentPattern(elems, static_cast<int>(len));
    env->ReleaseIntArrayElements(pattern, elems, JNI_ABORT);
}

void setGroove(JNIEnv* env, jobject thiz, int stepsPerBeat, jintArray stepVoices) {
    jsize len = env->GetArrayLength(stepVoices);
    jint* elems = env->GetIntArrayElements(stepVoices, nullptr);
    engine.setGroove(stepsPerBeat, elems, static_cast<int>(len));
    env->ReleaseIntArrayElements(stepVoices, elems, JNI_ABORT);
}

void loadSample(JNIEnv* env, jobject thiz, int voiceIndex, jfloatArray frames, int rate, int baseMidiNote) {
    jsize len = env->GetArrayLength(frames);
    jfloat* elems = env->GetFloatArrayElements(frames, nullptr);
    engine.loadSample(voiceIndex, elems, static_cast<int>(len), rate, baseMidiNote);
    env->ReleaseFloatArrayElements(frames, elems, JNI_ABORT);
}

void setBassLine(JNIEnv* env, jobject thiz, int stepsPerBeat, int bars, jintArray notes) {
    jsize len = env->GetArrayLength(notes);
    jint* elems = env->GetIntArrayElements(notes, nullptr);
    engine.setBassLine(stepsPerBeat, bars, elems, static_cast<int>(len));
    env->ReleaseIntArrayElements(notes, elems, JNI_ABORT);
}

void setBassRoot(JNIEnv* env, jobject thiz, int midiNote) {
    engine.setBassRoot(midiNote);
}

void setBassEnabled(JNIEnv* env, jobject thiz, bool enabled) {
    engine.setBassEnabled(enabled);
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
            {"getPlayingState", "()I", reinterpret_cast<void*>(getPlayingState)},
            {"setNumSilentMeasures", "(I)V", reinterpret_cast<void*>(setNumSilentMeasures)},
            {"setSilentMeasuresEnabled", "(Z)V", reinterpret_cast<void*>(setSilentMeasuresEnabled)},
            {"setCountInEnabled", "(Z)V", reinterpret_cast<void*>(setCountInEnabled)},
            {"setAccentPattern", "([I)V", reinterpret_cast<void*>(setAccentPattern)},
            {"setGroove", "(I[I)V", reinterpret_cast<void*>(setGroove)},
            {"loadSample", "(I[FII)V", reinterpret_cast<void*>(loadSample)},
            {"setBassLine", "(II[I)V", reinterpret_cast<void*>(setBassLine)},
            {"setBassRoot", "(I)V", reinterpret_cast<void*>(setBassRoot)},
            {"setBassEnabled", "(Z)V", reinterpret_cast<void*>(setBassEnabled)},
    };
    int rc = env->RegisterNatives(c, methods, sizeof(methods)/sizeof(JNINativeMethod));
    if (rc != JNI_OK) return rc;

//    jmethodID m = env->GetStaticMethodID(c, "onNativeBeat", "(I)V");
//    engine.initialize(vm, c, m);

    return JNI_VERSION_1_6;
}