package com.ffkit.lite

import android.content.Context

object StateBridge {
    var appCtx: Context? = null

    private fun flush() { appCtx?.let { StateManager.save(it) } }

    // ===== AIM =====
    var aimEnabled    = false; set(v) { field = v; setB("aim_enabled", v);     flush() }
    var aimOnScope    = true;  set(v) { field = v; setB("aim_on_scope", v);    flush() }
    var aimOnFire     = true;  set(v) { field = v; setB("aim_on_fire", v);     flush() }
    var aimIgnoreBots = true;  set(v) { field = v; setB("aim_ignore_bots", v); flush() }
    var aimFov        = 80f;   set(v) { field = v; setF("aim_fov", v);         flush() }
    var aimSmooth     = 5f;    set(v) { field = v; setF("aim_smooth", v);      flush() }
    var aimReactionMs = 200;   set(v) { field = v; setI("aim_reaction_ms", v); flush() }
    var shotHeadCount = 3;     set(v) { field = v; setI("shot_head_count", v); flush() }
    var shotBodyCount = 1;     set(v) { field = v; setI("shot_body_count", v); flush() }
    var aimPrediction = true;  set(v) { field = v; setB("aim_prediction", v);  flush() }
    var aimMultiTarget= true;  set(v) { field = v; setB("aim_multi_target", v); flush() }
    var aimSilent     = false; set(v) { field = v; setB("aim_silent", v);      flush() }   // mira sem camera mover

    // ===== ESP =====
    // mode: 0=OFF, 1=LEGIT (80m, delayed, no scope), 2=FULL (wall), 3=AUDIO
    var espMode       = 0;     set(v) { field = v; setI("esp_mode", v);        flush() }
    var espBox        = true;  set(v) { field = v; setB("esp_box", v);         flush() }
    var espSkeleton   = false; set(v) { field = v; setB("esp_skeleton", v);    flush() }
    var espHealth     = true;  set(v) { field = v; setB("esp_health", v);      flush() }
    var espDistance   = true;  set(v) { field = v; setB("esp_distance", v);    flush() }
    var espName       = false; set(v) { field = v; setB("esp_name", v);        flush() }
    var espMaxDist    = 200f;  set(v) { field = v; setF("esp_max_dist", v);    flush() }
    var espDelayMs    = 0;     set(v) { field = v; setI("esp_delay_ms", v);    flush() }  // 0 full, >0 legit
    var espHideScope  = true;  set(v) { field = v; setB("esp_hide_scope", v);  flush() }
    var espAudioThr   = 50f;   set(v) { field = v; setF("esp_audio_thr", v);   flush() }  // metros pra beep

    // ===== WEAPON (patches memoria) =====
    var noRecoil      = true;  set(v) { field = v; setB("no_recoil", v);       flush() }
    var noSpread      = false; set(v) { field = v; setB("no_spread", v);       flush() }
    var fastFire      = false; set(v) { field = v; setB("fast_fire", v);       flush() }
    var noReload      = false; set(v) { field = v; setB("no_reload", v);       flush() }
    var infiniteAmmo  = false; set(v) { field = v; setB("infinite_ammo", v);   flush() }
    var oneShotHs     = false; set(v) { field = v; setB("one_shot_hs", v);     flush() }
    var anyGunAwm     = false; set(v) { field = v; setB("any_gun_awm", v);     flush() }
    var pierceWalls   = false; set(v) { field = v; setB("pierce_walls", v);    flush() }
    var magicBullet   = false; set(v) { field = v; setB("magic_bullet", v);    flush() }  // bala curva pro alvo

    // ===== MOVEMENT =====
    var speedHack     = false; set(v) { field = v; setB("speed_hack", v);      flush() }
    var speedMult     = 1.3f;  set(v) { field = v; setF("speed_mult", v);      flush() }
    var flyMode       = false; set(v) { field = v; setB("fly_mode", v);        flush() }
    var noFallDmg     = true;  set(v) { field = v; setB("no_fall_dmg", v);     flush() }
    var superJump     = false; set(v) { field = v; setB("super_jump", v);      flush() }
    var bhop          = false; set(v) { field = v; setB("bhop", v);            flush() }
    var waterBreath   = true;  set(v) { field = v; setB("water_breath", v);    flush() }
    var instantRevive = false; set(v) { field = v; setB("instant_revive", v);  flush() }
    var noCollision   = false; set(v) { field = v; setB("no_collision", v);    flush() }  // noclip
    var wideView      = false; set(v) { field = v; setB("wide_view", v);       flush() }

    // ===== INTEL =====
    var grenadeAlert  = true;  set(v) { field = v; setB("grenade_alert", v);   flush() }
    var lootEsp       = false; set(v) { field = v; setB("loot_esp", v);        flush() }
    var zoneTimer     = true;  set(v) { field = v; setB("zone_timer", v);      flush() }
    var vehicleEsp    = false; set(v) { field = v; setB("vehicle_esp", v);     flush() }
    var miniMapEnemy  = false; set(v) { field = v; setB("minimap_enemy", v);   flush() }
    var airdropTracker= false; set(v) { field = v; setB("airdrop_tracker", v); flush() }
    var killFeedSpy   = false; set(v) { field = v; setB("killfeed_spy", v);    flush() }  // ve kills fora da tela

    // ===== SAFETY (sempre on, protege o user) =====
    var spectatorWarn = true;  set(v) { field = v; setB("spectator_warn", v);  flush() }
    var streamerMode  = false; set(v) { field = v; setB("streamer_mode", v);   flush() }  // esconde em OBS/screen record
    var autoOffKills  = 0;     set(v) { field = v; setI("auto_off_kills", v);  flush() }  // 0=desligado, >0 desliga aim apos N kills
    var hsCapDynamic  = true;  set(v) { field = v; setB("hs_cap_dynamic", v);  flush() }
    var replaySanit   = true;  set(v) { field = v; setB("replay_sanit", v);    flush() }  // limpa dados suspeitos no buffer de replay
    var signatureRot  = false; set(v) { field = v; setB("signature_rot", v);   flush() }  // roda assinatura APK periodicamente

    // ===== SYS =====
    var flagSecure    = true;  set(v) { field = v; setB("flag_secure", v);     flush() }

    fun panic() { try { NativeBridge.panic() } catch (_: Throwable) {} }

    @JvmStatic fun setB(k: String, v: Boolean) { try { setBNative(k, v) } catch (_: Throwable) {} }
    @JvmStatic fun setF(k: String, v: Float)   { try { setFNative(k, v) } catch (_: Throwable) {} }
    @JvmStatic fun setI(k: String, v: Int)     { try { setINative(k, v) } catch (_: Throwable) {} }

    @JvmStatic external fun setBNative(k: String, v: Boolean)
    @JvmStatic external fun setFNative(k: String, v: Float)
    @JvmStatic external fun setINative(k: String, v: Int)
}
