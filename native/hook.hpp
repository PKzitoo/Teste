#pragma once
#include <cstdint>
#include <cstring>
#include <sys/mman.h>
#include <unistd.h>

extern "C" inline bool hook_abs(void* target, void* replacement, void** original) {
    if (!target || !replacement) return false;
    long pg = sysconf(_SC_PAGESIZE);
    uintptr_t aligned = (uintptr_t)target & ~(pg - 1);
    mprotect((void*)aligned, pg * 2, PROT_READ | PROT_WRITE | PROT_EXEC);

    static thread_local uint8_t trampo[64];
    memcpy(trampo, target, 16);
    uint32_t cont[] = { 0x58000050, 0xD61F0200 };
    memcpy(trampo + 16, cont, sizeof(cont));
    *(void**)(trampo + 24) = (uint8_t*)target + 16;
    __builtin___clear_cache((char*)trampo, (char*)trampo + 64);
    if (original) *original = trampo;

    uint32_t jmp[] = { 0x58000050, 0xD61F0200 };
    memcpy(target, jmp, sizeof(jmp));
    *(void**)((uint8_t*)target + 8) = replacement;
    __builtin___clear_cache((char*)target, (char*)target + 16);
    return true;
}
