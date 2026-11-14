#include <jni.h>
#include <thread>
#include <chrono>
#include <vector>
#include <mutex>
#include <dlfcn.h>
#include <string>
#include <cstdio>
#include "state.hpp"
#include "math.hpp"
#include "memory.hpp"
#include "il2cpp_glue.hpp"
#include "offsets.h"

struct Enemy {
    Vec3 pos; Vec3 head; Vec2 head2d; float dist;
    int hp; int team; bool bot; char name[32];
    Vec2 bones2d[11]; bool bonesValid;
};
extern std::vector<Enemy> g_enemies;
extern std::mutex g_mu;

static std::string g_dataDir;

void* getLocalPlayer() { return nullptr; }
void* getLocalWeapon() { return nullptr; }

static void dumpOffsetsOnce() {
    void* h = dlopen("libil2cpp.so", RTLD_NOLOAD);
    if (!h) return;

    using FnDomainGet      = void*(*)();
    using FnDomainAsm      = void**(*)(void*, size_t*);
    using FnClassFromName  = void*(*)(void*, const char*, const char*);
    using FnMethodFromName = void*(*)(void*, const char*, int);
    using FnFieldFromName  = void*(*)(void*, const char*);

    auto domainGet  = (FnDomainGet)     dlsym(h, "il2cpp_domain_get");
    auto domainAsm  = (FnDomainAsm)     dlsym(h, "il2cpp_domain_get_assemblies");
    auto classFrom  = (FnClassFromName) dlsym(h, "il2cpp_class_from_name");
    auto methodFrom = (FnMethodFromName)dlsym(h, "il2cpp_class_get_method_from_name");
    auto fieldFrom  = (FnFieldFromName) dlsym(h, "il2cpp_class_get_field_from_name");
    if (!domainGet || !domainAsm || !classFrom) return;

    uintptr_t base = (uintptr_t)h;
    size_t count = 0;
    void** asms = domainAsm(domainGet(), &count);
    void *unityImg = nullptr, *gameImg = nullptr;
    for (size_t i = 0; i < count; i++) {
        void* img = *(void**)asms[i];
        const char* name = *(const char**)((char*)img + 0x18);
        if (!name) continue;
        std::string s = name;
        if (!unityImg && s.find("UnityEngine") != std::string::npos) unityImg = img;
        if (!gameImg  && (s == "Assembly-CSharp" ||
                          s.find("GameAssembly") != std::string::npos)) gameImg = img;
    }
    if (!unityImg || !gameImg) return;

    auto rel = [&](void* p) -> uintptr_t { return p ? ((uintptr_t)p - base) : 0; };
    auto methodPtr = [&](void* m) -> uintptr_t { return m ? rel(*(void**)m) : 0; };

    void* Camera    = classFrom(unityImg, "UnityEngine", "Camera");
    void* Transform = classFrom(unityImg, "UnityEngine", "Transform");
    void* Object_   = classFrom(unityImg, "UnityEngine", "Object");

    if (Camera) {
        off::Camera_get_main           = methodPtr(methodFrom(Camera, "get_main", 0));
        off::Camera_WorldToScreenPoint = methodPtr(methodFrom(Camera, "WorldToScreenPoint", 1));
    }
    if (Transform) {
        off::Transform_get_position = methodPtr(methodFrom(Transform, "get_position", 0));
        off::Transform_get_forward  = methodPtr(methodFrom(Transform, "get_forward", 0));
    }
    if (Object_) {
        off::Object_FindObjectsOfType = methodPtr(methodFrom(Object_, "FindObjectsOfType", 1));
    }

    const char* pcNames[] = {"PlayerController","FFPlayerController",
                             "HumanController","BrPlayerController",
                             "CharacterController"};
    void* PC = nullptr;
    for (auto n : pcNames) { PC = classFrom(gameImg, "", n); if (PC) break; }
    off::PlayerController_TypeInfo = PC;

    auto field = [&](void* k, const char* n) -> uintptr_t {
        void* f = fieldFrom(k, n);
        return f ? *(uint32_t*)((char*)f + 2*sizeof(void*)) : 0;
    };
    if (PC) {
        off::PlayerController_Health = field(PC, "health"); if (!off::PlayerController_Health) off::PlayerController_Health = field(PC, "hp");
        off::PlayerController_TeamId = field(PC, "teamId"); if (!off::PlayerController_TeamId) off::PlayerController_TeamId = field(PC, "team");
        off::PlayerController_IsBot  = field(PC, "isBot");  if (!off::PlayerController_IsBot)  off::PlayerController_IsBot  = field(PC, "bot");
        off::PlayerController_Head   = field(PC, "headBone");if (!off::PlayerController_Head) off::PlayerController_Head  = field(PC, "head");
    }

    void* WC = classFrom(gameImg, "", "WeaponController"); if (!WC) WC = classFrom(gameImg, "", "FFWeapon");
    if (WC) {
        off::WeaponController_Recoil   = field(WC, "recoil");   if (!off::WeaponController_Recoil)   off::WeaponController_Recoil   = field(WC, "recoilAmount");
        off::WeaponController_FireRate = field(WC, "fireRate"); if (!off::WeaponController_FireRate) off::WeaponController_FireRate = field(WC, "rpm");
    }

    void* MV = classFrom(gameImg, "", "CharacterMovement"); if (!MV) MV = classFrom(gameImg, "", "PlayerMovement");
    if (MV) {
        off::Movement_Speed   = field(MV, "speed");   if (!off::Movement_Speed)   off::Movement_Speed   = field(MV, "moveSpeed");
        off::Movement_Gravity = field(MV, "gravity"); if (!off::Movement_Gravity) off::Movement_Gravity = field(MV, "gravityScale");
    }

    FILE* fp = fopen((g_dataDir + "/offsets.json").c_str(), "w");
    if (fp) {
        fprintf(fp, "{\"base\":%zu,\"Cmain\":%zu,\"W2S\":%zu,\"TPos\":%zu,"
                    "\"Find\":%zu,\"HP\":%zu,\"Team\":%zu,"
                    "\"Bot\":%zu,\"Head\":%zu}\n",
                (size_t)base,
                (size_t)off::Camera_get_main,
                (size_t)off::Camera_WorldToScreenPoint,
                (size_t)off::Transform_get_position,
                (size_t)off::Object_FindObjectsOfType,
                (size_t)off::PlayerController_Health,
                (size_t)off::PlayerController_TeamId,
                (size_t)off::PlayerController_IsBot,
                (size_t)off::PlayerController_Head);
        fclose(fp);
    }
}

static void scanPlayers() {
    if (!off::Object_FindObjectsOfType || !off::PlayerController_TypeInfo) return;

    using FindFn = void*(*)(void*);
    auto find = g_il.resolve<FindFn>(off::Object_FindObjectsOfType);
    void* list = find(off::PlayerController_TypeInfo);
    if (!list) return;

    int32_t len = readT<int32_t>((uintptr_t)list + 0x18);
    uintptr_t items = (uintptr_t)list + 0x20;

    using CamFn = void*(*)();
    void* cam = g_il.resolve<CamFn>(off::Camera_get_main)();
    if (!cam) return;

    std::vector<Enemy> out; out.reserve(len);
    for (int i = 0; i < len; i++) {
        void* p = (void*)readT<uintptr_t>(items + i * sizeof(void*));
        if (!p) continue;
        int hp   = readT<int>((uintptr_t)p + off::PlayerController_Health);
        if (hp <= 0) continue;
        int team = readT<int>((uintptr_t)p + off::PlayerController_TeamId);
        bool bot = readT<bool>((uintptr_t)p + off::PlayerController_IsBot);
        void* head = (void*)readT<uintptr_t>((uintptr_t)p + off::PlayerController_Head);
        if (!head) continue;

        using PosFn = Vec3(*)(void*);
        auto getPos = g_il.resolve<PosFn>(off::Transform_get_position);
        Vec3 hpos = getPos(head);

        Vec2 s; float d;
        if (!W2S(cam, hpos, s, d)) continue;
        if (d > g_state.esp_max_dist) continue;

        Enemy e{};
        e.pos = hpos; e.head = hpos; e.head2d = s; e.bones2d[0] = s;
        e.dist = d; e.hp = hp; e.team = team; e.bot = bot;
        e.bonesValid = true;
        out.push_back(e);
    }
    std::lock_guard<std::mutex> g(g_mu);
    g_enemies = std::move(out);
}

extern void tick_features();
extern "C" void injectCameraDelta(float, float);

static std::chrono::steady_clock::time_point g_last_switch;
static void* g_prev_target = nullptr;
static int g_shot_cycle = 0;   // posicao no ciclo head-body
static bool g_prev_firing = false;

/**
 * SHOT PATTERN MODE
 * ─────────────────
 * O usuario configura dois numeros:
 *   shot_head_count  = N tiros na cabeca
 *   shot_body_count  = M tiros no peito
 *
 * A cada tiro, incrementa g_shot_cycle.
 *   indice 0..N-1   -> cabeca (bone=0)
 *   indice N..N+M-1 -> peito  (bone=2)
 *   em N+M, volta pra 0
 *
 * Isso substitui o random miss rate — eh deterministico e parece
 * "estilo de jogo deliberado" nas replays do Garena.
 */
static void aimTick(float cx, float cy, bool scoping, bool firing) {
    if (!g_state.aim_enabled) return;
    if (g_state.aim_on_scope && !scoping) return;
    if (g_state.aim_on_fire  && !firing)  return;

    std::lock_guard<std::mutex> g(g_mu);
    Enemy* best = nullptr; float bestD = g_state.aim_fov.load();
    for (auto& e : g_enemies) {
        if (g_state.aim_ignore_bots && e.bot) continue;
        if (e.hp <= 0) continue;
        float dx = e.head2d.x - cx, dy = e.head2d.y - cy;
        float d = std::sqrt(dx*dx + dy*dy);
        if (d < bestD) { bestD = d; best = &e; }
    }
    if (!best) return;

    // atraso de reacao: ao trocar de alvo, aguarda aim_reaction_ms
    auto now = std::chrono::steady_clock::now();
    if (best != g_prev_target) {
        g_last_switch = now;
        g_prev_target = best;
        return;
    }
    auto since = std::chrono::duration_cast<std::chrono::milliseconds>
                 (now - g_last_switch).count();
    if (since < g_state.aim_reaction_ms.load()) return;

    // conta como "novo tiro" apenas na borda (firing passou de false -> true)
    // isso impede o ciclo de avancar a cada tick do loop
    if (firing && !g_prev_firing) {
        g_shot_cycle++;
    }
    g_prev_firing = firing;

    int H = std::max(0, g_state.shot_head_count.load());
    int B = std::max(0, g_state.shot_body_count.load());
    int total = H + B;
    if (total <= 0) { H = 1; total = 1; }  // sempre cabeca se zerado
    int pos = g_shot_cycle % total;
    int bone = (pos < H) ? 0 : 2;   // 0=cabeca, 2=peito

    Vec2 tgt = best->bones2d[bone < 11 ? bone : 0];

    float sm = std::max(1.f, g_state.aim_smooth.load());
    injectCameraDelta((tgt.x - cx) / sm, (tgt.y - cy) / sm);
}

static void workerLoop() {
    while (!g_state.panic) {
        scanPlayers();
        tick_features();
        std::this_thread::sleep_for(std::chrono::milliseconds(8));
    }
}

extern "C" void start_worker()   { std::thread(workerLoop).detach(); }
extern "C" void set_data_dir(const char* p) { g_dataDir = p ? p : ""; }
extern "C" void run_dumper()     { dumpOffsetsOnce(); }
