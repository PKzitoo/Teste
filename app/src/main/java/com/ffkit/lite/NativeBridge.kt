package com.ffkit.lite

object NativeBridge {
    @JvmStatic external fun boot(dir: String)
    @JvmStatic external fun panic()
    @JvmStatic external fun pullEnemies(): Array<FloatArray>
}
