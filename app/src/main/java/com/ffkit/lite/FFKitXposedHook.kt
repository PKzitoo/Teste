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
 * Modulo Xposed do ffkit. Dentro do FF patcheado (LSPatch), carrega
 * a libffkit.so no processo do jogo e abre um poller que le o
 * state.json escrito pela Calculadora a cada 500ms, aplicando os
 * toggles em tempo real via JNI.
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
                        VAHardening.apply(app)

                        val ffkitPkg = "com.ffkit.lite"
                        val ctx = app.createPackageContext(ffkitPkg,
                            Context.CONTEXT_IGNORE_SECURITY)
                        val lib = extractSo(ctx, app)
                        System.load(lib.absolutePath)
                        NativeBridge.boot(app.filesDir.absolutePath)

                        // scheduler IPC: le state.json da Calculadora
                        val statePath = File(ctx.filesDir, "state.json")
                        startStatePoller(statePath)
                    } catch (_: Throwable) {}
                }
            })
    }

    private fun extractSo(src: Context, dst: Context): File {
        val out = File(dst.filesDir, "libffkit.so")
        if (!out.exists()) {
            src.assets.open("arm64-v8a/libffkit.so").use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
            out.setExecutable(true)
        }
        return out
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
