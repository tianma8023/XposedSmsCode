-keepclasseswithmembers class * implements de.robv.android.xposed.IXposedHookLoadPackage {
    public void handleLoadPackage(...);
}

-keep class com.github.tianma8023.xposed.smscode.utils.ModuleUtils {
    int getModuleVersion();
}