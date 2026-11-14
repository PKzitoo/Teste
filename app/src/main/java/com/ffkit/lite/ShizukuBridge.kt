package com.ffkit.lite

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import rikka.shizuku.Shizuku

/**
 * Cliente Shizuku.
 *
 * Shizuku roda um servico com UID do shell no device. Com permissao
 * concedida, podemos rodar comandos como se fossemos `adb shell`:
 *  - settings put secure enabled_accessibility_services ...
 *  - pm grant
 *  - appops set
 *
 * Isso BYPASSA o "Restricted Settings" da HyperOS, que eh justamente
 * o que trava o toggle de Acessibilidade pro ffkit.
 *
 * Nao requer root. Nao requer unlock bootloader. Precisa apenas que
 * o usuario ja tenha o Shizuku ativo (via Wireless Debugging), o
 * que ele confirmou que tem.
 */
object ShizukuBridge {

    private const val REQ_PERM = 0xF6F6

    private var permListener: Shizuku.OnRequestPermissionResultListener? = null

    enum class State { UNAVAILABLE, NO_PERMISSION, READY }

    fun state(): State = try {
        if (!Shizuku.pingBinder()) State.UNAVAILABLE
        else if (Shizuku.isPreV11()) State.UNAVAILABLE  // versao antiga, sem API
        else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) State.READY
        else State.NO_PERMISSION
    } catch (_: Throwable) { State.UNAVAILABLE }

    /** chama uma vez na MainActivity pra ouvir resultado do dialog */
    fun installListener(onGranted: () -> Unit) {
        try {
            permListener?.let { Shizuku.removeRequestPermissionResultListener(it) }
            val l = Shizuku.OnRequestPermissionResultListener { requestCode, result ->
                if (requestCode == REQ_PERM && result == PackageManager.PERMISSION_GRANTED) {
                    onGranted()
                }
            }
            Shizuku.addRequestPermissionResultListener(l)
            permListener = l
        } catch (_: Throwable) {}
    }

    fun requestPermission() {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(REQ_PERM)
            }
        } catch (_: Throwable) {}
    }

    /**
     * Roda um comando shell via Shizuku.
     * Retorna o exitCode ou -1 se der pau.
     *
     * Shizuku.newProcess eh @hide na API 13.x — chamamos via reflection,
     * que eh o padrao usado pelo proprio projeto Shizuku-API nos exemplos.
     */
    fun runShell(cmd: String): Int {
        return try {
            val m = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java, Array<String>::class.java, String::class.java
            )
            m.isAccessible = true
            val p = m.invoke(null, arrayOf("sh", "-c", cmd),
                null as Array<String>?, null as String?) as Process
            Thread { try { p.inputStream.bufferedReader().forEachLine { } } catch (_: Throwable) {} }.start()
            Thread { try { p.errorStream.bufferedReader().forEachLine { } } catch (_: Throwable) {} }.start()
            p.waitFor()
            p.exitValue()
        } catch (_: Throwable) { -1 }
    }

    /**
     * BYPASS do Restricted Settings: liga a Acessibilidade do ffkit
     * diretamente via `settings put secure`, sem passar pela UI.
     *
     * A HyperOS bloqueia o toggle, mas NAO bloqueia o write se vier
     * do shell (que eh de onde o Shizuku vem).
     */
    fun autoEnableAccessibility(ctx: Context): Boolean {
        val pkg = ctx.packageName
        val svc = "$pkg/$pkg.PanicService"

        // pega lista atual, concatena, re-grava.
        // formato: "pkg1/service1:pkg2/service2"
        val r1 = runShell(
            "cur=\$(settings get secure enabled_accessibility_services 2>/dev/null); " +
            "if [ -z \"\$cur\" ] || [ \"\$cur\" = \"null\" ]; then " +
            "  settings put secure enabled_accessibility_services $svc; " +
            "elif echo \"\$cur\" | grep -q \"$svc\"; then " +
            "  echo already; " +
            "else " +
            "  settings put secure enabled_accessibility_services \"\$cur:$svc\"; " +
            "fi"
        )
        val r2 = runShell("settings put secure accessibility_enabled 1")

        return r1 == 0 && r2 == 0
    }

    fun autoDisableAccessibility(ctx: Context): Boolean {
        val pkg = ctx.packageName
        val svc = "$pkg/$pkg.PanicService"
        val r = runShell(
            "cur=\$(settings get secure enabled_accessibility_services 2>/dev/null); " +
            "new=\$(echo \"\$cur\" | sed \"s|$svc||g\" | sed 's|::|:|g' | sed 's|^:||' | sed 's|:\$||'); " +
            "settings put secure enabled_accessibility_services \"\$new\""
        )
        return r == 0
    }

    fun toast(ctx: Context, msg: String) {
        try {
            val h = android.os.Handler(ctx.mainLooper)
            h.post { Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show() }
        } catch (_: Throwable) {}
    }
}
