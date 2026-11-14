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

    // ESP
    var espEnabled    = true;  set(v) { field = v; setB("esp_enabled", v);     flush() }
    var espBox        = true;  set(v) { field = v; setB("esp_box", v);         flush() }
    var espSkeleton   = true;  set(v) { field = v; setB("esp_skeleton", v);    flush() }
    var espHealth     = true;  set(v) { field = v; setB("esp_health", v);      flush() }
    var espDistance   = true;  set(v) { field = v; setB("esp_distance", v);    flush() }
    var espName       = false; set(v) { field = v; setB("esp_name", v);        flush() }
    var espMaxDist    = 200f;  set(v) { field = v; setF("esp_max_dist", v);    flush() }

    // MISC
    var noRecoil      = true;  set(v) { field = v; setB("no_recoil", v);       flush() }
    var speedHack     = false; set(v) { field = v; setB("speed_hack", v);      flush() }
    var speedMult     = 1.3f;  set(v) { field = v; setF("speed_mult", v);      flush() }
    var flyMode       = false; set(v) { field = v; setB("fly_mode", v);        flush() }
    var fastFire      = false; set(v) { field = v; setB("fast_fire", v);       flush() }
    var wideView      = false; set(v) { field = v; setB("wide_view", v);       flush() }

    // SYS
    var flagSecure    = true;  set(v) { field = v; setB("flag_secure", v);     flush() }

    // ===== RECOIL (no-root) =====
    // selecao e intensidade do padrao de recuo
    var weaponIdx       = 0;      set(v) { field = v; flush() }   // arma ATIVA
    var weaponPrimary   = 0;      set(v) { field = v; flush() }   // slot primario (1a arma)
    var weaponSecondary = 1;      set(v) { field = v; flush() }   // slot secundario (2a arma)
    var alwaysOn        = false;  set(v) { field = v; flush() }   // sempre ligado, sem botao
    var swipeDurationMs = 40;     set(v) { field = v; flush() }   // duracao de cada swipe
    var recoilIntensity = 1.0f;   set(v) { field = v; flush() }   // 0.3..2.0 (gain global)

    // posicao do "look pad" do FF (onde a gente envia o swipe de recuo).
    // valores padrao = ~meio da metade direita de uma tela 2400x1080.
    var lookPadX        = 1800;   set(v) { field = v; flush() }
    var lookPadY        = 540;    set(v) { field = v; flush() }

    // botao flutuante ASSIST
    var triggerEnabled  = true;   set(v) { field = v; flush() }
    var triggerX        = 60;     set(v) { field = v; flush() }   // px absoluto
    var triggerY        = 900;    set(v) { field = v; flush() }
    var triggerSize     = 72;     set(v) { field = v; flush() }   // dp
    var triggerOpacity  = 0.65f;  set(v) { field = v; flush() }   // 0.2..1.0

    // macros extras
    var burstMode       = false;  set(v) { field = v; flush() }   // tiro simples -> 3x
    var autoCrouch      = false;  set(v) { field = v; flush() }
    var tapFire         = false;  set(v) { field = v; flush() }   // pistolas rapido

    fun panic() { try { NativeBridge.panic() } catch (_: Throwable) {} }

    // os setters nativos so funcionam dentro do FF (quando libffkit
    // esta carregada via LSPosed). Na Calculadora pura, a libffkit
    // NAO esta carregada — sem try/catch, qualquer slider crasha
    // com UnsatisfiedLinkError. O estado "real" vive nas properties
    // Kotlin + state.json (IPC); as chamadas JNI sao redundantes
    // locais pra quando esta dentro do FF.
    @JvmStatic fun setB(k: String, v: Boolean) { try { setBNative(k, v) } catch (_: Throwable) {} }
    @JvmStatic fun setF(k: String, v: Float)   { try { setFNative(k, v) } catch (_: Throwable) {} }
    @JvmStatic fun setI(k: String, v: Int)     { try { setINative(k, v) } catch (_: Throwable) {} }

    @JvmStatic external fun setBNative(k: String, v: Boolean)
    @JvmStatic external fun setFNative(k: String, v: Float)
    @JvmStatic external fun setINative(k: String, v: Int)
}
