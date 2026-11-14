package com.ffkit.lite

import kotlin.math.max
import kotlin.math.sin

/**
 * Motor de compensacao de recuo. Roda enquanto o botao ASSIST esta
 * segurado. Thread dedicada; para quando o flag [running] vira false.
 *
 * A cada tick:
 *   • le o preset ativo em StateBridge
 *   • calcula o delta (puxada vertical + horizontal), aplicando
 *     burst multiplier nos primeiros N ticks
 *   • adiciona jitter leve (nao fica identico toda vez — anti-pattern)
 *   • manda `input swipe` via Shizuku, partindo do centro do "look pad"
 *     configurado pelo usuario
 *
 * O FF entende input vindos do shell como se fossem toques reais do
 * usuario, porque vem do nivel do sistema. Nao eh detectavel como
 * "cheat memoria" nem como "screen capture" — eh igual a um controle
 * de teclado fisico.
 */
object RecoilEngine {
    @Volatile private var running = false
    private var worker: Thread? = null
    private var tickCount = 0

    fun start() {
        if (running) return
        running = true
        tickCount = 0
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

    private fun runLoop() {
        while (running) {
            try { tick() } catch (_: Throwable) {}
            val preset = currentPreset()
            val sleep = max(10, preset.intervalMs)
            try { Thread.sleep(sleep.toLong()) }
            catch (_: InterruptedException) { break }
        }
    }

    private fun tick() {
        val p = currentPreset()
        if (p.pullY <= 0f && p.pullX == 0f) return  // AWM / shotgun

        // burst multiplier nos primeiros N ticks
        val mult = if (tickCount < p.burstLen) p.burstMult else 1f

        // jitter: +/- 15% random — anti-pattern recognition
        val jY = 1f + (Math.random().toFloat() - 0.5f) * 0.3f
        val jX = 1f + (Math.random().toFloat() - 0.5f) * 0.3f

        // intensidade global (0.5x..1.5x) que o user ajusta no painel
        val gain = StateBridge.recoilIntensity.coerceIn(0.3f, 2.0f)

        val dy = (p.pullY * mult * gain * jY).toInt()
        val dx = (p.pullX * mult * gain * jX).toInt()

        // onda senoidal leve pra deixar o X menos linear (nao eh linha reta)
        val wave = (sin(tickCount * 0.6) * 1.5).toInt()
        val dxFinal = dx + wave

        val cx = StateBridge.lookPadX
        val cy = StateBridge.lookPadY
        // movimento curto, duracao curta (evita "swipe de arrasto")
        ShizukuBridge.swipe(cx, cy, cx + dxFinal, cy + dy, 24)

        tickCount++
    }

    private fun currentPreset(): WeaponPreset =
        WeaponPresets.byIndex(StateBridge.weaponIdx)
}
