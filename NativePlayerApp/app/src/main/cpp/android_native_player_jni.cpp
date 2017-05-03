//
// Created by MrLukashem on 25.03.2017.
//

#include <jni.h>
#include <string>
#include <android/log.h>
#include <memory>

#include "include/NativePlayer.h"
#include "include/NativeLatencyMeasurement.h"

#define APPNAME "android_native_player_jni"
#define ALOGV(x) __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, x);

#ifdef __cplusplus
extern "C" {
#endif

namespace {
    NativeLatencyMeasurement* latencyMeasurement;
    void destroyLatencyMeasurementClass() {
        if (latencyMeasurement != nullptr) {
            delete latencyMeasurement;
        }
    }

    JavaVM* javaVM = nullptr;
    jclass latencyMeasurementJavaClass;
    jobject lmInstance;
    JNIEnv* jnienv = nullptr;

    std::unique_ptr<NativePlayer> player = nullptr;
}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_latencyutils_LatencyMeasurement_release(JNIEnv *env,
                                                                                jobject instance) {

    destroyLatencyMeasurementClass();

}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_setSourceBufferJNI(
        JNIEnv *env, jobject instance, jshortArray buffer_, jint bufferSize, jint bitsPerSample,
        jint channelCount, jint sampleRate, jint preferredBufferSize) {
    jshort *buffer = env->GetShortArrayElements(buffer_, NULL);

    // TODO
    if (player) {
        player->setBufferSource(buffer, bufferSize, bitsPerSample, channelCount, sampleRate, preferredBufferSize);
    }

    env->ReleaseShortArrayElements(buffer_, buffer, 0);
}


JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_createPlayerRequest(
        JNIEnv *env, jobject instance) {

    player = std::make_unique<NativePlayer>();
}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_latencyutils_LatencyMeasurement_startLatencyTest(
        JNIEnv *env, jobject instance, jint bitDepth, jint sampleRate) {

    ALOGV("startJNI");
    destroyLatencyMeasurementClass();
    env->GetJavaVM(&javaVM);
    auto cls = env->GetObjectClass(instance);
    latencyMeasurementJavaClass = static_cast<jclass >(env->NewGlobalRef(cls));
    lmInstance = env->NewGlobalRef(instance);

    latencyMeasurement = new NativeLatencyMeasurement([] (int latency) { // nativeResultCallback
        javaVM->AttachCurrentThread(&jnienv, nullptr);
        auto methodID = jnienv->GetMethodID(latencyMeasurementJavaClass, "nativeResultCallback", "(I)V");
        jnienv->CallVoidMethod(lmInstance, methodID, latency);
      //  if (latencyMeasurement != nullptr) {
       //     delete latencyMeasurement;
        //}
       // destroyLatencyMeasurementClass();
        javaVM->DetachCurrentThread();
        //###############################################################################
    }, [] () { // playRequestCallaback
        javaVM->AttachCurrentThread(&jnienv, nullptr);
        auto methodID = jnienv->GetMethodID(latencyMeasurementJavaClass, "playSinWaveRequest", "()V");
        jnienv->CallVoidMethod(lmInstance, methodID);
        javaVM->DetachCurrentThread();
    });
    latencyMeasurement->startTest(bitDepth, sampleRate);
}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_setDataSourceJNI(
        JNIEnv *env, jstring path) {
    ALOGV("setDataSourceJNI");
}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_pauseJNI(
        JNIEnv *env, jobject instance) {
    ALOGV("pauseJNI");
    // TODO

}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_playJNI(
        JNIEnv *env, jobject instance) {
    ALOGV("playJNI");
    // TODO
    if (player) {
        player->play();
    }
}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_prepareJNI(
        JNIEnv *env, jobject instance) {
    ALOGV("prepareJNI");
    // TODO
    if (player) {
        player->prepare();
    }
}

JNIEXPORT jint JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_getDurationJNI(
        JNIEnv *env, jobject instance) {
    ALOGV("getDurationJNI");
    // TODO

    return 0;
}

JNIEXPORT jint JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_getCurrentPositionJNI(
        JNIEnv *env, jobject instance) {
    ALOGV("getCurrentPosition");
    // TODO
    return 0;
}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_releaseJNI(
        JNIEnv *env, jobject instance) {
    ALOGV("releaseJNI");
    if (player) {
        player.release();
        player = nullptr;
    }
}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_seekToJNI(
        JNIEnv *env, jobject instance) {
    ALOGV("seekToJNI");
    // TODO

}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_nativeplayerapp_MainActivity_playJNI(
        JNIEnv *env, jobject instance, jint ms) {
    // TODO

}

#ifdef __cplusplus
};
#endif