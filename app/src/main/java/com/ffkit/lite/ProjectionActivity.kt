package com.ffkit.lite

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity

/**
 * Activity transparente pra pedir MediaProjection a partir do overlay
 * (que roda sem Activity propria). Abre a tela de consentimento do
 * Android, recebe o result e repassa pro CvBridge.
 */
class ProjectionActivity : ComponentActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        try {
            val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(mpm.createScreenCaptureIntent(), CvBridge.REQ_PROJECTION)
        } catch (t: Throwable) {
            Log.e("ffkit-cv", "ProjectionActivity.onCreate", t)
            Toast.makeText(this, "Proj crash: ${t.javaClass.simpleName}",
                Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == CvBridge.REQ_PROJECTION) {
                CvBridge.onProjectionResult(applicationContext, resultCode, data)
            }
        } catch (t: Throwable) {
            Log.e("ffkit-cv", "onProjectionResult", t)
        } finally {
            finish()
        }
    }
}
