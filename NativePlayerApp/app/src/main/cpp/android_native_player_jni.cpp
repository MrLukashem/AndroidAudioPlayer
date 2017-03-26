//
// Created by MrLukashem on 25.03.2017.
//

#include <jni.h>
#include <string>
#include <android/log.h>

#define APPNAME "android_native_player_jni"
#define ALOGV(x) __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, x);

#ifdef __cplusplus
extern "C" {
#endif

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

}

JNIEXPORT void JNICALL
Java_com_player_mrlukashem_customplayer_nativeplayer_NativePlayerJNIBridge_prepareJNI(
        JNIEnv *env, jobject instance) {
    ALOGV("prepareJNI");
    // TODO

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
    // TODO

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