package com.ffkit.lite
import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent

class PanicService : AccessibilityService() {
    private val hits = ArrayDeque<Long>()
    override fun onKeyEvent(e: KeyEvent): Boolean {
        if (e.action != KeyEvent.ACTION_DOWN) return false
        if (e.keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) return false
        val now = System.currentTimeMillis()
        hits.addLast(now)
        while (hits.first() < now - 2000) hits.removeFirst()
        if (hits.size >= 3) { hits.clear(); NativeBridge.panic() }
        return false
    }
    override fun onInterrupt() {}
    override fun onAccessibilityEvent(ev: android.view.accessibility.AccessibilityEvent?) {}
}
