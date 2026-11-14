// Entrypoint JNI do ffkit v16-root.
// Chamado do Kotlin (NativeBridge + StateBridge).

#include <jni.h>
#include <string>
#include <cstring>
#include <thread>
#include "state.hpp"

extern "C" void start_worker();
extern "C" void set_data_dir(const char* p);
extern "C" void run_dumper();

extern "C" {

// === NativeBridge ===

JNIEXPORT void JNICALL
Java_com_ffkit_1lite_NativeBridge_boot(JNIEnv* env, jclass, jstring jdir) {
    const char* c = env->GetStringUTFChars(jdir, nullptr);
    if (c) {
        set_data_dir(c);
        env->ReleaseStringUTFChars(jdir, c);
    }
    // dumper roda uma vez em background (nao bloqueia FF ao abrir)
    std::thread([]{ run_dumper(); }).detach();
    start_worker();
}

JNIEXPORT void JNICALL
Java_com_ffkit_1lite_NativeBridge_panic(JNIEnv*, jclass) {
    g_state.panic.store(true);
    g_state.aim_enabled.store(false);
    g_state.esp_enabled.store(false);
    g_state.no_recoil.store(false);
    g_state.speed_hack.store(false);
    g_state.fly_mode.store(false);
    g_state.fast_fire.store(false);
    g_state.wide_view.store(false);
}

JNIEXPORT jobjectArray JNICALL
Java_com_ffkit_1lite_NativeBridge_pullEnemies(JNIEnv* env, jclass) {
    // obsoleto na v16-root (usado no CV antigo). Mantido pra compat JNI.
    jclass fac = env->FindClass("[F");
    return env->NewObjectArray(0, fac, nullptr);
}

// === StateBridge ===

static inline bool eq(const char* a, const char* b) { return std::strcmp(a, b) == 0; }

JNIEXPORT void JNICALL
Java_com_ffkit_1lite_StateBridge_setBNative(JNIEnv* env, jclass,
                                             jstring jk, jboolean v) {
    const char* k = env->GetStringUTFChars(jk, nullptr);
    if (!k) return;
    bool b = v != 0;
    if      (eq(k, "aim_enabled"))     g_state.aim_enabled.store(b);
    else if (eq(k, "aim_on_scope"))    g_state.aim_on_scope.store(b);
    else if (eq(k, "aim_on_fire"))     g_state.aim_on_fire.store(b);
    else if (eq(k, "aim_ignore_bots")) g_state.aim_ignore_bots.store(b);
    else if (eq(k, "aim_bone_random")) g_state.aim_bone_random.store(b);
    else if (eq(k, "esp_enabled"))     g_state.esp_enabled.store(b);
    else if (eq(k, "esp_box"))         g_state.esp_box.store(b);
    else if (eq(k, "esp_skeleton"))    g_state.esp_skeleton.store(b);
    else if (eq(k, "esp_health"))      g_state.esp_health.store(b);
    else if (eq(k, "esp_distance"))    g_state.esp_distance.store(b);
    else if (eq(k, "esp_name"))        g_state.esp_name.store(b);
    else if (eq(k, "no_recoil"))       g_state.no_recoil.store(b);
    else if (eq(k, "speed_hack"))      g_state.speed_hack.store(b);
    else if (eq(k, "fly_mode"))        g_state.fly_mode.store(b);
    else if (eq(k, "fast_fire"))       g_state.fast_fire.store(b);
    else if (eq(k, "wide_view"))       g_state.wide_view.store(b);
    else if (eq(k, "flag_secure"))     g_state.flag_secure.store(b);
    env->ReleaseStringUTFChars(jk, k);
}

JNIEXPORT void JNICALL
Java_com_ffkit_1lite_StateBridge_setFNative(JNIEnv* env, jclass,
                                             jstring jk, jfloat v) {
    const char* k = env->GetStringUTFChars(jk, nullptr);
    if (!k) return;
    if      (eq(k, "aim_fov"))       g_state.aim_fov.store(v);
    else if (eq(k, "aim_smooth"))    g_state.aim_smooth.store(v);
    else if (eq(k, "aim_miss_rate")) g_state.aim_miss_rate.store(v);
    else if (eq(k, "esp_max_dist"))  g_state.esp_max_dist.store(v);
    else if (eq(k, "speed_mult"))    g_state.speed_mult.store(v);
    env->ReleaseStringUTFChars(jk, k);
}

JNIEXPORT void JNICALL
Java_com_ffkit_1lite_StateBridge_setINative(JNIEnv* env, jclass,
                                             jstring jk, jint v) {
    const char* k = env->GetStringUTFChars(jk, nullptr);
    if (!k) return;
    if      (eq(k, "aim_bone"))         g_state.aim_bone.store(v);
    else if (eq(k, "aim_reaction_ms"))  g_state.aim_reaction_ms.store(v);
    else if (eq(k, "aim_kpm_cap"))      g_state.aim_kpm_cap.store(v);
    else if (eq(k, "aim_max_hs_streak"))g_state.aim_max_hs_streak.store(v);
    env->ReleaseStringUTFChars(jk, k);
}

} // extern "C"
