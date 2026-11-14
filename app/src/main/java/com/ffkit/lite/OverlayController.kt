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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    private var dragHandle: View? = null
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
                    else Panel(c, onMinimize = { minimize() })
                }
            }
        } catch (_: Throwable) { return }

        val host = FrameLayout(c).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            // IMPORTANTE: addView sem params usa MATCH_PARENT, que estica
            // o painel/bolinha pra ocupar o parent todo. Forca WRAP_CONTENT.
            addView(cv, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ))
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

        val dh = View(c).apply {
            setBackgroundColor(0x00000000)
            setOnTouchListener(makeDragListener(cv, lpLocal, host))
        }
        // altura do handle depende do modo: bubble=inteiro, panel=so a barra
        host.addView(dh, dragHandleParamsForMode(c))
        dragHandle = dh

        try { wm?.addView(host, lpLocal); hostView = host; lp = lpLocal } catch (_: Throwable) {}
    }

    private fun dragHandleParamsForMode(c: Context): FrameLayout.LayoutParams =
        if (bubble.value)
            // MESMO tamanho que a bolinha (48dp). NAO usar MATCH_PARENT
            // ou vira capsula gigante esticando o host.
            FrameLayout.LayoutParams(dpToPx(c, 48), dpToPx(c, 48))
        else
            // barra de titulo do painel: cobre toda largura, 32dp alto
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                     dpToPx(c, 32))

    private fun syncDragHandle() {
        val c = ctxRef ?: return
        val host = hostView ?: return
        val dh = dragHandle ?: return
        try {
            dh.layoutParams = dragHandleParamsForMode(c)
            host.requestLayout()
        } catch (_: Throwable) {}
    }

    private fun makeDragListener(cv: View, lpLocal: WindowManager.LayoutParams,
                                 host: FrameLayout): View.OnTouchListener {
        var sx = 0; var sy = 0; var tx = 0f; var ty = 0f
        var dragging = false
        val density = hostView?.context?.resources?.displayMetrics?.density ?: 3f
        val slopPx = (8 * density).toInt()
        return View.OnTouchListener { _, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    sx = lpLocal.x; sy = lpLocal.y
                    tx = ev.rawX; ty = ev.rawY
                    dragging = false
                    false   // deixa Compose receber o DOWN
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.rawX - tx
                    val dy = ev.rawY - ty
                    if (!dragging) {
                        if (dx * dx + dy * dy > slopPx * slopPx) dragging = true
                        else return@OnTouchListener false
                    }
                    lpLocal.x = sx + dx.toInt()
                    lpLocal.y = sy + dy.toInt()
                    try { wm?.updateViewLayout(host, lpLocal) } catch (_: Throwable) {}
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val wasDragging = dragging
                    dragging = false
                    wasDragging
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
        hostView = null; lp = null; dragHandle = null
    }

    fun toggle() { if (hostView == null) show() else hide() }

    fun openPanel() {
        bubble.value = false
        if (hostView == null) show() else syncDragHandle()
    }
    fun minimize() {
        bubble.value = true
        syncDragHandle()
    }

    fun refreshFlags() {
        val lpLocal = lp ?: return
        val host = hostView ?: return
        val base = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                   WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        lpLocal.flags = base or
            (if (StateBridge.flagSecure) WindowManager.LayoutParams.FLAG_SECURE else 0)
        try { wm?.updateViewLayout(host, lpLocal) } catch (_: Throwable) {}
    }

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

    /**
     * Bolinha minimalista: circulo 48dp azul solido com "ff" branco
     * no centro. Click instantaneo (sem ripple animation) = abre painel.
     * Drag em qualquer parte dela (via dragHandle MATCH_PARENT) = move.
     */
    @Composable
    private fun Bubble() {
        val interaction = remember { MutableInteractionSource() }
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Neon.accent)
                .border(1.5.dp, Neon.accent3, CircleShape)
                .clickable(
                    interactionSource = interaction,
                    indication = null  // zero ripple -> resposta instantanea
                ) { openPanel() },
            contentAlignment = Alignment.Center
        ) {
            Text("ff", color = Color.White, fontSize = 15.sp,
                fontWeight = FontWeight.Black)
        }
    }
}
