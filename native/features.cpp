#include "state.hpp"
#include "memory.hpp"
#include "offsets.h"

extern void* getLocalPlayer();
extern void* getLocalWeapon();

void tick_features() {
    void* lp = getLocalPlayer();
    void* wp = getLocalWeapon();

    if (lp) {
        if (g_state.speed_hack && off::Movement_Speed) {
            float v = 5.5f * g_state.speed_mult.load();
            writeT<float>((uintptr_t)lp + off::Movement_Speed, v);
        }
        if (g_state.low_gravity && off::Movement_Gravity) {
            writeT<float>((uintptr_t)lp + off::Movement_Gravity,
                          9.8f * g_state.gravity_mult.load());
        }
        if (g_state.fly_mode && off::Movement_Gravity) {
            writeT<float>((uintptr_t)lp + off::Movement_Gravity, -0.5f);
        }
    }
    if (wp) {
        if (g_state.no_recoil && off::WeaponController_Recoil)
            writeT<float>((uintptr_t)wp + off::WeaponController_Recoil, 0.f);
        if (g_state.fast_fire && off::WeaponController_FireRate)
            writeT<float>((uintptr_t)wp + off::WeaponController_FireRate, 0.02f);
    }
}

extern "C" void injectCameraDelta(float dx, float dy) {
    (void)dx; (void)dy;
    // injeta via offset de CameraController.pitch/yaw apos o dump.
}
