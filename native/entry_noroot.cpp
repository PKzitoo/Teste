#include <jni.h>
#include <thread>
#include <chrono>
#include <dlfcn.h>
#include "il2cpp_glue.hpp"
#include "state.hpp"

extern "C" void start_worker();
extern "C" void run_dumper();
extern "C" void set_data_dir(const char*);
extern "C" void mask_install();

extern "C" JNIEXPORT void JNICALL
Java_com_ffkit_lite_NativeBridge_boot(JNIEnv* env, jclass, jstring jdir) {
    const char* c = env->GetStringUTFChars(jdir, nullptr);
    set_data_dir(c);
    env->ReleaseStringUTFChars(jdir, c);

    std::thread([](){
        while (!dlopen("libil2cpp.so", RTLD_NOLOAD))
            std::this_thread::sleep_for(std::chrono::milliseconds(200));
        g_il.init();
        mask_install();
        run_dumper();
        start_worker();
    }).detach();
}

extern "C" JNIEXPORT void JNICALL
Java_com_ffkit_lite_NativeBridge_panic(JNIEnv*, jclass) {
    g_state.panic = true;
}

extern "C" JNIEXPORT void JNICALL
Java_com_ffkit_lite_StateBridge_setB(JNIEnv* env, jclass, jstring jk, jboolean v) {
    const char* k = env->GetStringUTFChars(jk, nullptr);
    std::string key = k; env->ReleaseStringUTFChars(jk, k);
    if      (key == "aim_enabled")     g_state.aim_enabled = v;
    else if (key == "aim_on_scope")    g_state.aim_on_scope = v;
    else if (key == "aim_on_fire")     g_state.aim_on_fire = v;
    else if (key == "aim_ignore_bots") g_state.aim_ignore_bots = v;
    else if (key == "no_recoil")       g_state.no_recoil = v;
    else if (key == "speed_hack")      g_state.speed_hack = v;
    else if (key == "flag_secure")     g_state.flag_secure = v;
}

extern "C" JNIEXPORT void JNICALL
Java_com_ffkit_lite_StateBridge_setF(JNIEnv* env, jclass, jstring jk, jfloat v) {
    const char* k = env->GetStringUTFChars(jk, nullptr);
    std::string key = k; env->ReleaseStringUTFChars(jk, k);
    if      (key == "aim_fov")    g_state.aim_fov = v;
    else if (key == "aim_smooth") g_state.aim_smooth = v;
}

extern "C" JNIEXPORT void JNICALL
Java_com_ffkit_lite_StateBridge_setI(JNIEnv* env, jclass, jstring jk, jint v) {
    const char* k = env->GetStringUTFChars(jk, nullptr);
    std::string key = k; env->ReleaseStringUTFChars(jk, k);
    if (key == "aim_bone") g_state.aim_bone = v;
}
