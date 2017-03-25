#include <jni.h>
#include <string>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

extern "C"
jstring
Java_com_player_mrlukashem_nativeplayerapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    std::string data = "/storage/emulated/0/Music/Trivium - (2005) - Ascendancy   x/01. The End Of Everything.mp3";

    SLObjectItf engine_obj;
    slCreateEngine( &engine_obj, 0, nullptr, 0, nullptr, nullptr );
// ...
// Now realize the player here.
    return env->NewStringUTF(hello.c_str());
}
