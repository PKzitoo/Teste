package com.ffkit.lite

import android.content.Context

object StateBridge {
    var appCtx: Context? = null  // setado por MainActivity/HostApp

    private fun flush() { appCtx?.let { StateManager.save(it) } }

    var aimEnabled    = false; set(v) { field = v; setB("aim_enabled", v);     flush() }
    var aimOnScope    = true;  set(v) { field = v; setB("aim_on_scope", v);    flush() }
    var aimOnFire     = true;  set(v) { field = v; setB("aim_on_fire", v);     flush() }
    var aimIgnoreBots = true;  set(v) { field = v; setB("aim_ignore_bots", v); flush() }
    var aimFov        = 80f;   set(v) { field = v; setF("aim_fov", v);         flush() }
    var aimSmooth     = 5f;    set(v) { field = v; setF("aim_smooth", v);      flush() }
    var aimBone       = 0;     set(v) { field = v; setI("aim_bone", v);        flush() }

    var aimMissRate   = 0.30f; set(v) { field = v; setF("aim_miss_rate", v);   flush() }
    var aimBoneRandom = true;  set(v) { field = v; setB("aim_bone_random", v); flush() }
    var aimReactionMs = 200;   set(v) { field = v; setI("aim_reaction_ms", v); flush() }
    var aimKpmCap     = 8;     set(v) { field = v; setI("aim_kpm_cap", v);     flush() }
    var aimMaxHsStreak= 3;     set(v) { field = v; setI("aim_max_hs_streak", v); flush() }

    var noRecoil      = true;  set(v) { field = v; setB("no_recoil", v);       flush() }
    var speedHack     = false; set(v) { field = v; setB("speed_hack", v);      flush() }
    var flagSecure    = true;  set(v) { field = v; setB("flag_secure", v);     flush() }

    fun panic() = NativeBridge.panic()

    @JvmStatic external fun setB(k: String, v: Boolean)
    @JvmStatic external fun setF(k: String, v: Float)
    @JvmStatic external fun setI(k: String, v: Int)
}
