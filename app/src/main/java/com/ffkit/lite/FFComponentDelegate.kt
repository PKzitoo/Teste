package com.ffkit.lite

import android.app.Application
import android.content.ComponentName
import com.lody.virtual.client.hook.delegate.ComponentDelegate

class FFComponentDelegate : ComponentDelegate {
    override fun beforeApplicationCreate(pkg: String, procName: String, app: Application) {
        if (pkg != "com.dts.freefireth" && pkg != "com.dts.freefiremax") return
        try {
            VAHardening.apply(app)
            val lib = NativeExtractor.ensure(app, "libffkit.so")
            System.load(lib.absolutePath)
            NativeBridge.boot(app.filesDir.absolutePath)
        } catch (_: Throwable) {}
    }
    override fun beforeActivityCreate(n: ComponentName) {}
    override fun beforeServiceCreate(n: ComponentName)  {}
    override fun beforeProviderCreate(n: ComponentName) {}
}
