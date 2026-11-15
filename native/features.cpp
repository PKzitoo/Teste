#include "state.hpp"
#include "memory.hpp"
#include "offsets.h"
#include "il2cpp_glue.hpp"
#include "math.hpp"
#include <atomic>

extern void* getLocalPlayer();
extern void* getLocalWeapon();

// deltas pendentes de aim; injectCameraDelta() acumula, tick_features() aplica
// na rotacao do camera transform (se offsets ja encontrados).
static std::atomic<float> g_aim_dx{0.f};
static std::atomic<float> g_aim_dy{0.f};

/**
 * Chamado do aimTick: acumula o delta pra ser aplicado no proximo tick
 * em cima do CameraController. Separa a decisao (aimTick) da aplicacao
 * (tick_features) pra isolar o hot path do loop de IL2CPP.
 */
extern "C" void injectCameraDelta(float dx, float dy) {
    g_aim_dx.store(g_aim_dx.load() + dx);
    g_aim_dy.store(g_aim_dy.load() + dy);
}

// aplica delta de camera: le rotacao atual, soma yaw/pitch, escreve de volta.
// Usa Camera.main.transform (via get_main -> get_transform).
static void applyCameraDelta() {
    float dx = g_aim_dx.exchange(0.f);
    float dy = g_aim_dy.exchange(0.f);
    if (dx == 0.f && dy == 0.f) return;

    if (!off::Camera_get_main || !off::Camera_get_transform ||
        !off::Transform_get_rotation || !off::Transform_set_rotation) return;

    using CamFn  = void*(*)();
    using GetT   = void*(*)(void*);
    using GetRot = Quat(*)(void*);
    using SetRot = void(*)(void*, Quat);

    auto getCam = g_il.resolve<CamFn>(off::Camera_get_main);
    auto getT   = g_il.resolve<GetT>(off::Camera_get_transform);
    auto getRot = g_il.resolve<GetRot>(off::Transform_get_rotation);
    auto setRot = g_il.resolve<SetRot>(off::Transform_set_rotation);
    if (!getCam || !getT || !getRot || !setRot) return;

    void* cam = getCam();  if (!cam) return;
    void* tr  = getT(cam); if (!tr)  return;

    Quat q = getRot(tr);
    // converte dx/dy (pixels) em radianos (sensibilidade ~ 500 px/360 graus)
    const float SENS = 0.00349f;  // ~1 pixel = 0.2 graus em rad
    Quat yaw   = quatYaw(dx * SENS);
    Quat pitch = quatPitch(dy * SENS);
    q = quatMul(quatMul(yaw, q), pitch);
    setRot(tr, q);
}

/**
 * tick_features roda a cada 8ms junto com o scan.
 * Le state.* e aplica patches na memoria do jogador local + arma equipada.
 */
void tick_features() {
    void* lp = getLocalPlayer();
    void* wp = getLocalWeapon();

    // ===== MOVEMENT (local player) =====
    if (lp) {
        // em FF, movement controller geralmente vive em offset dentro do player.
        // Por falta de dumper dedicado, usamos lp como base — se nao bater,
        // os writes falham silenciosos (nao crasha).
        uintptr_t mv = (uintptr_t)lp;

        if (g_state.speed_hack.load() && off::Movement_Speed) {
            float v = 5.5f * g_state.speed_mult.load();
            writeT<float>(mv + off::Movement_Speed, v);
        }
        if (g_state.fly_mode.load() && off::Movement_Gravity) {
            writeT<float>(mv + off::Movement_Gravity, -0.5f);  // gravidade negativa = flutua
        } else if (g_state.low_gravity.load() && off::Movement_Gravity) {
            writeT<float>(mv + off::Movement_Gravity,
                          9.8f * g_state.gravity_mult.load());
        }
        if (g_state.super_jump.load() && off::Movement_JumpPower) {
            writeT<float>(mv + off::Movement_JumpPower, 15.0f);  // ~2x o padrao
        }
        if (g_state.no_fall_dmg.load() && off::Movement_FallDamage) {
            writeT<float>(mv + off::Movement_FallDamage, 0.f);
        }
    }

    // ===== WEAPON (arma equipada) =====
    if (wp) {
        if (g_state.no_recoil.load() && off::WeaponController_Recoil)
            writeT<float>((uintptr_t)wp + off::WeaponController_Recoil, 0.f);
        if (g_state.no_spread.load() && off::WeaponController_Spread)
            writeT<float>((uintptr_t)wp + off::WeaponController_Spread, 0.f);
        if (g_state.fast_fire.load() && off::WeaponController_FireRate)
            writeT<float>((uintptr_t)wp + off::WeaponController_FireRate, 0.02f);
        if (g_state.infinite_ammo.load() && off::WeaponController_Ammo)
            writeT<int>((uintptr_t)wp + off::WeaponController_Ammo, 999);
        if (g_state.one_shot_hs.load() && off::WeaponController_Damage)
            writeT<float>((uintptr_t)wp + off::WeaponController_Damage, 999.f);
        else if (g_state.any_gun_awm.load() && off::WeaponController_Damage)
            writeT<float>((uintptr_t)wp + off::WeaponController_Damage, 130.f);  // dano AWM
    }

    // ===== CAMERA (FOV / aim write) =====
    if (g_state.wide_view.load() && off::Camera_set_fieldOfView &&
        off::Camera_get_main) {
        using CamFn = void*(*)();
        using SetFov = void(*)(void*, float);
        auto getCam = g_il.resolve<CamFn>(off::Camera_get_main);
        auto setFov = g_il.resolve<SetFov>(off::Camera_set_fieldOfView);
        if (getCam && setFov) {
            void* cam = getCam();
            if (cam) setFov(cam, 95.f);  // FOV padrao ~65, wide = 95
        }
    }

    // aplica aim delta acumulado (silent aim ou soft aim)
    applyCameraDelta();
}
