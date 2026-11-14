package com.ffkit.lite

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * IPC compartilhado: Calculadora (processo A) escreve state.json;
 * libffkit injetada no FF (processo B) le e aplica via JNI.
 */
object StateManager {
    private const val FILE_NAME = "state.json"
    private val publicPath = File("/sdcard/Download/ffkit_state.json")

    fun path(ctx: Context): File = File(ctx.filesDir, FILE_NAME)

    @Synchronized
    fun save(ctx: Context) {
        val s = StateBridge
        val o = JSONObject().apply {
            // AIM
            put("aim_enabled",      s.aimEnabled)
            put("aim_on_scope",     s.aimOnScope)
            put("aim_on_fire",      s.aimOnFire)
            put("aim_ignore_bots",  s.aimIgnoreBots)
            put("aim_fov",          s.aimFov.toDouble())
            put("aim_smooth",       s.aimSmooth.toDouble())
            put("aim_reaction_ms",  s.aimReactionMs)
            put("shot_head_count",  s.shotHeadCount)
            put("shot_body_count",  s.shotBodyCount)
            put("aim_prediction",   s.aimPrediction)
            put("aim_multi_target", s.aimMultiTarget)
            put("aim_silent",       s.aimSilent)
            // ESP
            put("esp_mode",         s.espMode)
            put("esp_box",          s.espBox)
            put("esp_skeleton",     s.espSkeleton)
            put("esp_health",       s.espHealth)
            put("esp_distance",     s.espDistance)
            put("esp_name",         s.espName)
            put("esp_max_dist",     s.espMaxDist.toDouble())
            put("esp_delay_ms",     s.espDelayMs)
            put("esp_hide_scope",   s.espHideScope)
            put("esp_audio_thr",    s.espAudioThr.toDouble())
            // WEAPON
            put("no_recoil",        s.noRecoil)
            put("no_spread",        s.noSpread)
            put("fast_fire",        s.fastFire)
            put("no_reload",        s.noReload)
            put("infinite_ammo",    s.infiniteAmmo)
            put("one_shot_hs",      s.oneShotHs)
            put("any_gun_awm",      s.anyGunAwm)
            put("pierce_walls",     s.pierceWalls)
            put("magic_bullet",     s.magicBullet)
            // MOVEMENT
            put("speed_hack",       s.speedHack)
            put("speed_mult",       s.speedMult.toDouble())
            put("fly_mode",         s.flyMode)
            put("no_fall_dmg",      s.noFallDmg)
            put("super_jump",       s.superJump)
            put("bhop",             s.bhop)
            put("water_breath",     s.waterBreath)
            put("instant_revive",   s.instantRevive)
            put("no_collision",     s.noCollision)
            put("wide_view",        s.wideView)
            // INTEL
            put("grenade_alert",    s.grenadeAlert)
            put("loot_esp",         s.lootEsp)
            put("zone_timer",       s.zoneTimer)
            put("vehicle_esp",      s.vehicleEsp)
            put("minimap_enemy",    s.miniMapEnemy)
            put("airdrop_tracker",  s.airdropTracker)
            put("killfeed_spy",     s.killFeedSpy)
            // SAFETY
            put("spectator_warn",   s.spectatorWarn)
            put("streamer_mode",    s.streamerMode)
            put("auto_off_kills",   s.autoOffKills)
            put("hs_cap_dynamic",   s.hsCapDynamic)
            put("replay_sanit",     s.replaySanit)
            put("signature_rot",    s.signatureRot)
            // SYS
            put("flag_secure",      s.flagSecure)
            put("ts", System.currentTimeMillis())
        }
        val text = o.toString()
        try { path(ctx).writeText(text) } catch (_: Throwable) {}
        try {
            publicPath.parentFile?.mkdirs()
            publicPath.writeText(text)
        } catch (_: Throwable) {}
    }

    @Synchronized
    fun applyFromFile(file: File) {
        if (!file.exists()) return
        val o = try { JSONObject(file.readText()) } catch (_: Throwable) { return }

        fun b(k: String) = if (o.has(k)) StateBridge.setB(k, o.getBoolean(k)) else Unit
        fun f(k: String) = if (o.has(k)) StateBridge.setF(k, o.getDouble(k).toFloat()) else Unit
        fun i(k: String) = if (o.has(k)) StateBridge.setI(k, o.getInt(k)) else Unit

        // AIM
        b("aim_enabled"); b("aim_on_scope"); b("aim_on_fire")
        b("aim_ignore_bots"); b("aim_prediction"); b("aim_multi_target"); b("aim_silent")
        f("aim_fov"); f("aim_smooth")
        i("aim_reaction_ms"); i("shot_head_count"); i("shot_body_count")
        // ESP
        b("esp_box"); b("esp_skeleton"); b("esp_health")
        b("esp_distance"); b("esp_name"); b("esp_hide_scope")
        f("esp_max_dist"); f("esp_audio_thr")
        i("esp_mode"); i("esp_delay_ms")
        // WEAPON
        b("no_recoil"); b("no_spread"); b("fast_fire"); b("no_reload")
        b("infinite_ammo"); b("one_shot_hs"); b("any_gun_awm")
        b("pierce_walls"); b("magic_bullet")
        // MOVEMENT
        b("speed_hack"); b("fly_mode"); b("no_fall_dmg"); b("super_jump")
        b("bhop"); b("water_breath"); b("instant_revive"); b("no_collision"); b("wide_view")
        f("speed_mult")
        // INTEL
        b("grenade_alert"); b("loot_esp"); b("zone_timer")
        b("vehicle_esp"); b("minimap_enemy"); b("airdrop_tracker"); b("killfeed_spy")
        // SAFETY
        b("spectator_warn"); b("streamer_mode"); b("hs_cap_dynamic")
        b("replay_sanit"); b("signature_rot")
        i("auto_off_kills")
        // SYS
        b("flag_secure")
    }
}
