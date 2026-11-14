package com.lody.virtual.client.core

import android.content.Context
import android.content.Intent
import com.lody.virtual.client.hook.delegate.ComponentDelegate
import com.lody.virtual.remote.InstallResult

/**
 * STUB — VirtualApp API shim. Substitua por `com.lody:VirtualApp:source` ou
 * fork publicado com os binarios reais pra habilitar a parte de container.
 * Nesta build o metodo so retorna falha controlada; o painel segue funcionando.
 */
class VirtualCore private constructor() {
    companion object {
        const val FLAG_RUN_APK_IN_VM = 1
        const val FLAG_PRELOAD_ALL = 2
        private val I = VirtualCore()
        @JvmStatic fun get(): VirtualCore = I
    }

    fun startup(ctx: Context) { /* stub */ }
    fun setComponentDelegate(d: ComponentDelegate) { /* stub */ }
    fun isAppInstalled(pkg: String): Boolean = false
    fun installPackage(path: String, flags: Int): InstallResult =
        InstallResult("VA runtime nao embutido neste APK (stub). Veja README.")
    fun getLaunchIntent(pkg: String, userId: Int): Intent? = null
    fun setAppRequestListener(l: Any?) { /* stub */ }
}
