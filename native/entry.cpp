// Entrypoint JNI do ffkit.
// Mapeia TODAS as chaves do StateBridge pros campos de g_state,
// e orquestra o boot do modulo dentro do processo do FF.

#include <jni.h>
#include <string>
#include <cstring>
#include <thread>
#include <chrono>
#include <cstdlib>
#include "state.hpp"
#include "il2cpp_glue.hpp"

extern "C" void start_worker();
extern "C" void set_data_dir(const char* p);
extern "C" void run_dumper();
extern "C" void mask_install();    // mask.cpp

extern "C" {

JNIEXPORT void JNICALL
Java_com_ffkit_1lite_NativeBridge_boot(JNIEnv* env, jclass, jstring jdir) {
    const char* c = env->GetStringUTFChars(jdir, nullptr);
    if (c) {
        set_data_dir(c);
        env->ReleaseStringUTFChars(jdir, c);
    }

    // STEALTH PRIMEIRO: instala mascaras no libc ANTES de qualquer
    // outra coisa suspeita (dlopen de libil2cpp eh observavel).
    // try/catch no C++ nao funciona com SIGSEGV de mprotect falho,
    // mas as funcoes internas ja usam best-effort.
    try { mask_install(); } catch (...) {}

    // DELAY ANTI-DETECT: Garena scanner roda nos primeiros ~10s apos
    // process start pra ver "libs carregadas". Se a gente nao estiver
    // la, o scan passa limpo. Depois disso somos invisiveis (mask).
    // Threads abaixo rodam em background sem bloquear o FF.
    std::thread([]{
        // sleep inicial pra escapar do primeiro sweep
        std::this_thread::sleep_for(std::chrono::seconds(12));

        // inicializa IL2CPP (acha ponteiro base da libil2cpp.so)
        g_il.init();

        // dump de offsets uma vez (preenche namespace off::)
        run_dumper();

        // worker ja pode rodar — scanPlayers, aim, features
        start_worker();
    }).detach();
}

JNIEXPORT void JNICALL
Java_com_ffkit_1lite_NativeBridge_panic(JNIEnv*, jclass) {
    g_state.panic.store(true);
    g_state.aim_enabled.store(false);
    g_state.esp_mode.store(0);
    g_state.esp_enabled.store(false);
    g_state.no_recoil.store(false);
    g_state.no_spread.store(false);
    g_state.fast_fire.store(false);
    g_state.no_reload.store(false);
    g_state.infinite_ammo.store(false);
    g_state.one_shot_hs.store(false);
    g_state.any_gun_awm.store(false);
    g_state.pierce_walls.store(false);
    g_state.magic_bullet.store(false);
    g_state.speed_hack.store(false);
    g_state.fly_mode.store(false);
    g_state.super_jump.store(false);
    g_state.bhop.store(false);
    g_state.instant_revive.store(false);
    g_state.no_collision.store(false);
    g_state.wide_view.store(false);
    g_state.loot_esp.store(false);
    g_state.vehicle_esp.store(false);
    g_state.minimap_enemy.store(false);
    g_state.airdrop_tracker.store(false);
    g_state.killfeed_spy.store(false);
}

// pullEnemies esta em esp.cpp (retorna g_enemies de verdade).
// Removida versao stub daqui pra evitar duplicata de simbolo JNI.

static inline bool eq(const char* a, const char* b) { return std::strcmp(a, b) == 0; }

JNIEXPORT void JNICALL
Java_com_ffkit_1lite_StateBridge_setBNative(JNIEnv* env, jclass,
                                             jstring jk, jboolean v) {
    const char* k = env->GetStringUTFChars(jk, nullptr);
    if (!k) return;
    bool b = v != 0;
    // AIM
    if      (eq(k, "aim_enabled"))      g_state.aim_enabled.store(b);
    else if (eq(k, "aim_on_scope"))     g_state.aim_on_scope.store(b);
    else if (eq(k, "aim_on_fire"))      g_state.aim_on_fire.store(b);
    else if (eq(k, "aim_ignore_bots"))  g_state.aim_ignore_bots.store(b);
    else if (eq(k, "aim_prediction"))   g_state.aim_prediction.store(b);
    else if (eq(k, "aim_multi_target")) g_state.aim_multi_target.store(b);
    else if (eq(k, "aim_silent"))       g_state.aim_silent.store(b);
    // ESP flags
    else if (eq(k, "esp_enabled"))      g_state.esp_enabled.store(b);
    else if (eq(k, "esp_box"))          g_state.esp_box.store(b);
    else if (eq(k, "esp_skeleton"))     g_state.esp_skeleton.store(b);
    else if (eq(k, "esp_health"))       g_state.esp_health.store(b);
    else if (eq(k, "esp_distance"))     g_state.esp_distance.store(b);
    else if (eq(k, "esp_name"))         g_state.esp_name.store(b);
    else if (eq(k, "esp_hide_scope"))   g_state.esp_hide_scope.store(b);
    // WEAPON
    else if (eq(k, "no_recoil"))        g_state.no_recoil.store(b);
    else if (eq(k, "no_spread"))        g_state.no_spread.store(b);
    else if (eq(k, "fast_fire"))        g_state.fast_fire.store(b);
    else if (eq(k, "no_reload"))        g_state.no_reload.store(b);
    else if (eq(k, "infinite_ammo"))    g_state.infinite_ammo.store(b);
    else if (eq(k, "one_shot_hs"))      g_state.one_shot_hs.store(b);
    else if (eq(k, "any_gun_awm"))      g_state.any_gun_awm.store(b);
    else if (eq(k, "pierce_walls"))     g_state.pierce_walls.store(b);
    else if (eq(k, "magic_bullet"))     g_state.magic_bullet.store(b);
    // MOVEMENT
    else if (eq(k, "speed_hack"))       g_state.speed_hack.store(b);
    else if (eq(k, "fly_mode"))         g_state.fly_mode.store(b);
    else if (eq(k, "no_fall_dmg"))      g_state.no_fall_dmg.store(b);
    else if (eq(k, "super_jump"))       g_state.super_jump.store(b);
    else if (eq(k, "bhop"))             g_state.bhop.store(b);
    else if (eq(k, "water_breath"))     g_state.water_breath.store(b);
    else if (eq(k, "instant_revive"))   g_state.instant_revive.store(b);
    else if (eq(k, "no_collision"))     g_state.no_collision.store(b);
    else if (eq(k, "wide_view"))        g_state.wide_view.store(b);
    // INTEL
    else if (eq(k, "grenade_alert"))    g_state.grenade_alert.store(b);
    else if (eq(k, "loot_esp"))         g_state.loot_esp.store(b);
    else if (eq(k, "zone_timer"))       g_state.zone_timer.store(b);
    else if (eq(k, "vehicle_esp"))      g_state.vehicle_esp.store(b);
    else if (eq(k, "minimap_enemy"))    g_state.minimap_enemy.store(b);
    else if (eq(k, "airdrop_tracker"))  g_state.airdrop_tracker.store(b);
    else if (eq(k, "killfeed_spy"))     g_state.killfeed_spy.store(b);
    // SAFETY
    else if (eq(k, "spectator_warn"))   g_state.spectator_warn.store(b);
    else if (eq(k, "streamer_mode"))    g_state.streamer_mode.store(b);
    else if (eq(k, "hs_cap_dynamic"))   g_state.hs_cap_dynamic.store(b);
    else if (eq(k, "replay_sanit"))     g_state.replay_sanit.store(b);
    else if (eq(k, "signature_rot"))    g_state.signature_rot.store(b);
    // SYS
    else if (eq(k, "flag_secure"))      g_state.flag_secure.store(b);
    env->ReleaseStringUTFChars(jk, k);
}

JNIEXPORT void JNICALL
Java_com_ffkit_1lite_StateBridge_setFNative(JNIEnv* env, jclass,
                                             jstring jk, jfloat v) {
    const char* k = env->GetStringUTFChars(jk, nullptr);
    if (!k) return;
    if      (eq(k, "aim_fov"))       g_state.aim_fov.store(v);
    else if (eq(k, "aim_smooth"))    g_state.aim_smooth.store(v);
    else if (eq(k, "esp_max_dist"))  g_state.esp_max_dist.store(v);
    else if (eq(k, "esp_audio_thr")) g_state.esp_audio_thr.store(v);
    else if (eq(k, "speed_mult"))    g_state.speed_mult.store(v);
    env->ReleaseStringUTFChars(jk, k);
}

JNIEXPORT void JNICALL
Java_com_ffkit_1lite_StateBridge_setINative(JNIEnv* env, jclass,
                                             jstring jk, jint v) {
    const char* k = env->GetStringUTFChars(jk, nullptr);
    if (!k) return;
    if      (eq(k, "aim_reaction_ms")) g_state.aim_reaction_ms.store(v);
    else if (eq(k, "shot_head_count")) g_state.shot_head_count.store(v);
    else if (eq(k, "shot_body_count")) g_state.shot_body_count.store(v);
    else if (eq(k, "esp_mode")) {
        g_state.esp_mode.store(v);
        g_state.esp_enabled.store(v != 0);  // compat flag
    }
    else if (eq(k, "esp_delay_ms"))    g_state.esp_delay_ms.store(v);
    else if (eq(k, "auto_off_kills"))  g_state.auto_off_kills.store(v);
    env->ReleaseStringUTFChars(jk, k);
}

} // extern "C"
