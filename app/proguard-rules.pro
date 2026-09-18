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
