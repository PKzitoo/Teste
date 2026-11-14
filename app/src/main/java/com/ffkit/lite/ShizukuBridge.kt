package com.ffkit.lite

import android.content.pm.PackageManager
import android.widget.Toast
import rikka.shizuku.Shizuku
import java.io.OutputStream

/**
 * Cliente Shizuku compacto.
 *
 * Mantem UM processo shell aberto em background (PERSISTENT_SHELL)
 * e pipeia comandos `input swipe` / `input tap` por stdin. Isso
 * evita o overhead de ~50-100ms que teria iniciando um novo
 * processo pra cada toque — critico pro recoil que roda a 30-50 Hz.
 */
object ShizukuBridge {
    private const val REQ_PERM = 0xF6F6
    private var permListener: Shizuku.OnRequestPermissionResultListener? = null

    enum class State { UNAVAILABLE, NO_PERMISSION, READY }

    @Volatile private var persistent: Process? = null
    @Volatile private var sink: OutputStream? = null
    private val sinkLock = Any()

    fun state(): State = try {
        if (!Shizuku.pingBinder()) State.UNAVAILABLE
        else if (Shizuku.isPreV11()) State.UNAVAILABLE
        else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) State.READY
        else State.NO_PERMISSION
    } catch (_: Throwable) { State.UNAVAILABLE }

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
     * Garante que o shell persistente esta vivo. Chama antes do primeiro
     * comando. Retorna false se nao conseguiu (Shizuku off ou sem perm).
     */
    fun ensureShell(): Boolean {
        synchronized(sinkLock) {
            val p = persistent
            if (p != null && p.isAlive) return true
            if (state() != State.READY) return false

            return try {
                val m = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java, Array<String>::class.java, String::class.java
                )
                m.isAccessible = true
                val proc = m.invoke(null, arrayOf("sh"),
                    null as Array<String>?, null as String?) as Process
                // consome stdout/stderr pra nao bloquear
                Thread { try { proc.inputStream.bufferedReader().forEachLine { } } catch (_: Throwable) {} }
                    .apply { isDaemon = true }.start()
                Thread { try { proc.errorStream.bufferedReader().forEachLine { } } catch (_: Throwable) {} }
                    .apply { isDaemon = true }.start()
                persistent = proc
                sink = proc.outputStream
                true
            } catch (_: Throwable) { false }
        }
    }

    /**
     * Manda um comando shell pro processo persistente. Bota \n no final.
     * Thread-safe.
     */
    fun send(cmd: String): Boolean {
        if (!ensureShell()) return false
        return try {
            synchronized(sinkLock) {
                val s = sink ?: return false
                s.write((cmd + "\n").toByteArray())
                s.flush()
            }
            true
        } catch (_: Throwable) {
            // tenta reconectar na proxima chamada
            persistent = null; sink = null
            false
        }
    }

    /** shortcut pra `input swipe` (coordenadas em px, duracao em ms) */
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durMs: Int) =
        send("input swipe $x1 $y1 $x2 $y2 $durMs")

    /** shortcut pra `input tap` */
    fun tap(x: Int, y: Int) =
        send("input tap $x $y")

    fun toast(ctx: android.content.Context, msg: String) {
        try {
            android.os.Handler(ctx.mainLooper).post {
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            }
        } catch (_: Throwable) {}
    }
}
