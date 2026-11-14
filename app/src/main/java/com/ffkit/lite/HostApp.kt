package com.ffkit.lite

import android.app.Application
import android.content.Context
import com.lody.virtual.client.core.VirtualCore

class HostApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        try { VirtualCore.get().startup(this) } catch (_: Throwable) {}
    }
    override fun onCreate() {
        super.onCreate()
        try { VirtualCore.get().setComponentDelegate(FFComponentDelegate()) } catch (_: Throwable) {}
    }
}
