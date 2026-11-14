package de.robv.android.xposed

@Suppress("UNUSED_PARAMETER")
object XposedHelpers {
    @JvmStatic fun findClass(className: String, classLoader: ClassLoader?): Class<*> =
        Class.forName(className, true, classLoader ?: ClassLoader.getSystemClassLoader())

    @JvmStatic fun findAndHookMethod(
        clazz: Class<*>,
        methodName: String,
        vararg parameterTypesAndCallback: Any
    ): Any? {
        // stub — em runtime o VirtualXposed substitui esta classe inteira
        // pela implementacao real. Esta versao nao hooka nada.
        return null
    }
}
