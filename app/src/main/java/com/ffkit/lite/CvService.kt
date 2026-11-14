package com.ffkit.lite

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Captura tela via MediaProjection, decima pra 10fps,
 * joga cada frame no CvEngine. Rodara como foreground service.
 */
class CvService : Service() {
    private var mp: MediaProjection? = null
    private var vd: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var running = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotif()
        val code  = intent?.getIntExtra("code", Activity.RESULT_OK) ?: Activity.RESULT_OK
        val data  = intent?.getParcelableExtra<Intent>("data") ?: return START_NOT_STICKY
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mp = mpm.getMediaProjection(code, data) ?: return START_NOT_STICKY

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics().also { wm.defaultDisplay.getRealMetrics(it) }
        val w = dm.widthPixels / 2    // metade pra economizar
        val h = dm.heightPixels / 2
        reader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        vd = mp?.createVirtualDisplay(
            "ffkit-cv", w, h, dm.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader!!.surface, null, null
        )

        running = true
        Thread { captureLoop() }.start()
        return START_STICKY
    }

    private fun captureLoop() {
        var last = 0L
        while (running) {
            val image = reader?.acquireLatestImage()
            if (image == null) { Thread.sleep(50); continue }
            val now = System.currentTimeMillis()
            try {
                if (now - last > 100) {   // ~10fps CV
                    val plane = image.planes[0]
                    val bmp = Bitmap.createBitmap(
                        plane.rowStride / plane.pixelStride, image.height,
                        Bitmap.Config.ARGB_8888
                    )
                    bmp.copyPixelsFromBuffer(plane.buffer)
                    CvEngine.submit(bmp, 0)
                    last = now
                }
            } catch (_: Throwable) {}
            image.close()
        }
        vd?.release(); reader?.close(); mp?.stop()
    }

    override fun onDestroy() { running = false; super.onDestroy() }
    override fun onBind(i: Intent?): IBinder? = null

    private fun startForegroundNotif() {
        val id = "ffkit-cv"
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(id, "ffkit",
                NotificationManager.IMPORTANCE_MIN))
        }
        val n = Notification.Builder(this, id)
            .setContentTitle("Calculadora").setSmallIcon(R.drawable.ic_calc).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(42, n,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        else startForeground(42, n)
    }

    companion object {
        fun start(ctx: Context, code: Int, data: Intent) {
            val i = Intent(ctx, CvService::class.java)
                .putExtra("code", code).putExtra("data", data)
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i) else ctx.startService(i)
        }
        fun stop(ctx: Context) { ctx.stopService(Intent(ctx, CvService::class.java)) }
    }
}
