package com.ffkit.lite

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

/**
 * Botao flutuante "ASSIST" que, quando segurado, liga o RecoilEngine.
 * Quando solto, desliga.
 *
 * O botao eh desenhado com Canvas (nao Compose) porque:
 *  • toque precisa ser instantaneo e nao bloqueado por recomposicao
 *  • queremos consumir o touch (nao passar pra view de baixo) — o
 *    user pode arrastar pra reposicionar com long-press, mas touch
 *    curto apenas liga/desliga o recoil
 *
 * Posicao, tamanho e opacidade sao lidos do StateBridge (user
 * configura no painel). Reposicionar: long-press 400ms depois arrasta.
 */
class AssistTrigger private constructor(ctx: Context) : View(ctx) {

    private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        typeface = Typeface.DEFAULT_BOLD
    }
    private var pressed = false
    private var longPressing = false
    private var downX = 0f; private var downY = 0f
    private var downTime = 0L
    private var dragStartLpX = 0; private var dragStartLpY = 0

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val cx = w / 2f; val cy = h / 2f
        val r = minOf(w, h) / 2f - 4f

        val alpha = (StateBridge.triggerOpacity * 255).toInt().coerceIn(40, 255)
        val baseColor = if (pressed) 0xFFEF4444.toInt()      // vermelho ativo
                        else         0xFF2563EB.toInt()      // azul padrao
        paintBg.color = (baseColor and 0x00FFFFFF) or (alpha shl 24)
        canvas.drawCircle(cx, cy, r, paintBg)

        paintRing.strokeWidth = 3f
        paintRing.alpha = (alpha * 0.7f).toInt()
        canvas.drawCircle(cx, cy, r, paintRing)

        paintText.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 11f, resources.displayMetrics)
        paintText.alpha = alpha
        val label = if (pressed) "ON" else "FIRE"
        val fm = paintText.fontMetrics
        canvas.drawText(label, cx, cy - (fm.ascent + fm.descent) / 2, paintText)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = e.rawX; downY = e.rawY
                downTime = System.currentTimeMillis()
                dragStartLpX = lp.x; dragStartLpY = lp.y
                pressed = true
                longPressing = false
                invalidate()
                RecoilEngine.start()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val held = System.currentTimeMillis() - downTime
                val dx = e.rawX - downX
                val dy = e.rawY - downY
                // long-press + move = reposiciona
                if (!longPressing && held > 400 && (dx * dx + dy * dy) > 40 * 40) {
                    longPressing = true
                    RecoilEngine.stop()  // nao atira enquanto arrasta
                    pressed = false
                    invalidate()
                }
                if (longPressing) {
                    lp.x = (dragStartLpX + dx).toInt()
                    lp.y = (dragStartLpY + dy).toInt()
                    try { wm?.updateViewLayout(this, lp) } catch (_: Throwable) {}
                    // salva posicao enquanto arrasta
                    StateBridge.triggerX = lp.x
                    StateBridge.triggerY = lp.y
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                pressed = false
                invalidate()
                RecoilEngine.stop()
                return true
            }
        }
        return false
    }

    companion object {
        private var wm: WindowManager? = null
        private var instance: AssistTrigger? = null
        private lateinit var lp: WindowManager.LayoutParams

        fun show(ctx: Context) {
            if (instance != null) return
            val c = ctx.applicationContext
            wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val sizePx = dp(c, StateBridge.triggerSize)
            lp = WindowManager.LayoutParams(
                sizePx, sizePx,
                if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = StateBridge.triggerX
                y = StateBridge.triggerY
            }

            val v = AssistTrigger(c)
            try { wm?.addView(v, lp); instance = v } catch (_: Throwable) {}
        }

        fun hide() {
            val v = instance ?: return
            try { wm?.removeView(v) } catch (_: Throwable) {}
            instance = null
        }

        fun toggle(ctx: Context) {
            if (instance == null) show(ctx) else hide()
        }

        fun isVisible(): Boolean = instance != null

        fun refresh(ctx: Context) {
            if (instance != null) { hide(); show(ctx) }
        }

        private fun dp(c: Context, v: Int): Int =
            (v * c.resources.displayMetrics.density).toInt()
    }
}
