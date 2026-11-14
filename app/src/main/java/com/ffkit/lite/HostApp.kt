package com.ffkit.lite

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import com.lody.virtual.client.core.VirtualCore
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HostApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        installCrashHandler(base)
        try { VirtualCore.get().startup(this) } catch (_: Throwable) {}
    }
    override fun onCreate() {
        super.onCreate()
        StateBridge.appCtx = this  // habilita IPC: toggle -> state.json
        try { VirtualCore.get().setComponentDelegate(FFComponentDelegate()) } catch (_: Throwable) {}
    }

    /**
     * Grava a stacktrace de qualquer crash fatal em:
     *   /sdcard/Download/ffkit_crash.log
     * e em ctx.filesDir/crash.log (fallback caso scoped storage bloqueie).
     * Depois re-propaga pro default handler pro Android fechar/relogar.
     */
    private fun installCrashHandler(ctx: Context) {
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val msg = "[$stamp] thread=${t.name}\n$sw\n"
                // tenta scoped storage primeiro
                try {
                    val dl = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS)
                    if (!dl.exists()) dl.mkdirs()
                    File(dl, "ffkit_crash.log").appendText(msg)
                } catch (_: Throwable) {}
                // fallback: filesDir (sempre acessivel)
                try { File(ctx.filesDir, "crash.log").appendText(msg) } catch (_: Throwable) {}
                Log.e("ffkit-crash", msg)
            } catch (_: Throwable) {}
            prev?.uncaughtException(t, e)
        }
    }
}
