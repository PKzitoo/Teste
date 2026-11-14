package de.robv.android.xposed.callbacks

class XC_LoadPackage {
    class LoadPackageParam {
        @JvmField var packageName: String = ""
        @JvmField var processName: String = ""
        @JvmField var classLoader: ClassLoader? = null
        @JvmField var appInfo: android.content.pm.ApplicationInfo? = null
        @JvmField var isFirstApplication: Boolean = false
    }
}
