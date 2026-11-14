#include <jni.h>
#include <vector>
#include <mutex>
#include <cstring>
#include "state.hpp"
#include "math.hpp"
#include "offsets.h"

struct Enemy {
    Vec3 pos;
    Vec3 head;
    Vec2 head2d;
    float dist;
    int hp;
    int team;
    bool bot;
    char name[32];
    Vec2 bones2d[11];
    bool bonesValid;
};

std::vector<Enemy> g_enemies;
std::mutex g_mu;

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_ffkit_lite_NativeBridge_pullEnemies(JNIEnv* env, jclass) {
    std::lock_guard<std::mutex> g(g_mu);
    jclass fac = env->FindClass("[F");
    jobjectArray arr = env->NewObjectArray(g_enemies.size(), fac, nullptr);
    for (size_t i = 0; i < g_enemies.size(); i++) {
        auto& e = g_enemies[i];
        jfloatArray row = env->NewFloatArray(6 + 22);
        float buf[6 + 22];
        buf[0] = e.head2d.x; buf[1] = e.head2d.y;
        buf[2] = e.dist;     buf[3] = (float)e.hp;
        buf[4] = (float)e.team; buf[5] = e.bot ? 1.f : 0.f;
        for (int b = 0; b < 11; b++) {
            buf[6 + b*2]     = e.bones2d[b].x;
            buf[6 + b*2 + 1] = e.bones2d[b].y;
        }
        env->SetFloatArrayRegion(row, 0, 6 + 22, buf);
        env->SetObjectArrayElement(arr, i, row);
        env->DeleteLocalRef(row);
    }
    return arr;
}
