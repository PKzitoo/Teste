#pragma once
#include <cstdint>

namespace off {
    inline uintptr_t Camera_get_main             = 0;
    inline uintptr_t Camera_WorldToScreenPoint   = 0;
    inline uintptr_t Transform_get_position      = 0;
    inline uintptr_t Transform_get_forward       = 0;
    inline uintptr_t GameObject_get_transform    = 0;
    inline uintptr_t GameObject_get_name         = 0;
    inline uintptr_t Object_FindObjectsOfType    = 0;

    inline void* PlayerController_TypeInfo       = nullptr;
    inline uintptr_t PlayerController_Health     = 0;
    inline uintptr_t PlayerController_TeamId     = 0;
    inline uintptr_t PlayerController_IsBot      = 0;
    inline uintptr_t PlayerController_Head       = 0;

    inline uintptr_t WeaponController_Recoil     = 0;
    inline uintptr_t WeaponController_FireRate   = 0;

    inline uintptr_t Movement_Speed              = 0;
    inline uintptr_t Movement_Gravity            = 0;
}
