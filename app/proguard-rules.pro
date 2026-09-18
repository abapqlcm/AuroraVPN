# Keep the entry points Compose and serialization need under R8 minification.

# Compose needs its runtime and the lambdas composables capture.
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# kotlinx.serialization generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep the app's own serializable models and their serializers.
-keep,includedescriptorclasses class com.auroravpn.app.**$$serializer { *; }
-keepclassmembers class com.auroravpn.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.auroravpn.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# VpnService is started by name from an Intent; R8 cannot see that reference.
-keep class com.auroravpn.app.service.AuroraVpnService { *; }

# The JNI bridge is looked up by name from native code. If R8 renames or
# removes any of these, the engine has no entry point and the tunnel cannot
# start — a failure that shows up only on a real device, as a silent crash.
-keep class com.auroravpn.app.core.NativeAetherBridge { *; }
-keep class com.auroravpn.app.core.NativeAetherBridge$* { *; }
-keepclasseswithmembers class com.auroravpn.app.core.* {
    native <methods>;
}
-keep @java.lang.FunctionalInterface class com.auroravpn.app.core.NativeSocketProtector { *; }
-keep @java.lang.FunctionalInterface class com.auroravpn.app.core.NativeEngineListener { *; }
