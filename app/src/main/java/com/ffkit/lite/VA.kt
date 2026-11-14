package com.ffkit.lite
import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.lody.virtual.client.core.VirtualCore
import java.io.File

object VA {
    private const val FF = "com.dts.freefireth"
    private const val FF_MAX = "com.dts.freefiremax"

    fun isInstalled(): Boolean = try {
        VirtualCore.get().isAppInstalled(FF) || VirtualCore.get().isAppInstalled(FF_MAX)
    } catch (_: Throwable) { false }

    fun installFromUri(ctx: Context, uri: Uri): String {
        val tmp = File(ctx.cacheDir, "ff.apk")
        ctx.contentResolver.openInputStream(uri)!!.use { input ->
            tmp.outputStream().use { input.copyTo(it) }
        }
        val r = VirtualCore.get().installPackage(
            tmp.absolutePath,
            VirtualCore.FLAG_RUN_APK_IN_VM or VirtualCore.FLAG_PRELOAD_ALL
        )
        return if (r.isSuccess) "ok" else (r.error ?: "falha")
    }

    fun launch(ctx: Context) {
        val pkg = if (VirtualCore.get().isAppInstalled(FF)) FF else FF_MAX
        if (!VirtualCore.get().isAppInstalled(pkg)) {
            Toast.makeText(ctx, "instala o free fire primeiro", Toast.LENGTH_SHORT).show(); return
        }
        val intent = VirtualCore.get().getLaunchIntent(pkg, 0)
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    }
}
