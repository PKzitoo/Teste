package com.ffkit.lite

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager

/**
 * Bridge que a MainActivity usa pra pedir MediaProjection permission
 * ao usuario e, apos aprovado, iniciar o CvService + overlay.
 */
object CvBridge {
    const val REQ_PROJECTION = 9988
    var running = false

    fun requestProjection(ctx: Context) {
        val i = Intent(ctx, ProjectionActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
    }

    fun onProjectionResult(ctx: Context, code: Int, data: Intent?) {
        if (data == null || code != Activity.RESULT_OK) return
        CvService.start(ctx, code, data)
        CvOverlay.show(ctx)
        CvAimLoop.start(ctx)
        running = true
    }

    fun stop(ctx: Context) {
        CvAimLoop.stop()
        CvOverlay.hide(ctx)
        CvService.stop(ctx)
        running = false
    }
}
