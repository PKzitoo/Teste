package com.ffkit.lite

object StateBridge {
    var aimEnabled    = false; set(v) { field = v; setB("aim_enabled", v) }
    var aimOnScope    = true;  set(v) { field = v; setB("aim_on_scope", v) }
    var aimOnFire     = true;  set(v) { field = v; setB("aim_on_fire", v) }
    var aimIgnoreBots = true;  set(v) { field = v; setB("aim_ignore_bots", v) }
    var aimFov        = 80f;   set(v) { field = v; setF("aim_fov", v) }
    var aimSmooth     = 5f;    set(v) { field = v; setF("aim_smooth", v) }
    var aimBone       = 0;     set(v) { field = v; setI("aim_bone", v) }
    var noRecoil      = true;  set(v) { field = v; setB("no_recoil", v) }
    var speedHack     = false; set(v) { field = v; setB("speed_hack", v) }
    var flagSecure    = true;  set(v) { field = v; setB("flag_secure", v) }

    fun panic() = NativeBridge.panic()

    @JvmStatic external fun setB(k: String, v: Boolean)
    @JvmStatic external fun setF(k: String, v: Float)
    @JvmStatic external fun setI(k: String, v: Int)
}
