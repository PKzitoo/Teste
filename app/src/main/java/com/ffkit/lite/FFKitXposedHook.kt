package com.ffkit.lite

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File
import java.util.zip.ZipFile

/**
 * Modulo ffkit pro LSPosed (root).
 *
 * Fluxo:
 *  1. LSPosed carrega o APK do modulo no processo do FF
 *  2. handleLoadPackage dispara quando o pkg bate com xposed_scope
 *  3. Hook em Application.attach() pra rodar depois que o FF
 *     terminou de construir seu Application (classloader estavel)
 *  4. Extrai libffkit.so do APK do modulo (lpparam.appInfo.sourceDir
 *     via ZipFile) pra files do FF, e System.load()
 *  5. NativeBridge.boot() entrega o filesDir pro codigo nativo
 *  6. Poller IPC: le state.json de /sdcard/Download/ffkit_state.json
 *     que o painel (Calculadora) escreve, aplica no state nativo
 */
class FFKitXposedHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val pkg = lpparam.packageName
        if (pkg != "com.dts.freefireth" && pkg != "com.dts.freefiremax") return

        val appClass = XposedHelpers.findClass("android.app.Application", lpparam.classLoader)
        XposedHelpers.findAndHookMethod(appClass, "attach", Context::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        val app = param.thisObject as Application

                        // 1) extrai libffkit.so DO APK DO MODULO
                        //    LSPosed expõe o path do APK do modulo via
                        //    lpparam.appInfo.sourceDir — mas isso eh o APK do FF.
                        //    O APK do modulo ta no classloader do proprio
                        //    FFKitXposedHook — usamos codeSource.
                        val lib = extractSoFromModule(app) ?: return
                        System.load(lib.absolutePath)
                        NativeBridge.boot(app.filesDir.absolutePath)

                        // 2) poller IPC
                        val publicState = File("/sdcard/Download/ffkit_state.json")
                        startStatePoller(publicState)
                    } catch (_: Throwable) {
                        // silencio em producao: nunca deixar o hook crashar o FF
                    }
                }
            })
    }

    /**
     * Extrai a .so do APK do modulo pros filesDir do FF, mas com NOME
     * DISFARCADO ("libart_cache.so") pra nao aparecer como "ffkit" em
     * nenhum /proc/self/maps caso Shamiko falhe em algum momento.
     */
    private fun extractSoFromModule(dst: Context): File? {
        // nome final disfarcado — parece lib da ART runtime
        val out = File(dst.filesDir, "libart_cache.so")
        if (out.exists() && out.length() > 0) return out

        val modulePath: String? = try {
            this::class.java.protectionDomain?.codeSource?.location?.path
        } catch (_: Throwable) { null } ?: return null

        return try {
            ZipFile(modulePath).use { z ->
                val candidates = listOf(
                    "lib/arm64-v8a/libffkit.so",
                    "assets/arm64-v8a/libffkit.so"
                )
                for (p in candidates) {
                    val e = z.getEntry(p) ?: continue
                    z.getInputStream(e).use { input ->
                        out.outputStream().use { input.copyTo(it) }
                    }
                    if (out.length() > 0) {
                        out.setExecutable(true)
                        return@use out
                    }
                }
                null
            }
        } catch (_: Throwable) { null }
    }

    private fun startStatePoller(statePath: File) {
        val thread = HandlerThread("ffkit-ipc").apply { start() }
        val h = Handler(thread.looper)
        var lastMtime = 0L
        val task = object : Runnable {
            override fun run() {
                try {
                    val m = statePath.lastModified()
                    if (m != 0L && m != lastMtime) {
                        StateManager.applyFromFile(statePath)
                        lastMtime = m
                    }
                } catch (_: Throwable) {}
                h.postDelayed(this, 500)
            }
        }
        h.post(task)
    }
}
