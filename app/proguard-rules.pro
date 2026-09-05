# Keep native methods and their classes
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep project models and data classes
-keep class io.github.abapqlcm.auroravpn.model.** { *; }
-keep class io.github.abapqlcm.auroravpn.data.** { *; }

# Absolutely keep the JNI bridge class and all its members
-keep class io.github.abapqlcm.auroravpn.core.HevTun2SocksNative {
    <methods>;
    <fields>;
}

# General networking and serialization stability
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn okhttp3.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-keep class io.github.abapqlcm.auroravpn.** { *; }

-keep public class * extends android.app.Application {
    <init>();
    void onCreate();
}

-keep public class * extends android.app.Activity {
    <init>();
    void onCreate(android.os.Bundle);
}
