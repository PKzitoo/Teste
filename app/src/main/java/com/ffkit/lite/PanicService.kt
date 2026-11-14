package com.ffkit.lite

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * Acessibilidade: escuta teclas de volume globais.
 *  VOL- 3x em 2s  = PANIC (desliga cheat + esconde overlay)
 *  VOL+ 3x em 2s  = toggle bolha (esconde se tiver aberta, mostra se escondida)
 */
class PanicService : AccessibilityService() {
    private val downHits = ArrayDeque<Long>()
    private val upHits   = ArrayDeque<Long>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        GestureEngine.service.set(this)
        // runtime: habilita filter key events (o XML sozinho NAO basta
        // em alguns Android, precisa setServiceInfo com a flag)
        val info = serviceInfo
        info.flags = info.flags or
            AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
            AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
    }

    override fun onKeyEvent(e: KeyEvent): Boolean {
        if (e.action != KeyEvent.ACTION_DOWN) return false
        val now = System.currentTimeMillis()
        when (e.keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                downHits.addLast(now)
                while (downHits.isNotEmpty() && downHits.first() < now - 2000) downHits.removeFirst()
                if (downHits.size >= 3) {
                    downHits.clear()
                    StateBridge.panic()
                    OverlayController.hide()
                }
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                upHits.addLast(now)
                while (upHits.isNotEmpty() && upHits.first() < now - 2000) upHits.removeFirst()
                if (upHits.size >= 3) {
                    upHits.clear()
                    OverlayController.ensure(this)
                    OverlayController.toggle()
                }
            }
        }
        return false
    }
    override fun onInterrupt() {}
    override fun onAccessibilityEvent(ev: android.view.accessibility.AccessibilityEvent?) {}
}
