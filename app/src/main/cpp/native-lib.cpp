#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "OoT3D_Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT void JNICALL
Java_com_portsnativos111_oot3dport_MainActivity_startNativeEngine(
        JNIEnv* env,
        jobject /* this */,
        jstring internalFilesPath) {

    const char *path = env->GetStringUTFChars(internalFilesPath, nullptr);
    std::string basePath(path);
    std::string romPath = basePath + "/oot3d_base.rom";
    std::string patchPath = basePath + "/SingleScreen_3.2.2.zip";

    LOGI("Arrancando motor con ROM en: %s", romPath.c_str());
    LOGI("Aplicando mod SingleScreen desde: %s", patchPath.c_str());

    env->ReleaseStringUTFChars(internalFilesPath, path);
}
