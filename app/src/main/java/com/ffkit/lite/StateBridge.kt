package com.ffkit.lite

import android.content.Context

object StateBridge {
    var appCtx: Context? = null

    private fun flush() { appCtx?.let { StateManager.save(it) } }

    // ===== AIM (root/LSPosed) =====
    var aimEnabled    = false; set(v) { field = v; setB("aim_enabled", v);     flush() }
    var aimOnScope    = true;  set(v) { field = v; setB("aim_on_scope", v);    flush() }
    var aimOnFire     = true;  set(v) { field = v; setB("aim_on_fire", v);     flush() }
    var aimIgnoreBots = true;  set(v) { field = v; setB("aim_ignore_bots", v); flush() }
    var aimFov        = 80f;   set(v) { field = v; setF("aim_fov", v);         flush() }
    var aimSmooth     = 5f;    set(v) { field = v; setF("aim_smooth", v);      flush() }
    var aimReactionMs = 200;   set(v) { field = v; setI("aim_reaction_ms", v); flush() }

    // PADRAO DE TIRO (substitui miss_rate random + max_hs_streak)
    // Ex: head=3, body=1 -> "3 tapa, 1 peito, 3 tapa, 1 peito..."
    // cycle counter conta tiros e escolhe cabeca/peito pelo indice.
    var shotHeadCount = 3;     set(v) { field = v; setI("shot_head_count", v); flush() }
    var shotBodyCount = 1;     set(v) { field = v; setI("shot_body_count", v); flush() }

    // ===== ESP =====
    var espEnabled    = false; set(v) { field = v; setB("esp_enabled", v);     flush() }  // OFF por padrao
    var espBox        = true;  set(v) { field = v; setB("esp_box", v);         flush() }
    var espSkeleton   = false; set(v) { field = v; setB("esp_skeleton", v);    flush() }
    var espHealth     = true;  set(v) { field = v; setB("esp_health", v);      flush() }
    var espDistance   = true;  set(v) { field = v; setB("esp_distance", v);    flush() }
    var espName       = false; set(v) { field = v; setB("esp_name", v);        flush() }
    var espMaxDist    = 200f;  set(v) { field = v; setF("esp_max_dist", v);    flush() }

    // ===== RECOIL / MISC (root patches direto na memoria) =====
    var noRecoil      = true;  set(v) { field = v; setB("no_recoil", v);       flush() }
    var speedHack     = false; set(v) { field = v; setB("speed_hack", v);      flush() }
    var speedMult     = 1.3f;  set(v) { field = v; setF("speed_mult", v);      flush() }
    var flyMode       = false; set(v) { field = v; setB("fly_mode", v);        flush() }
    var fastFire      = false; set(v) { field = v; setB("fast_fire", v);       flush() }
    var wideView      = false; set(v) { field = v; setB("wide_view", v);       flush() }

    // ===== SYS =====
    var flagSecure    = true;  set(v) { field = v; setB("flag_secure", v);     flush() }

    fun panic() { try { NativeBridge.panic() } catch (_: Throwable) {} }

    // os setters nativos so funcionam dentro do FF (quando libffkit
    // esta carregada via LSPosed). Na Calculadora pura, a libffkit
    // NAO esta carregada — try/catch pra nao crashar UnsatisfiedLinkError.
    // O estado "real" vive nas properties Kotlin + state.json (IPC).
    @JvmStatic fun setB(k: String, v: Boolean) { try { setBNative(k, v) } catch (_: Throwable) {} }
    @JvmStatic fun setF(k: String, v: Float)   { try { setFNative(k, v) } catch (_: Throwable) {} }
    @JvmStatic fun setI(k: String, v: Int)     { try { setINative(k, v) } catch (_: Throwable) {} }

    @JvmStatic external fun setBNative(k: String, v: Boolean)
    @JvmStatic external fun setFNative(k: String, v: Float)
    @JvmStatic external fun setINative(k: String, v: Int)
}
