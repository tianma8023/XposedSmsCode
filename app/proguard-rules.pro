-keepclasseswithmembers class * implements de.robv.android.xposed.IXposedHookLoadPackage {
    public void handleLoadPackage(...);
}

-keep class com.github.tianma8023.xposed.smscode.utils.ModuleUtils {
    int getModuleVersion();
}

# ==========================
# Umeng analyze proguard start

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keep public class com.github.tianma8023.xposed.smscode.R$*{
public static final int *;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.umeng.** {*;}

# Umeng analyze proguard end
# ==========================