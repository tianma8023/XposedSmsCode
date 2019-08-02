-keepclasseswithmembers class * implements de.robv.android.xposed.IXposedHookLoadPackage {
    public void handleLoadPackage(...);
}

-keepclasseswithmembers class * implements de.robv.android.xposed.IXposedHookZygoteInit {
    public void initZygote(...);
}

-keep class com.tianma.xsmscode.common.utils.ModuleUtils {
    int getModuleVersion();
}


# ==========================
# Umeng analyze proguard start

#-keepclassmembers class * {
#   public <init> (org.json.JSONObject);
#}
#
#-keep public class com.github.tianma8023.xposed.smscode.R$*{
#public static final int *;
#}
#
#-keepclassmembers enum * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}
#
#-keep class com.umeng.** {*;}

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
### GreenDaoUpgradeHelper
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
    public static void dropTable(org.greenrobot.greendao.database.Database, boolean);
    public static void createTable(org.greenrobot.greendao.database.Database, boolean);
}
-keep class **$Properties

# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
-dontwarn rx.**

# greenDAO 3 proguard end
# ==========================


# ==========================
# bugly proguard start

#-dontwarn com.tencent.bugly.**
#-keep public class com.tencent.bugly.** {
#    *;
#}

# bugly proguard end
# ==========================


# ==========================
# jsoup proguard start
-keeppackagenames org.jsoup.nodes
# jsoup proguard end
# ==========================


# ==========================
# okhttp3 start
# okhttp3 end
# ==========================


# ==========================
# okio start
# okio end
# ==========================