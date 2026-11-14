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
 * - do lado do hook Xposed: FFKitXposedHook abre um scheduler que
 *   le este arquivo a cada 500ms e aplica no State nativo via JNI.
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
            put("aim_bone",         StateBridge.aimBone)
            put("aim_miss_rate",    StateBridge.aimMissRate.toDouble())
            put("aim_bone_random",  StateBridge.aimBoneRandom)
            put("aim_reaction_ms",  StateBridge.aimReactionMs)
            put("aim_kpm_cap",      StateBridge.aimKpmCap)
            put("aim_max_hs_streak",StateBridge.aimMaxHsStreak)
            put("no_recoil",        StateBridge.noRecoil)
            put("speed_hack",       StateBridge.speedHack)
            put("flag_secure",      StateBridge.flagSecure)
            put("ts", System.currentTimeMillis())
        }
        val text = o.toString()
        // 1) copia local pro app (compatibilidade)
        try { path(ctx).writeText(text) } catch (_: Throwable) {}
        // 2) copia publica em /sdcard/Download/ pro FF patcheado ler
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
        b("aim_ignore_bots"); b("aim_bone_random")
        b("no_recoil"); b("speed_hack"); b("flag_secure")
        f("aim_fov"); f("aim_smooth"); f("aim_miss_rate")
        i("aim_bone"); i("aim_reaction_ms"); i("aim_kpm_cap"); i("aim_max_hs_streak")
    }
}
