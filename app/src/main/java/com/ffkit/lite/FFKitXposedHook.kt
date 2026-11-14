package com.ffkit.lite

import android.app.Application
import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

/**
 * Modulo Xposed do ffkit. Dentro do VirtualXposed, quando o Free Fire
 * (com.dts.freefireth / freefiremax) carrega, a libffkit.so eh extraida
 * dos assets do nosso APK e carregada NO MESMO PROCESSO do jogo.
 */
class FFKitXposedHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val pkg = lpparam.packageName
        if (pkg != "com.dts.freefireth" && pkg != "com.dts.freefiremax") return

        // intercepta Application.attach pra injetar antes do jogo iniciar
        val appClass = XposedHelpers.findClass("android.app.Application", lpparam.classLoader)
        XposedHelpers.findAndHookMethod(appClass, "attach", Context::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        val app = param.thisObject as Application
                        VAHardening.apply(app)

                        // extrai a .so do APK do ffkit (dataDir dele eh diferente,
                        // pegamos via PackageManager do sistema)
                        val ffkitPkg = "com.ffkit.lite"
                        val ctx = app.createPackageContext(ffkitPkg,
                            Context.CONTEXT_IGNORE_SECURITY)
                        val lib = extractSo(ctx, app)
                        System.load(lib.absolutePath)
                        NativeBridge.boot(app.filesDir.absolutePath)
                    } catch (_: Throwable) { /* silencio em producao */ }
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
}
