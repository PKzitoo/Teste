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
#include "hook.hpp"

using open_t     = int(*)(const char*, int, ...);
using readlink_t = ssize_t(*)(const char*, char*, size_t);
using getprop_t  = int(*)(const char*, char*);
using readdir_t  = struct dirent*(*)(DIR*);
using getppid_t  = pid_t(*)();
using getenv_t   = char*(*)(const char*);

static open_t     real_open     = nullptr;
static readlink_t real_readlink = nullptr;
static getprop_t  real_getprop  = nullptr;
static readdir_t  real_readdir  = nullptr;
static getppid_t  real_getppid  = nullptr;
static getenv_t   real_getenv   = nullptr;

static bool isLeakyPath(const char* p) {
    if (!p) return false;
    return !strcmp(p, "/proc/self/maps") ||
           !strcmp(p, "/proc/self/smaps") ||
           !strcmp(p, "/proc/self/status") ||
           !strcmp(p, "/proc/self/cmdline") ||
           !strcmp(p, "/proc/self/cgroup") ||
           !strcmp(p, "/proc/self/mountinfo") ||
           (strstr(p, "/proc/") == p && strstr(p, "/maps"));
}

static int fake_open(const char* path, int flags, ...) {
    if (isLeakyPath(path)) {
        int memfd = (int)syscall(SYS_memfd_create, "x", 0);
        int src = real_open(path, O_RDONLY);
        if (src < 0 || memfd < 0) return real_open(path, flags);
        char buf[8192]; ssize_t n;
        while ((n = read(src, buf, sizeof(buf))) > 0) {
            std::string s(buf, n);
            const char* needles[] = {
                "libffkit.so","libva++.so","virtualapp","com.ffkit",
                "/data/data/com.ffkit","lody","stub_","ffkit", nullptr
            };
            for (int i = 0; needles[i]; i++) {
                size_t pos = 0;
                while ((pos = s.find(needles[i], pos)) != std::string::npos) {
                    size_t ln = s.rfind('\n', pos);
                    if (ln == std::string::npos) ln = 0; else ln++;
                    size_t le = s.find('\n', pos);
                    if (le == std::string::npos) le = s.size();
                    s.erase(ln, le - ln + 1);
                    pos = ln;
                }
            }
            write(memfd, s.data(), s.size());
        }
        close(src);
        lseek(memfd, 0, SEEK_SET);
        return memfd;
    }
    return real_open(path, flags);
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
    static const char* clean[] = {
        "ro.debuggable","ro.secure","service.adb.root",
        "ro.build.selinux","init.svc.magisk", nullptr
    };
    for (int i = 0; clean[i]; i++) if (!strcmp(key, clean[i])) {
        if (!strcmp(key, "ro.secure"))    { strcpy(value, "1"); return 1; }
        if (!strcmp(key, "ro.debuggable")){ strcpy(value, "0"); return 1; }
        value[0] = 0; return 0;
    }
    return real_getprop(key, value);
}

static struct dirent* fake_readdir(DIR* d) {
    while (true) {
        struct dirent* e = real_readdir(d);
        if (!e) return nullptr;
        if (e->d_name[0] >= '0' && e->d_name[0] <= '9') {
            char p[64]; snprintf(p, sizeof(p), "/proc/%s/cmdline", e->d_name);
            int fd = real_open(p, O_RDONLY);
            if (fd >= 0) {
                char cm[256] = {0};
                read(fd, cm, sizeof(cm) - 1);
                close(fd);
                if (strstr(cm, "com.ffkit") || strstr(cm, "VirtualApp")) continue;
            }
        }
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
    return real_getenv(k);
}

extern "C" void mask_install() {
    void* libc = dlopen("libc.so", RTLD_NOW);
    hook_abs(dlsym(libc, "open"),     (void*)fake_open,     (void**)&real_open);
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
