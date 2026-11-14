package com.ffkit.lite
import android.content.Context
import java.io.File

object NativeExtractor {
    fun ensure(ctx: Context, name: String): File {
        val out = File(ctx.filesDir, name)
        if (!out.exists()) {
            ctx.assets.open("arm64-v8a/$name").use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
            out.setExecutable(true)
        }
        return out
    }
}
