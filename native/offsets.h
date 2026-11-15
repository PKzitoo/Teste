#pragma once
#include <cstdint>

namespace off {
    // UnityEngine
    inline uintptr_t Camera_get_main             = 0;
    inline uintptr_t Camera_WorldToScreenPoint   = 0;
    inline uintptr_t Camera_set_fieldOfView      = 0;
    inline uintptr_t Camera_get_transform        = 0;
    inline uintptr_t Transform_get_position      = 0;
    inline uintptr_t Transform_get_forward       = 0;
    inline uintptr_t Transform_set_rotation      = 0;
    inline uintptr_t Transform_get_rotation      = 0;
    inline uintptr_t GameObject_get_transform    = 0;
    inline uintptr_t GameObject_get_name         = 0;
    inline uintptr_t Object_FindObjectsOfType    = 0;

    // PlayerController
    inline void*     PlayerController_TypeInfo   = nullptr;
    inline uintptr_t PlayerController_Health     = 0;
    inline uintptr_t PlayerController_TeamId     = 0;
    inline uintptr_t PlayerController_IsBot      = 0;
    inline uintptr_t PlayerController_IsLocal    = 0;  // novo: detectar jogador local
    inline uintptr_t PlayerController_Head       = 0;
    inline uintptr_t PlayerController_Neck       = 0;  // novo: bone 1
    inline uintptr_t PlayerController_Chest      = 0;  // novo: bone 2 (peito)
    inline uintptr_t PlayerController_Transform  = 0;  // novo: pos do corpo
    inline uintptr_t PlayerController_Weapon     = 0;  // novo: arma equipada

    // Weapon
    inline uintptr_t WeaponController_Recoil     = 0;
    inline uintptr_t WeaponController_FireRate   = 0;
    inline uintptr_t WeaponController_Spread     = 0;  // no-spread
    inline uintptr_t WeaponController_Ammo       = 0;  // infinite ammo
    inline uintptr_t WeaponController_Damage     = 0;  // one-shot

    // Movement
    inline uintptr_t Movement_Speed              = 0;
    inline uintptr_t Movement_Gravity            = 0;
    inline uintptr_t Movement_JumpPower          = 0;  // super jump
    inline uintptr_t Movement_FallDamage         = 0;  // no fall damage
}
