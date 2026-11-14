package com.ffkit.lite

import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.atomic.AtomicReference

/**
 * CV engine: recebe bitmaps, roda ML Kit pose-detection,
 * publica deteccoes pro overlay e pro aimbot.
 */
object CvEngine {
    data class Target(val box: RectF, val head: Pair<Float, Float>,
                      val chest: Pair<Float, Float>, val conf: Float)

    private val detector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
            .build()
    )

    val latest = AtomicReference<List<Target>>(emptyList())

    fun submit(bitmap: Bitmap, rotation: Int) {
        val img = InputImage.fromBitmap(bitmap, rotation)
        detector.process(img)
            .addOnSuccessListener { pose -> latest.set(listOf(poseToTarget(pose)).filterNotNull()) }
            .addOnFailureListener { latest.set(emptyList()) }
    }

    private fun poseToTarget(pose: Pose): Target? {
        val nose    = pose.getPoseLandmark(PoseLandmark.NOSE) ?: return null
        val lSh     = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rSh     = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val lHip    = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rHip    = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        if (nose.inFrameLikelihood < 0.4f) return null
        val xs = listOfNotNull(nose, lSh, rSh, lHip, rHip).map { it.position.x }
        val ys = listOfNotNull(nose, lSh, rSh, lHip, rHip).map { it.position.y }
        val box = RectF(xs.min(), ys.min(), xs.max(), ys.max())
        val chestX = ((lSh?.position?.x ?: nose.position.x) + (rSh?.position?.x ?: nose.position.x)) / 2
        val chestY = ((lSh?.position?.y ?: nose.position.y) + (rSh?.position?.y ?: nose.position.y)) / 2
        val conf = nose.inFrameLikelihood
        return Target(box, nose.position.x to nose.position.y, chestX to chestY, conf)
    }
}
