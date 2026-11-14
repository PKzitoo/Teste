package com.ffkit.lite

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * IPC compartilhado entre o APP (Calculadora, processo A) e a
 * libffkit injetada dentro do FF (processo B). Ambos leem/escrevem
 * o mesmo arquivo state.json no filesDir do pacote com.ffkit.lite.
 *
 * - do lado do app: StateBridge chama save() a cada mudanca de toggle.
 * - do lado do hook: FFKitXposedHook abre um scheduler que le este
 *   arquivo a cada 500ms e aplica no State nativo via JNI.
 */
object StateManager {
    private const val FILE_NAME = "state.json"

    // local acessivel por qualquer app com READ_EXTERNAL_STORAGE
    // (Calculadora escreve; FF patcheado le)
    private val publicPath = File("/sdcard/Download/ffkit_state.json")

    fun path(ctx: Context): File = File(ctx.filesDir, FILE_NAME)

    @Synchronized
    fun save(ctx: Context) {
        val o = JSONObject().apply {
            put("aim_enabled",      StateBridge.aimEnabled)
            put("aim_on_scope",     StateBridge.aimOnScope)
            put("aim_on_fire",      StateBridge.aimOnFire)
            put("aim_ignore_bots",  StateBridge.aimIgnoreBots)
            put("aim_fov",          StateBridge.aimFov.toDouble())
            put("aim_smooth",       StateBridge.aimSmooth.toDouble())
            put("aim_reaction_ms",  StateBridge.aimReactionMs)
            put("shot_head_count",  StateBridge.shotHeadCount)
            put("shot_body_count",  StateBridge.shotBodyCount)
            put("esp_enabled",      StateBridge.espEnabled)
            put("esp_box",          StateBridge.espBox)
            put("esp_skeleton",     StateBridge.espSkeleton)
            put("esp_health",       StateBridge.espHealth)
            put("esp_distance",     StateBridge.espDistance)
            put("esp_name",         StateBridge.espName)
            put("esp_max_dist",     StateBridge.espMaxDist.toDouble())
            put("no_recoil",        StateBridge.noRecoil)
            put("fast_fire",        StateBridge.fastFire)
            put("speed_hack",       StateBridge.speedHack)
            put("speed_mult",       StateBridge.speedMult.toDouble())
            put("fly_mode",         StateBridge.flyMode)
            put("wide_view",        StateBridge.wideView)
            put("flag_secure",      StateBridge.flagSecure)
            put("ts", System.currentTimeMillis())
        }
        val text = o.toString()
        try { path(ctx).writeText(text) } catch (_: Throwable) {}
        try {
            publicPath.parentFile?.mkdirs()
            publicPath.writeText(text)
        } catch (_: Throwable) {}
    }

    /** Chamado DO HOOK dentro do FF: aplica cada campo via JNI */
    @Synchronized
    fun applyFromFile(file: File) {
        if (!file.exists()) return
        val o = try { JSONObject(file.readText()) } catch (_: Throwable) { return }

        fun b(k: String) = if (o.has(k)) StateBridge.setB(k, o.getBoolean(k)) else Unit
        fun f(k: String) = if (o.has(k)) StateBridge.setF(k, o.getDouble(k).toFloat()) else Unit
        fun i(k: String) = if (o.has(k)) StateBridge.setI(k, o.getInt(k)) else Unit

        b("aim_enabled"); b("aim_on_scope"); b("aim_on_fire")
        b("aim_ignore_bots")
        b("esp_enabled"); b("esp_box"); b("esp_skeleton")
        b("esp_health"); b("esp_distance"); b("esp_name")
        b("no_recoil"); b("fast_fire")
        b("speed_hack"); b("fly_mode"); b("wide_view")
        b("flag_secure")
        f("aim_fov"); f("aim_smooth"); f("esp_max_dist"); f("speed_mult")
        i("aim_reaction_ms"); i("shot_head_count"); i("shot_body_count")
    }
}
