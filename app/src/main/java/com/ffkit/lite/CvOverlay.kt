package com.ffkit.lite

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.Choreographer
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * Overlay transparente que desenha as deteccoes do CvEngine
 * em cima do FF. Separado do painel neon.
 */
@SuppressLint("ViewConstructor")
class CvOverlay(ctx: Context) : View(ctx) {
    private val boxPaint = Paint().apply {
        color = 0xFF00FFB4.toInt(); style = Paint.Style.STROKE
        strokeWidth = 4f; isAntiAlias = true
    }
    private val headPaint = Paint().apply {
        color = 0xFFFF2BD6.toInt(); style = Paint.Style.FILL; isAntiAlias = true
    }
    private val skelPaint = Paint().apply {
        color = 0xFF00E7FF.toInt(); style = Paint.Style.STROKE
        strokeWidth = 3f; isAntiAlias = true
    }

    init {
        Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                invalidate()
                Choreographer.getInstance().postFrameCallback(this)
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        val list = CvEngine.latest.get()
        val sx = width.toFloat() / (width / 2f)   // compensa o /2 do CvService
        val sy = height.toFloat() / (height / 2f)
        for (t in list) {
            canvas.drawRect(
                t.box.left * sx, t.box.top * sy,
                t.box.right * sx, t.box.bottom * sy, boxPaint
            )
            canvas.drawCircle(t.head.first * sx, t.head.second * sy, 10f, headPaint)
        }
    }

    companion object {
        private var view: CvOverlay? = null
        fun show(ctx: Context) {
            if (view != null) return
            val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val v = CvOverlay(ctx)
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            lp.gravity = Gravity.TOP or Gravity.START
            wm.addView(v, lp); view = v
        }
        fun hide(ctx: Context) {
            val v = view ?: return
            (ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(v)
            view = null
        }
    }
}
