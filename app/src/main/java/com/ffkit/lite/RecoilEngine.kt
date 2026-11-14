package com.ffkit.lite

import android.util.Log
import kotlin.math.max
import kotlin.math.sin

/**
 * Motor de compensacao de recuo. Roda enquanto o botao ASSIST esta
 * segurado OU quando alwaysOn=true.
 *
 * Usa /system/bin/input direto (full path) via Shizuku shell —
 * alguns shells nao tem `input` no PATH imediato.
 */
object RecoilEngine {
    private const val TAG = "ffkit-recoil"
    @Volatile private var running = false
    private var worker: Thread? = null
    private var tickCount = 0
    var swipesSent = 0; private set
    var lastError: String = ""; private set

    fun start() {
        if (running) return
        running = true
        tickCount = 0
        swipesSent = 0
        worker = Thread {
            runLoop()
        }.apply { isDaemon = true; name = "ffkit-recoil"; start() }
    }

    fun stop() {
        running = false
        try { worker?.interrupt() } catch (_: Throwable) {}
        worker = null
        tickCount = 0
    }

    val isActive: Boolean get() = running

    /**
     * Swipe DIAGNOSTICO visivel. Da pra confirmar se o Shizuku esta
     * de fato enviando input. Se o FF/tela mexer ao tocar o botao
     * TEST, Shizuku ta OK — o problema eh so coord/forca/intervalo.
     */
    fun testOnce() {
        val cx = StateBridge.lookPadX
        val cy = StateBridge.lookPadY
        val cmd = "/system/bin/input swipe $cx $cy $cx ${cy + 120} 200"
        val ok = ShizukuBridge.send(cmd)
        lastError = if (ok) "TEST swipe enviado em ($cx,$cy)+120"
                    else "TEST FAILED: Shizuku off ou shell morto"
        Log.d(TAG, lastError)
    }

    private fun runLoop() {
        while (running) {
            try { tick() } catch (t: Throwable) {
                lastError = "tick err: ${t.message}"
                Log.e(TAG, lastError)
            }
            val preset = currentPreset()
            val sleep = max(10, preset.intervalMs)
            try { Thread.sleep(sleep.toLong()) }
            catch (_: InterruptedException) { break }
        }
    }

    private fun tick() {
        val p = currentPreset()
        if (p.pullY <= 0f && p.pullX == 0f) return

        val mult = if (tickCount < p.burstLen) p.burstMult else 1f
        val jY = 1f + (Math.random().toFloat() - 0.5f) * 0.3f
        val jX = 1f + (Math.random().toFloat() - 0.5f) * 0.3f
        val gain = StateBridge.recoilIntensity.coerceIn(0.3f, 2.0f)

        val dy = (p.pullY * mult * gain * jY).toInt()
        val dx = (p.pullX * mult * gain * jX).toInt()
        val wave = (sin(tickCount * 0.6) * 1.5).toInt()
        val dxFinal = dx + wave

        val cx = StateBridge.lookPadX
        val cy = StateBridge.lookPadY
        val dur = StateBridge.swipeDurationMs.coerceIn(20, 200)

        // usa /system/bin/input (full path) pra evitar PATH issues
        val cmd = "/system/bin/input swipe $cx $cy ${cx + dxFinal} ${cy + dy} $dur"
        val ok = ShizukuBridge.send(cmd)
        if (ok) swipesSent++
        else lastError = "swipe send failed at tick $tickCount"

        tickCount++
    }

    private fun currentPreset(): WeaponPreset =
        WeaponPresets.byIndex(StateBridge.weaponIdx)
}
