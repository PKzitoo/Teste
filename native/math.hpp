#pragma once
#include <cmath>
#include "offsets.h"

struct Vec2 { float x, y; };
struct Vec3 {
    float x, y, z;
    Vec3 operator-(const Vec3& o) const { return {x-o.x, y-o.y, z-o.z}; }
    float dot(const Vec3& o) const { return x*o.x + y*o.y + z*o.z; }
    float len() const { return std::sqrt(x*x + y*y + z*z); }
};

// Quaternion pra rotacao de camera (x,y,z,w)
struct Quat {
    float x, y, z, w;
};

// quat yaw (rotacao no eixo Y vertical)
inline Quat quatYaw(float rad) {
    float h = rad * 0.5f;
    return { 0.f, std::sin(h), 0.f, std::cos(h) };
}

// quat pitch (rotacao no eixo X horizontal)
inline Quat quatPitch(float rad) {
    float h = rad * 0.5f;
    return { std::sin(h), 0.f, 0.f, std::cos(h) };
}

inline Quat quatMul(const Quat& a, const Quat& b) {
    return {
        a.w*b.x + a.x*b.w + a.y*b.z - a.z*b.y,
        a.w*b.y - a.x*b.z + a.y*b.w + a.z*b.x,
        a.w*b.z + a.x*b.y - a.y*b.x + a.z*b.w,
        a.w*b.w - a.x*b.x - a.y*b.y - a.z*b.z
    };
}

inline bool W2S(void* cam, const Vec3& world, Vec2& out, float& dist) {
    using Fn = Vec3(*)(void*, Vec3);
    if (!off::Camera_WorldToScreenPoint) return false;
    auto fn = (Fn)off::Camera_WorldToScreenPoint;
    Vec3 s = fn(cam, world);
    if (s.z <= 0.1f) return false;
    out  = { s.x, s.y };
    dist = s.z;
    return true;
}
