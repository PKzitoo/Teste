package com.lody.virtual.client.hook.delegate

import android.app.Application
import android.content.ComponentName

interface ComponentDelegate {
    fun beforeApplicationCreate(pkg: String, procName: String, app: Application)
    fun beforeActivityCreate(n: ComponentName)
    fun beforeServiceCreate(n: ComponentName)
    fun beforeProviderCreate(n: ComponentName)
}
