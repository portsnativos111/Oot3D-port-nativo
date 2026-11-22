#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "OoT3D_Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT void JNICALL
Java_com_portsnativos111_oot3dport_MainActivity_startNativeEngine(
        JNIEnv* env,
        jobject /* this */,
        jstring romUriStr,
        jstring patchUriStr) {

    const char *romChars = env->GetStringUTFChars(romUriStr, nullptr);
    const char *patchChars = env->GetStringUTFChars(patchUriStr, nullptr);

    std::string romUri(romChars);
    std::string patchUri(patchChars);

    LOGI("ROM URI: %s", romUri.c_str());
    LOGI("SingleScreen URI: %s", patchUri.c_str());

    env->ReleaseStringUTFChars(romUriStr, romChars);
    env->ReleaseStringUTFChars(patchUriStr, patchChars);
}
