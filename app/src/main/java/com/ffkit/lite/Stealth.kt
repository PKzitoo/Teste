package com.ffkit.lite
import android.app.ActivityManager
import android.content.Context
import java.io.File

object Stealth {
    fun hideRecents(ctx: Context) {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.appTasks.forEach { it.setExcludeFromRecents(true) }
    }
    fun wipe(ctx: Context) {
        ctx.cacheDir.deleteRecursively()
        ctx.codeCacheDir.deleteRecursively()
        File(ctx.filesDir, "logs").deleteRecursively()
        Runtime.getRuntime().exec(arrayOf("logcat", "-c"))
    }
}
