#include <jni.h>
#include <thread>
#include <chrono>
#include <vector>
#include <mutex>
#include <atomic>
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

// ponteiros do jogador local — atualizados a cada scanPlayers
static std::atomic<void*> g_local_player{nullptr};
static std::atomic<void*> g_local_weapon{nullptr};

void* getLocalPlayer() { return g_local_player.load(); }
void* getLocalWeapon() { return g_local_weapon.load(); }

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
        off::Camera_set_fieldOfView    = methodPtr(methodFrom(Camera, "set_fieldOfView", 1));
        off::Camera_get_transform      = methodPtr(methodFrom(Camera, "get_transform", 0));
    }
    if (Transform) {
        off::Transform_get_position = methodPtr(methodFrom(Transform, "get_position", 0));
        off::Transform_get_forward  = methodPtr(methodFrom(Transform, "get_forward", 0));
        off::Transform_set_rotation = methodPtr(methodFrom(Transform, "set_rotation", 1));
        off::Transform_get_rotation = methodPtr(methodFrom(Transform, "get_rotation", 0));
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
    auto field_any = [&](void* k, std::initializer_list<const char*> names) -> uintptr_t {
        for (auto n : names) { auto v = field(k, n); if (v) return v; }
        return 0;
    };
    if (PC) {
        off::PlayerController_Health = field_any(PC, {"health", "hp", "Hp", "HP"});
        off::PlayerController_TeamId = field_any(PC, {"teamId", "team", "TeamId"});
        off::PlayerController_IsBot  = field_any(PC, {"isBot", "bot", "IsBot"});
        off::PlayerController_IsLocal= field_any(PC, {"isLocal", "isLocalPlayer",
                                                       "isMine", "isOwner",
                                                       "IsLocalPlayer"});
        off::PlayerController_Head   = field_any(PC, {"headBone", "head", "Head",
                                                       "headTransform"});
        off::PlayerController_Neck   = field_any(PC, {"neckBone", "neck", "Neck"});
        off::PlayerController_Chest  = field_any(PC, {"chestBone", "chest", "spine",
                                                       "Spine", "upperBody"});
        off::PlayerController_Transform = field_any(PC, {"transform", "_transform"});
        off::PlayerController_Weapon = field_any(PC, {"currentWeapon", "weapon",
                                                       "equippedWeapon", "activeWeapon"});
    }

    void* WC = classFrom(gameImg, "", "WeaponController"); if (!WC) WC = classFrom(gameImg, "", "FFWeapon");
    if (WC) {
        off::WeaponController_Recoil   = field_any(WC, {"recoil", "recoilAmount",
                                                         "verticalRecoil"});
        off::WeaponController_FireRate = field_any(WC, {"fireRate", "rpm",
                                                         "fireInterval"});
        off::WeaponController_Spread   = field_any(WC, {"spread", "spreadRadius",
                                                         "accuracy", "bulletSpread"});
        off::WeaponController_Ammo     = field_any(WC, {"currentAmmo", "ammo",
                                                         "bulletsInMag"});
        off::WeaponController_Damage   = field_any(WC, {"damage", "baseDamage",
                                                         "bulletDamage"});
    }

    void* MV = classFrom(gameImg, "", "CharacterMovement"); if (!MV) MV = classFrom(gameImg, "", "PlayerMovement");
    if (MV) {
        off::Movement_Speed       = field_any(MV, {"speed", "moveSpeed", "walkSpeed"});
        off::Movement_Gravity     = field_any(MV, {"gravity", "gravityScale"});
        off::Movement_JumpPower   = field_any(MV, {"jumpPower", "jumpHeight",
                                                    "jumpForce"});
        off::Movement_FallDamage  = field_any(MV, {"fallDamageMult", "fallDamage",
                                                    "fallDmgScale"});
    }

    FILE* fp = fopen((g_dataDir + "/offsets.json").c_str(), "w");
    if (fp) {
        fprintf(fp, "{\"base\":%zu,\"Cmain\":%zu,\"W2S\":%zu,\"TPos\":%zu,"
                    "\"Find\":%zu,\"HP\":%zu,\"Team\":%zu,"
                    "\"Bot\":%zu,\"Local\":%zu,\"Head\":%zu,"
                    "\"Chest\":%zu,\"Transform\":%zu,\"Weapon\":%zu,"
                    "\"Recoil\":%zu,\"FireRate\":%zu,\"Speed\":%zu}\n",
                (size_t)base,
                (size_t)off::Camera_get_main,
                (size_t)off::Camera_WorldToScreenPoint,
                (size_t)off::Transform_get_position,
                (size_t)off::Object_FindObjectsOfType,
                (size_t)off::PlayerController_Health,
                (size_t)off::PlayerController_TeamId,
                (size_t)off::PlayerController_IsBot,
                (size_t)off::PlayerController_IsLocal,
                (size_t)off::PlayerController_Head,
                (size_t)off::PlayerController_Chest,
                (size_t)off::PlayerController_Transform,
                (size_t)off::PlayerController_Weapon,
                (size_t)off::WeaponController_Recoil,
                (size_t)off::WeaponController_FireRate,
                (size_t)off::Movement_Speed);
        fclose(fp);
    }
}

static void scanPlayers() {
    if (!off::Object_FindObjectsOfType || !off::PlayerController_TypeInfo) return;

    using FindFn = void*(*)(void*);
    auto find = g_il.resolve<FindFn>(off::Object_FindObjectsOfType);
    if (!find) return;
    void* list = find(off::PlayerController_TypeInfo);
    if (!list) return;

    int32_t len = readT<int32_t>((uintptr_t)list + 0x18);
    uintptr_t items = (uintptr_t)list + 0x20;

    using CamFn = void*(*)();
    auto getCam = g_il.resolve<CamFn>(off::Camera_get_main);
    if (!getCam) return;
    void* cam = getCam();
    if (!cam) return;

    using PosFn = Vec3(*)(void*);
    auto getPos = g_il.resolve<PosFn>(off::Transform_get_position);
    if (!getPos) return;

    std::vector<Enemy> out; out.reserve(len);
    void* local_pc = nullptr;
    void* local_wp = nullptr;

    for (int i = 0; i < len; i++) {
        void* p = (void*)readT<uintptr_t>(items + i * sizeof(void*));
        if (!p) continue;

        // detecta jogador local ANTES de pular por HP (local tbm aparece)
        if (off::PlayerController_IsLocal) {
            if (readT<bool>((uintptr_t)p + off::PlayerController_IsLocal)) {
                local_pc = p;
                if (off::PlayerController_Weapon) {
                    local_wp = (void*)readT<uintptr_t>(
                        (uintptr_t)p + off::PlayerController_Weapon);
                }
                continue;  // local nao entra em g_enemies
            }
        }

        int hp = off::PlayerController_Health
                    ? readT<int>((uintptr_t)p + off::PlayerController_Health) : 100;
        if (hp <= 0) continue;

        int team = off::PlayerController_TeamId
                    ? readT<int>((uintptr_t)p + off::PlayerController_TeamId) : 0;
        bool bot = off::PlayerController_IsBot
                    ? readT<bool>((uintptr_t)p + off::PlayerController_IsBot) : false;

        void* head = (void*)readT<uintptr_t>(
            (uintptr_t)p + off::PlayerController_Head);
        if (!head) continue;

        Vec3 hpos = getPos(head);
        Vec2 s; float d;
        if (!W2S(cam, hpos, s, d)) continue;
        if (d > g_state.esp_max_dist) continue;

        Enemy e{};
        e.pos = hpos; e.head = hpos; e.head2d = s;
        e.dist = d; e.hp = hp; e.team = team; e.bot = bot;

        // preenche 11 bones:
        //  0 = cabeca (head)
        //  1 = pescoco (neck, ou interp head+chest)
        //  2 = peito (chest)
        //  3..10 = resto (membros, pra skeleton futuro)
        e.bones2d[0] = s;

        // CHEST: ponteiro direto se dumper achou, senao offset mundial
        void* chest = off::PlayerController_Chest
                        ? (void*)readT<uintptr_t>(
                            (uintptr_t)p + off::PlayerController_Chest)
                        : nullptr;
        if (chest) {
            Vec3 cpos = getPos(chest);
            Vec2 cs; float cd;
            if (W2S(cam, cpos, cs, cd)) e.bones2d[2] = cs;
            else                        e.bones2d[2] = s;
        } else {
            // fallback: 1 unidade do mundo abaixo da cabeca = peito aproximado
            Vec3 cpos = hpos;
            cpos.y -= 1.0f;
            Vec2 cs; float cd;
            if (W2S(cam, cpos, cs, cd)) e.bones2d[2] = cs;
            else                        e.bones2d[2] = s;
        }

        // NECK: interpola entre head e chest
        e.bones2d[1] = Vec2{
            (e.bones2d[0].x + e.bones2d[2].x) * 0.5f,
            (e.bones2d[0].y * 0.7f + e.bones2d[2].y * 0.3f)
        };

        // outros bones = chest por padrao (evita (0,0) se aimbot pedir)
        for (int b = 3; b < 11; b++) e.bones2d[b] = e.bones2d[2];

        e.bonesValid = true;
        out.push_back(e);
    }

    g_local_player.store(local_pc);
    if (local_wp) g_local_weapon.store(local_wp);

    std::lock_guard<std::mutex> g(g_mu);
    g_enemies = std::move(out);
}

extern void tick_features();
extern "C" void injectCameraDelta(float, float);

static std::chrono::steady_clock::time_point g_last_switch;
static void* g_prev_target = nullptr;
static int g_shot_cycle = 0;
static bool g_prev_firing = false;

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

    auto now = std::chrono::steady_clock::now();
    if (best != g_prev_target) {
        g_last_switch = now;
        g_prev_target = best;
        return;
    }
    auto since = std::chrono::duration_cast<std::chrono::milliseconds>
                 (now - g_last_switch).count();
    if (since < g_state.aim_reaction_ms.load()) return;

    if (firing && !g_prev_firing) g_shot_cycle++;
    g_prev_firing = firing;

    int H = std::max(0, g_state.shot_head_count.load());
    int B = std::max(0, g_state.shot_body_count.load());
    int total = H + B;
    if (total <= 0) { H = 1; total = 1; }
    int pos = g_shot_cycle % total;
    int bone = (pos < H) ? 0 : 2;

    Vec2 tgt = best->bones2d[bone < 11 ? bone : 0];

    float sm = std::max(1.f, g_state.aim_smooth.load());
    injectCameraDelta((tgt.x - cx) / sm, (tgt.y - cy) / sm);
}

static void workerLoop() {
    while (!g_state.panic) {
        scanPlayers();
        tick_features();
        // aimTick precisa saber se usuario esta atirando/scopando;
        // essa deteccao fica em um hook Java (TODO) — por enquanto
        // aim desativado por default (aim_enabled=false ate user ligar)
        std::this_thread::sleep_for(std::chrono::milliseconds(8));
    }
}

extern "C" void start_worker()   { std::thread(workerLoop).detach(); }
extern "C" void set_data_dir(const char* p) { g_dataDir = p ? p : ""; }
extern "C" void run_dumper()     { dumpOffsetsOnce(); }
