# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-injars 'C:\Users\WangXingxing\Desktop\jar\plugin.jar'
-outjars 'C:\Users\WangXingxing\Desktop\jar\plugin_guard.jar'
-libraryjars 'D:\android-sdk\platforms\android-19\android.jar'
#-libraryjars 'D:\Users\Administrator\workspace-EE\CloudTV_Plug_New\libs\sun.misc.BASE64Decoder.jar'
#-libraryjars 'D:\Users\Administrator\workspace-EE\CloudTV_Plug_New\libs\p2ptimeshift.jar'
#-libraryjars 'D:\Program Files\android-sdk-windows\platforms\android-23\android.jar'

-target 1.6
-optimizations !class/merging/*,!field/*,!code/simplification/arithmetic
-optimizationpasses 5
-useuniqueclassmembernames
-keepattributes *Annotation*
-renamesourcefileattribute SourceFile
-adaptresourcefilenames **.properties
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF
-verbose


-keep public class com.cloudmedia.tv.plug.ParserURLUtils {
    public <fields>;
    public <methods>;
}
-keep public class com.cloudmedia.tv.plug.ParserEpgUtils {
    public <fields>;
    public <methods>;
}
-keep public class com.alibaba.video.hook {
    public <fields>;
    public <methods>;
    native <methods>;
}
-keep public class com.alibaba.wireless.security.jaq.SecuritySign  {
    public <fields>;
    public <methods>;
    native <methods>;
}
-keep public class com.wasu.** {
   *;
}
-keep public class com.hdp.SimpleHttp  {
    public <fields>;
    public <methods>;
    native <methods>;
}
-keep public class com.extremep2p.sdk.SdkApi  {
    public <fields>;
    public <methods>;
    native <methods>;
}
#-libraryjars libs/p2ptimeshift.jar
-keep class com.p2ptimeshift.** {*;}
-keep class cn.vbyte.p2p.** {*;}
-keep public class android.apk.ActivityThread {
    *;
}
-keep public class com.cntv.cbox.player.core.CBoxP2PCore {
    *;
}
-keep interface com.cntv.cbox.player.core.OnP2PEventListener{
    *;
}
-keep public class * extends android.app.Activity

-keep public class * extends android.app.Application

-keep public class * extends android.app.Service

-keep public class * extends android.content.BroadcastReceiver

-keep public class * extends android.content.ContentProvider

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keepclassmembers class * extends android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

-keep public class com.anlytics.plug.ParserUtils
