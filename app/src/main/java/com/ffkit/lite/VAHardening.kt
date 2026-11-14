package com.ffkit.lite
import android.app.Application

object VAHardening {
    fun apply(app: Application) {
        val pkg = "com.dts.freefireth"
        val ai = app.applicationInfo
        val realDir = "/data/data/$pkg"
        setField(ai, "dataDir", realDir)
        setField(ai, "deviceProtectedDataDir", realDir)
        setField(ai, "nativeLibraryDir", "$realDir/lib/arm64")
        setField(ai, "sourceDir", "/data/app/$pkg-1/base.apk")
        setField(ai, "publicSourceDir", "/data/app/$pkg-1/base.apk")
        setField(ai, "processName", pkg)
    }
    private fun setField(obj: Any, name: String, value: Any) {
        val f = obj.javaClass.getDeclaredField(name); f.isAccessible = true
        f.set(obj, value)
    }
}
