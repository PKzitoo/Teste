#pragma once
#include <dlfcn.h>
#include <cstdint>
#include "offsets.h"

struct Il2Cpp {
    void* base = nullptr;
    void init() { base = dlopen("libil2cpp.so", RTLD_LAZY); }
    template<typename Fn>
    Fn resolve(uintptr_t offRel) {
        if (!base || !offRel) return nullptr;
        return (Fn)((uintptr_t)base + offRel);
    }
};

inline Il2Cpp g_il;
