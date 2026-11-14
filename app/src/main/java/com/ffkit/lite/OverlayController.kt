package com.ffkit.lite

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * Singleton — gerencia bolha + painel flutuante.
 * MainActivity chama ensure(ctx)+show() quando o PIN entra.
 * Botao "esconder bolha" chama hide(); PanicService (VOL+ 3x)
 * chama toggle(). PANIC (VOL- 3x) chama hideFully().
 */
object OverlayController {
    private var wm: WindowManager? = null
    private var view: ComposeView? = null
    private var ctxRef: Context? = null
    private val bubble = mutableStateOf(true)

    fun ensure(ctx: Context) {
        if (ctxRef == null) {
            ctxRef = ctx.applicationContext
            wm = ctxRef!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
    }

    private class OverlayOwners : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
        private val _lifecycle = LifecycleRegistry.createUnsafe(this)
        private val _vms = ViewModelStore()
        private val _ssr = SavedStateRegistryController.create(this)
        init {
            _ssr.performAttach()
            _ssr.performRestore(null)
            _lifecycle.currentState = Lifecycle.State.RESUMED
        }
        override val lifecycle: Lifecycle get() = _lifecycle
        override val viewModelStore: ViewModelStore get() = _vms
        override val savedStateRegistry: SavedStateRegistry get() = _ssr.savedStateRegistry
    }
    private val lifecycleOwner by lazy { OverlayOwners() }

    fun show() {
        if (view != null) return
        val c = ctxRef ?: return
        val cv = try {
            ComposeView(c).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setContent { if (bubble.value) Bubble() else Panel(c) { bubble.value = true } }
            }
        } catch (t: Throwable) { return }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                (if (StateBridge.flagSecure) WindowManager.LayoutParams.FLAG_SECURE else 0),
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 40; y = 180 }
        try { wm?.addView(cv, lp); view = cv } catch (_: Throwable) {}
    }

    fun hide() {
        view?.let { wm?.removeView(it) }
        view = null
    }

    fun toggle() { if (view == null) show() else hide() }

    fun openPanel() { bubble.value = false; if (view == null) show() }
    fun minimize() { bubble.value = true }

    @Composable
    private fun Bubble() {
        FloatingActionButton(onClick = { bubble.value = false }) { Text("ff") }
    }
}
