package de.robv.android.xposed

open class XC_MethodHook {
    open class MethodHookParam {
        @JvmField var thisObject: Any? = null
        @JvmField var args: Array<Any?>? = null
        @JvmField var method: java.lang.reflect.Member? = null
        @JvmField var result: Any? = null
        @JvmField var throwable: Throwable? = null
    }
    open fun beforeHookedMethod(param: MethodHookParam) {}
    open fun afterHookedMethod(param: MethodHookParam) {}
}
