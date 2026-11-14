#pragma once
#include <cstdint>
#include <sys/mman.h>
#include <unistd.h>

template<typename T>
inline T readT(uintptr_t addr) {
    if (!addr) return T{};
    return *reinterpret_cast<T*>(addr);
}

template<typename T>
inline void writeT(uintptr_t addr, T v) {
    if (!addr) return;
    long page = sysconf(_SC_PAGESIZE);
    uintptr_t aligned = addr & ~(page - 1);
    mprotect((void*)aligned, page, PROT_READ | PROT_WRITE | PROT_EXEC);
    *reinterpret_cast<T*>(addr) = v;
}
