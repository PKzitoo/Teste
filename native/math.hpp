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
