package com.ffkit.lite

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.hypot
import kotlin.math.max

/**
 * Loop do aimbot CV. Roda numa thread dedicada enquanto CvBridge
 * esta ligado. A cada ~30ms le CvEngine.latest, escolhe o alvo
 * mais proximo do centro da tela dentro do fov, e aplica o
 * puxao de mira via GestureEngine.
 *
 * Nao requer injecao no FF — usa Accessibility pra tocar/arrastar.
 * Os parametros (forca, fov, suavidade, stickiness, miss rate)
 * sao controlados por StateBridge.aim* (salvos em state.json).
 */
object CvAimLoop {
    private var thread: Thread? = null
    @Volatile private var running = false
    private var screenW = 1080
    private var screenH = 2400

    // state interno do tracking
    private var lastTarget: CvEngine.Target? = null
    private var stickyUntil = 0L

    fun start(ctx: Context) {
        if (running) return
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics().also { wm.defaultDisplay.getRealMetrics(it) }
        screenW = dm.widthPixels; screenH = dm.heightPixels
        running = true
        thread = Thread {
            while (running) {
                try { tick() } catch (_: Throwable) {}
                try { Thread.sleep(30) } catch (_: InterruptedException) { break }
            }
        }.apply { isDaemon = true; start() }
    }

    fun stop() {
        running = false
        thread?.interrupt(); thread = null
        lastTarget = null; stickyUntil = 0L
    }

    private fun tick() {
        if (!StateBridge.aimEnabled) return
        val targets = CvEngine.latest.get()
        if (targets.isEmpty()) { lastTarget = null; return }

        // CvEngine usa frame em metade da resolucao -> escala pra tela real
        val sx = screenW / (screenW / 2f)
        val sy = screenH / (screenH / 2f)

        val cx = screenW / 2f
        val cy = screenH / 2f
        val fov = StateBridge.aimFov

        // pick: ou mantem o alvo atual se ainda esta valido (stickiness)
        // ou escolhe o mais proximo do centro
        val now = System.currentTimeMillis()
        val keep = lastTarget != null && now < stickyUntil &&
                   targets.any { sameTarget(it, lastTarget!!) }
        val target = if (keep) targets.first { sameTarget(it, lastTarget!!) }
                     else chooseClosest(targets, cx, cy, sx, sy, fov) ?: return
        lastTarget = target
        if (!keep) stickyUntil = now + StateBridge.aimStickyMs

        // bone selection: respeita aim_bone / aim_bone_random
        val point = when {
            StateBridge.aimBoneRandom -> {
                val roll = ((now xor target.hashCode().toLong()) and 0xFF).toInt()
                when {
                    roll < 60  -> target.head
                    roll < 140 -> ((target.head.first + target.chest.first) / 2f to
                                   (target.head.second + target.chest.second) / 2f)
                    else       -> target.chest
                }
            }
            StateBridge.aimBone == 0 -> target.head
            StateBridge.aimBone == 1 -> ((target.head.first + target.chest.first) / 2f to
                                         (target.head.second + target.chest.second) / 2f)
            else                      -> target.chest
        }

        var tx = point.first * sx
        var ty = point.second * sy

        // miss rate: humaniza baixando accuracy de propósito
        if (Math.random().toFloat() < StateBridge.aimMissRate) {
            val off = 30f + (Math.random().toFloat() * 60f)
            val ang = Math.random().toFloat() * 6.283f
            tx += (kotlin.math.cos(ang) * off)
            ty += (kotlin.math.sin(ang) * off)
        }

        // força: 1-10. 1 = pull minimo, 10 = snap quase direto.
        val strength = StateBridge.aimSmooth.coerceIn(1f, 10f)
        val factor = strength / 10f
        val dx = (tx - cx) * factor
        val dy = (ty - cy) * factor
        val fromX = cx; val fromY = cy
        val toX = cx + dx; val toY = cy + dy

        // atraso de reacao ao MUDAR de alvo
        if (!keep && (now - stickyUntil + StateBridge.aimStickyMs) < StateBridge.aimReactionMs) return

        // dispatch drag curto — simula o dedo deslizando pro alvo
        GestureEngine.drag(fromX, fromY, toX, toY, durMs = 40)
    }

    private fun sameTarget(a: CvEngine.Target, b: CvEngine.Target): Boolean {
        val d = hypot(a.chest.first - b.chest.first, a.chest.second - b.chest.second)
        return d < 80f  // ~80px no frame downscaled = mesmo alvo
    }

    private fun chooseClosest(
        targets: List<CvEngine.Target>, cx: Float, cy: Float,
        sx: Float, sy: Float, fov: Float
    ): CvEngine.Target? {
        var best: CvEngine.Target? = null
        var bestD = fov
        for (t in targets) {
            val px = t.head.first * sx
            val py = t.head.second * sy
            val d = hypot(px - cx, py - cy)
            if (d < bestD) { bestD = d; best = t }
        }
        return best
    }
}
