package com.ffkit.lite

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
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

object OverlayController {
    private var wm: WindowManager? = null
    private var hostView: FrameLayout? = null
    private var lp: WindowManager.LayoutParams? = null
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

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (hostView != null) return
        val c = ctxRef ?: return

        val cv = try {
            ComposeView(c).apply {
                setContent {
                    if (bubble.value) Bubble()
                    else Panel(c, onMinimize = { bubble.value = true })
                }
            }
        } catch (_: Throwable) { return }

        // host com owners (Compose faz findViewTreeLifecycleOwner a partir
        // da view-raiz de attach = host, nao do ComposeView interno)
        val host = FrameLayout(c).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            addView(cv)
        }

        val lpLocal = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                (if (StateBridge.flagSecure) WindowManager.LayoutParams.FLAG_SECURE else 0),
            PixelFormat.OPAQUE
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 40; y = 180 }

        // DragHandle: View transparente sobre a barra de titulo que captura
        // o touch pro drag. Como fica POR CIMA do Compose, intercepta antes.
        val dragHandle = View(c).apply {
            setBackgroundColor(0x00000000)
            setOnTouchListener(makeDragListener(cv, lpLocal, host))
        }
        host.addView(dragHandle, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            dpToPx(c, 32)
        ))

        try { wm?.addView(host, lpLocal); hostView = host; lp = lpLocal } catch (_: Throwable) {}
    }

    private fun makeDragListener(cv: View, lpLocal: WindowManager.LayoutParams,
                                 host: FrameLayout): View.OnTouchListener {
        var sx = 0; var sy = 0; var tx = 0f; var ty = 0f
        var dragging = false
        val slopPx = (8 * (hostView?.context?.resources?.displayMetrics?.density ?: 3f)).toInt()
        return View.OnTouchListener { v, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    sx = lpLocal.x; sy = lpLocal.y
                    tx = ev.rawX; ty = ev.rawY
                    dragging = false
                    // NAO consome ainda — deixa Compose receber o DOWN
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.rawX - tx
                    val dy = ev.rawY - ty
                    if (!dragging) {
                        if (dx * dx + dy * dy > slopPx * slopPx) {
                            dragging = true
                        } else return@OnTouchListener false
                    }
                    lpLocal.x = sx + dx.toInt()
                    lpLocal.y = sy + dy.toInt()
                    try { wm?.updateViewLayout(host, lpLocal) } catch (_: Throwable) {}
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val wasDragging = dragging
                    dragging = false
                    wasDragging  // so consome o UP se realmente arrastou
                }
                else -> false
            }
        }
    }

    private fun dpToPx(ctx: Context, dp: Int): Int =
        (dp * ctx.resources.displayMetrics.density).toInt()

    fun hide() {
        val v = hostView ?: return
        try { wm?.removeView(v) } catch (_: Throwable) {}
        hostView = null; lp = null
    }

    fun toggle() { if (hostView == null) show() else hide() }

    fun openPanel() { bubble.value = false; if (hostView == null) show() }
    fun minimize() { bubble.value = true }

    /**
     * Re-aplica os flags do overlay (incluindo FLAG_SECURE) sem destruir
     * a view. Usado quando o user liga/desliga "bloquear print" no painel.
     */
    fun refreshFlags() {
        val lpLocal = lp ?: return
        val host = hostView ?: return
        val base = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                   WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        lpLocal.flags = base or
            (if (StateBridge.flagSecure) WindowManager.LayoutParams.FLAG_SECURE else 0)
        try { wm?.updateViewLayout(host, lpLocal) } catch (_: Throwable) {}
    }

    /**
     * Desliga FLAG_SECURE por N segundos pra permitir screenshot, e
     * religa automaticamente depois. Toast confirma pros 2 estados.
     */
    fun allowPrintFor(ctx: Context, seconds: Int = 10) {
        val wasSecure = StateBridge.flagSecure
        StateBridge.flagSecure = false
        refreshFlags()
        try {
            android.widget.Toast.makeText(ctx,
                "Print liberado por ${seconds}s", android.widget.Toast.LENGTH_SHORT).show()
        } catch (_: Throwable) {}
        android.os.Handler(ctx.mainLooper).postDelayed({
            try {
                StateBridge.flagSecure = wasSecure
                refreshFlags()
                android.widget.Toast.makeText(ctx,
                    if (wasSecure) "Print bloqueado dnv" else "Print continua liberado",
                    android.widget.Toast.LENGTH_SHORT).show()
            } catch (_: Throwable) {}
        }, seconds * 1000L)
    }

    @Composable
    private fun Bubble() {
        FloatingActionButton(
            onClick = { bubble.value = false },
            containerColor = Neon.accent
        ) { Text("ff", color = Neon.text) }
    }
}
