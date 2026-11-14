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

    fun requestProjection(activity: Activity) {
        val mpm = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE)
            as MediaProjectionManager
        activity.startActivityForResult(mpm.createScreenCaptureIntent(), REQ_PROJECTION)
    }

    fun onProjectionResult(ctx: Context, code: Int, data: Intent?) {
        if (data == null || code != Activity.RESULT_OK) return
        CvService.start(ctx, code, data)
        CvOverlay.show(ctx)
        running = true
    }

    fun stop(ctx: Context) {
        CvOverlay.hide(ctx)
        CvService.stop(ctx)
        running = false
    }
}
