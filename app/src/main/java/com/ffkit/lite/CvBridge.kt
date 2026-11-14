package com.ffkit.lite

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * Bridge que a MainActivity usa pra pedir MediaProjection permission
 * ao usuario e, apos aprovado, iniciar o CvService + overlay.
 *
 * TODA chamada sensivel vai em try/catch com Toast — se CV crashar,
 * o usuario ve o que crashou sem precisar de logcat.
 */
object CvBridge {
    private const val TAG = "ffkit-cv"
    const val REQ_PROJECTION = 9988
    var running = false

    fun requestProjection(ctx: Context) {
        try {
            val i = Intent(ctx, ProjectionActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
        } catch (t: Throwable) {
            Log.e(TAG, "requestProjection failed", t)
            toast(ctx, "CV req falhou: ${t.javaClass.simpleName}")
        }
    }

    fun onProjectionResult(ctx: Context, code: Int, data: Intent?) {
        if (data == null || code != Activity.RESULT_OK) {
            toast(ctx, "CV: permissão negada")
            return
        }
        try { CvService.start(ctx, code, data) }
        catch (t: Throwable) {
            Log.e(TAG, "CvService.start", t)
            toast(ctx, "CvService crash: ${t.javaClass.simpleName}")
            return
        }
        try { CvOverlay.show(ctx) }
        catch (t: Throwable) {
            Log.e(TAG, "CvOverlay.show", t)
            toast(ctx, "CvOverlay crash: ${t.javaClass.simpleName}")
        }
        try { CvAimLoop.start(ctx) }
        catch (t: Throwable) {
            Log.e(TAG, "CvAimLoop.start", t)
            toast(ctx, "CvAim crash: ${t.javaClass.simpleName}")
        }
        running = true
        toast(ctx, "CV ligado")
    }

    fun stop(ctx: Context) {
        try { CvAimLoop.stop() } catch (_: Throwable) {}
        try { CvOverlay.hide(ctx) } catch (_: Throwable) {}
        try { CvService.stop(ctx) } catch (_: Throwable) {}
        running = false
    }

    private fun toast(ctx: Context, msg: String) {
        try {
            android.os.Handler(ctx.mainLooper).post {
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            }
        } catch (_: Throwable) {}
    }
}
