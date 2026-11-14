package com.ffkit.lite

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import java.util.concurrent.atomic.AtomicReference

/**
 * Dispatcha gestos (toques/arrastos) via o PanicService que ja
 * eh um AccessibilityService. Usado pelo aimbot pra "mover o dedo"
 * pro target detectado quando o usuario atira.
 */
object GestureEngine {
    val service = AtomicReference<AccessibilityService?>(null)

    fun tap(x: Float, y: Float, durMs: Long = 50) {
        val svc = service.get() ?: return
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, durMs)
        svc.dispatchGesture(
            GestureDescription.Builder().addStroke(stroke).build(),
            null, null
        )
    }

    fun drag(fromX: Float, fromY: Float, toX: Float, toY: Float, durMs: Long = 60) {
        val svc = service.get() ?: return
        val path = Path().apply { moveTo(fromX, fromY); lineTo(toX, toY) }
        val stroke = GestureDescription.StrokeDescription(path, 0, durMs)
        svc.dispatchGesture(
            GestureDescription.Builder().addStroke(stroke).build(),
            null, null
        )
    }
}
