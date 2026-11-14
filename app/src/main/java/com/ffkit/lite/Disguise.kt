package com.ffkit.lite
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object Disguise {
    private val skins = listOf(
        "com.ffkit.lite.Calc",
        "com.ffkit.lite.Weather",
        "com.ffkit.lite.Notes",
        "com.ffkit.lite.Flashlight",
        "com.ffkit.lite.Launcher"
    )
    private var idx = 0
    fun cycle(ctx: Context) {
        val pm = ctx.packageManager
        skins.forEachIndexed { i, alias ->
            pm.setComponentEnabledSetting(
                ComponentName(ctx, alias),
                if (i == idx) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else           PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        idx = (idx + 1) % skins.size
    }
}
