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

# ==========================
# event bus proguard start

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# event bus proguard end
# ==========================

# ==========================
# greenDAO 3 proguard start
### greenDAO 3
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties

# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
-dontwarn rx.**

# greenDAO 3 proguard end
# ==========================