#include <dlfcn.h>
#include <cstring>
#include <cstdio>
#include <cstdlib>
#include <string>
#include <dirent.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/syscall.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include "hook.hpp"

// === tipos dos hooks ===
using open_t      = int(*)(const char*, int, ...);
using open64_t    = int(*)(const char*, int, ...);
using fopen_t     = FILE*(*)(const char*, const char*);
using fopen64_t   = FILE*(*)(const char*, const char*);
using access_t    = int(*)(const char*, int);
using stat_t      = int(*)(const char*, struct stat*);
using lstat_t     = int(*)(const char*, struct stat*);
using readlink_t  = ssize_t(*)(const char*, char*, size_t);
using getprop_t   = int(*)(const char*, char*);
using readdir_t   = struct dirent*(*)(DIR*);
using getppid_t   = pid_t(*)();
using getenv_t    = char*(*)(const char*);

static open_t      real_open     = nullptr;
static open64_t    real_open64   = nullptr;
static fopen_t     real_fopen    = nullptr;
static fopen64_t   real_fopen64  = nullptr;
static access_t    real_access   = nullptr;
static stat_t      real_stat     = nullptr;
static lstat_t     real_lstat    = nullptr;
static readlink_t  real_readlink = nullptr;
static getprop_t   real_getprop  = nullptr;
static readdir_t   real_readdir  = nullptr;
static getppid_t   real_getppid  = nullptr;
static getenv_t    real_getenv   = nullptr;

// === strings suspeitas que removemos de qualquer saida de /proc ===
static const char* g_needles[] = {
    "libffkit.so", "libva++.so", "virtualapp",
    "com.ffkit", "/data/data/com.ffkit", "lody", "stub_", "ffkit",
    "magisk", "magiskhide", "zygisk", "riru", "edxp",
    "lsposed", "lspd", "lspatch", "lsplant", "shamiko",
    "KernelSU", "ksud", "io.github.vvb2060",
    nullptr
};

static bool isLeakyPath(const char* p) {
    if (!p) return false;
    // /proc/self/*
    if (!strcmp(p, "/proc/self/maps")) return true;
    if (!strcmp(p, "/proc/self/smaps")) return true;
    if (!strcmp(p, "/proc/self/status")) return true;
    if (!strcmp(p, "/proc/self/cmdline")) return true;
    if (!strcmp(p, "/proc/self/cgroup")) return true;
    if (!strcmp(p, "/proc/self/mountinfo")) return true;
    if (!strcmp(p, "/proc/self/mounts")) return true;
    if (!strcmp(p, "/proc/self/task")) return true;
    if (!strcmp(p, "/proc/self/comm")) return true;
    // /proc/<pid>/maps generico
    if (strstr(p, "/proc/") == p &&
        (strstr(p, "/maps") || strstr(p, "/status") ||
         strstr(p, "/cmdline") || strstr(p, "/cgroup"))) return true;
    // paths de magisk / lsposed / zygisk
    if (strstr(p, "/magisk")) return true;
    if (strstr(p, "/.magisk")) return true;
    if (strstr(p, "lsposed")) return true;
    if (strstr(p, "/su")) return true;
    if (strstr(p, "/busybox")) return true;
    return false;
}

static bool isMagiskPath(const char* p) {
    if (!p) return false;
    return strstr(p, "/magisk") || strstr(p, "/.magisk") ||
           strstr(p, "lsposed") || strstr(p, "/su") ||
           !strcmp(p, "/system/bin/su") ||
           !strcmp(p, "/system/xbin/su");
}

// filtra o conteudo em memoria removendo linhas suspeitas
static std::string sanitize(const std::string& in) {
    std::string s = in;
    for (int i = 0; g_needles[i]; i++) {
        size_t pos = 0;
        while ((pos = s.find(g_needles[i], pos)) != std::string::npos) {
            size_t ln = s.rfind('\n', pos);
            if (ln == std::string::npos) ln = 0; else ln++;
            size_t le = s.find('\n', pos);
            if (le == std::string::npos) le = s.size();
            s.erase(ln, le - ln + 1);
            pos = ln;
        }
    }
    return s;
}

static int fake_open(const char* path, int flags, ...) {
    if (isLeakyPath(path)) {
        int memfd = (int)syscall(SYS_memfd_create, "x", 0);
        int src = real_open(path, O_RDONLY);
        if (src < 0 || memfd < 0) return real_open(path, flags);
        std::string all;
        char buf[8192]; ssize_t n;
        while ((n = read(src, buf, sizeof(buf))) > 0) all.append(buf, n);
        close(src);
        std::string clean = sanitize(all);
        write(memfd, clean.data(), clean.size());
        lseek(memfd, 0, SEEK_SET);
        return memfd;
    }
    if (isMagiskPath(path)) { errno = ENOENT; return -1; }
    return real_open(path, flags);
}

static int fake_open64(const char* path, int flags, ...) {
    if (isLeakyPath(path) || isMagiskPath(path)) {
        return fake_open(path, flags);
    }
    return real_open64(path, flags);
}

static FILE* fake_fopen(const char* path, const char* mode) {
    if (isLeakyPath(path)) {
        int fd = fake_open(path, O_RDONLY);
        if (fd < 0) return nullptr;
        return fdopen(fd, mode);
    }
    if (isMagiskPath(path)) { errno = ENOENT; return nullptr; }
    return real_fopen(path, mode);
}

static FILE* fake_fopen64(const char* path, const char* mode) {
    if (isLeakyPath(path)) {
        int fd = fake_open(path, O_RDONLY);
        if (fd < 0) return nullptr;
        return fdopen(fd, mode);
    }
    if (isMagiskPath(path)) { errno = ENOENT; return nullptr; }
    return real_fopen64 ? real_fopen64(path, mode) : real_fopen(path, mode);
}

static int fake_access(const char* path, int mode) {
    if (isMagiskPath(path)) { errno = ENOENT; return -1; }
    return real_access(path, mode);
}

static int fake_stat(const char* path, struct stat* s) {
    if (isMagiskPath(path)) { errno = ENOENT; return -1; }
    return real_stat(path, s);
}

static int fake_lstat(const char* path, struct stat* s) {
    if (isMagiskPath(path)) { errno = ENOENT; return -1; }
    return real_lstat(path, s);
}

static ssize_t fake_readlink(const char* path, char* buf, size_t sz) {
    ssize_t r = real_readlink(path, buf, sz);
    if (r > 0 && path && strstr(path, "exe")) {
        const char* fake = "/data/app/com.dts.freefireth-1/base.apk";
        strncpy(buf, fake, sz);
        r = (ssize_t)strlen(fake);
    }
    return r;
}

static int fake_getprop(const char* key, char* value) {
    static const char* fake_vals[][2] = {
        {"ro.secure",            "1"},
        {"ro.debuggable",        "0"},
        {"ro.build.selinux",     "1"},
        {"ro.build.type",        "user"},
        {"ro.build.tags",        "release-keys"},
        {"service.adb.root",     "0"},
        {"init.svc.magisk",      ""},
        {"init.svc.magisk_pfs",  ""},
        {"ro.boot.verifiedbootstate", "green"},
        {"ro.boot.flash.locked", "1"},
        {nullptr, nullptr}
    };
    for (int i = 0; fake_vals[i][0]; i++) {
        if (!strcmp(key, fake_vals[i][0])) {
            strcpy(value, fake_vals[i][1]);
            return (int)strlen(value);
        }
    }
    return real_getprop(key, value);
}

static struct dirent* fake_readdir(DIR* d) {
    while (true) {
        struct dirent* e = real_readdir(d);
        if (!e) return nullptr;
        // filtra entradas numericas (PIDs) cujo cmdline bate com algo suspeito
        if (e->d_name[0] >= '0' && e->d_name[0] <= '9') {
            char p[64]; snprintf(p, sizeof(p), "/proc/%s/cmdline", e->d_name);
            int fd = real_open(p, O_RDONLY);
            if (fd >= 0) {
                char cm[256] = {0};
                read(fd, cm, sizeof(cm) - 1);
                close(fd);
                if (strstr(cm, "com.ffkit") ||
                    strstr(cm, "VirtualApp") ||
                    strstr(cm, "magisk") ||
                    strstr(cm, "lsposed") ||
                    strstr(cm, "zygisk")) continue;
            }
        }
        // filtra arquivos cujo nome bate direto
        if (strstr(e->d_name, "ffkit") ||
            strstr(e->d_name, "magisk") ||
            strstr(e->d_name, "lsposed")) continue;
        return e;
    }
}

static pid_t g_zygote_pid = 0;
static pid_t fake_getppid() {
    if (!g_zygote_pid) {
        DIR* d = opendir("/proc");
        if (d) {
            struct dirent* e;
            while ((e = real_readdir(d))) {
                if (e->d_name[0] < '0' || e->d_name[0] > '9') continue;
                char p[64]; snprintf(p, sizeof(p), "/proc/%s/cmdline", e->d_name);
                int fd = real_open(p, O_RDONLY); if (fd < 0) continue;
                char cm[64] = {0}; read(fd, cm, sizeof(cm)-1); close(fd);
                if (!strcmp(cm, "zygote64") || !strcmp(cm, "zygote")) {
                    g_zygote_pid = atoi(e->d_name); break;
                }
            }
            closedir(d);
        }
    }
    return g_zygote_pid ? g_zygote_pid : real_getppid();
}

static char* fake_getenv(const char* k) {
    if (k && !strcmp(k, "ANDROID_DATA")) {
        static char buf[] = "/data";
        return buf;
    }
    // mascarar vars de LD_* que indicam debug/injecao
    if (k && (!strcmp(k, "LD_PRELOAD") || !strcmp(k, "LD_LIBRARY_PATH"))) {
        return nullptr;  // "nao existe"
    }
    return real_getenv(k);
}

extern "C" void mask_install() {
    void* libc = dlopen("libc.so", RTLD_NOW);
    if (!libc) return;

    hook_abs(dlsym(libc, "open"),     (void*)fake_open,     (void**)&real_open);
    hook_abs(dlsym(libc, "open64"),   (void*)fake_open64,   (void**)&real_open64);
    hook_abs(dlsym(libc, "fopen"),    (void*)fake_fopen,    (void**)&real_fopen);
    hook_abs(dlsym(libc, "fopen64"),  (void*)fake_fopen64,  (void**)&real_fopen64);
    hook_abs(dlsym(libc, "access"),   (void*)fake_access,   (void**)&real_access);
    hook_abs(dlsym(libc, "stat"),     (void*)fake_stat,     (void**)&real_stat);
    hook_abs(dlsym(libc, "lstat"),    (void*)fake_lstat,    (void**)&real_lstat);
    hook_abs(dlsym(libc, "readlink"), (void*)fake_readlink, (void**)&real_readlink);
    hook_abs(dlsym(libc, "readdir"),  (void*)fake_readdir,  (void**)&real_readdir);
    hook_abs(dlsym(libc, "getppid"),  (void*)fake_getppid,  (void**)&real_getppid);
    hook_abs(dlsym(libc, "getenv"),   (void*)fake_getenv,   (void**)&real_getenv);

    void* libcutil = dlopen("libcutils.so", RTLD_NOW);
    if (libcutil) {
        void* gp = dlsym(libcutil, "__system_property_get");
        hook_abs(gp, (void*)fake_getprop, (void**)&real_getprop);
    }
}
