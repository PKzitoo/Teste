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

/**
 * Modulo Xposed do ffkit. No LSPatch (integrated mode), o APK do
 * modulo eh EMBUTIDO dentro do FF patcheado — nao precisa (e nao
 * pode) usar createPackageContext("com.ffkit.lite") porque a
 * assinatura do FF patcheado difere da do ffkit. A libffkit.so
 * eh extraida direto dos assets do PROPRIO classloader do modulo.
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
                        try { VAHardening.apply(app) } catch (_: Throwable) {}

                        // 1) extrai a .so dos assets do MODULO (nao do app FFkit)
                        val lib = extractSoFromSelf(app)
                        if (lib == null) return
                        System.load(lib.absolutePath)
                        NativeBridge.boot(app.filesDir.absolutePath)

                        // 2) poller IPC: le state.json de /sdcard/Download/ffkit_state.json
                        //    (local publico que a Calculadora escreve com MediaStore;
                        //     se o arquivo nao existe, so usa os defaults do State nativo.)
                        val publicState = File("/sdcard/Download/ffkit_state.json")
                        startStatePoller(publicState)
                    } catch (_: Throwable) {
                        // silencio em producao — NUNCA deixar o hook crashar o FF
                    }
                }
            })
    }

    /**
     * Extrai a libffkit.so pros files do FF, usando o classloader
     * do PROPRIO modulo (que tem acesso aos assets empacotados pelo
     * LSPatch dentro do APK patcheado).
     */
    private fun extractSoFromSelf(dst: Context): File? {
        val out = File(dst.filesDir, "libffkit.so")
        if (out.exists() && out.length() > 0) return out
        val cl = this::class.java.classLoader ?: return null
        // tenta 2 paths conhecidos de embed (LSPatch coloca os assets
        // do modulo em assets/ dentro do jar do modulo)
        val candidates = listOf(
            "assets/arm64-v8a/libffkit.so",
            "arm64-v8a/libffkit.so",
            "lib/arm64-v8a/libffkit.so"
        )
        for (p in candidates) {
            val input = cl.getResourceAsStream(p) ?: continue
            input.use { s -> out.outputStream().use { s.copyTo(it) } }
            if (out.length() > 0) { out.setExecutable(true); return out }
        }
        return null
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
