# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class okb.onboarding.register.RegisterHomeFragment$JavaScriptInterface {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# These are used by various libs and tools
-keepattributes *Annotation*

# If you are using custom exceptions, add this line so that custom exception types are skipped during obfuscation:
-keep public class * extends java.lang.Exception

## Crashlytics
# https://docs.fabric.io/android/crashlytics/dex-and-proguard.html
# For Fabric to properly de-obfuscate your crash reports, you need to remove this line from your configuration file, or we won’t be able to automatically upload your mapping file:
# -printmapping mapping.txt
# To skip running ProGuard on Crashlytics, just add the following to your ProGuard config file.
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-dontnote com.crashlytics.**
# avoid notes, probably can be removed
-keep class io.fabric.sdk.android.** { *; }

## Retrifit
# https://stackoverflow.com/questions/41135913/android-proguard-and-retrofit-2
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod
# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**
# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit
# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions

# OkHttp
# https://github.com/square/okhttp/issues/3922
-dontwarn okhttp3.internal.platform.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform.**
# avoid notes, probably can be removed
-dontnote okhttp3.internal.platform.*

## Googe Services
# https://stackoverflow.com/questions/38217024/proguard-error-trying-to-generate-signed-apk
-dontwarn com.google.android.gms.**
# https://stackoverflow.com/a/33048929/7172273
-dontwarn com.google.android.gms.internal.zzhu
# avoid notes, probably can be removed
-dontnote com.google.android.gms.**

# Firebase
# https://stackoverflow.com/questions/50015961/com-google-firebase-messaging-zza-cant-find-referenced-class-android-graphics
-dontwarn com.google.firebase.messaging.**
# avoid notes, probably can be removed
-dontnote com.google.firebase.iid.FirebaseInstanceId$zza

## Kotlin
# https://stackoverflow.com/questions/52689607/is-it-safe-to-ignore-these-proguard-notes-for-kotlin
-keep class kotlin.Metadata { *; }
-dontnote kotlin.internal.PlatformImplementationsKt
-dontnote kotlin.reflect.jvm.internal.**
# avoid notes for classes with default fields in constructors: "blah blah blah but not the descriptor class 'kotlin.jvm.internal.DefaultConstructorMarker'"
-keep class kotlin.jvm.internal.DefaultConstructorMarker { *; }

## Gson
## https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
###---------------Begin: proguard configuration for Gson  ----------
## Gson specific classes
#-dontwarn sun.misc.**
##-keep class com.google.gson.stream.** { *; }
## Application classes that will be serialized/deserialized over Gson
#-keep class com.google.gson.examples.android.model.** { *; }
## Prevent proguard from stripping interface information from TypeAdapterFactory,
## JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
#-keep class * implements com.google.gson.TypeAdapterFactory
#-keep class * implements com.google.gson.JsonSerializer
#-keep class * implements com.google.gson.JsonDeserializer
###---------------End: proguard configuration for Gson  ----------
### Entities
#-keepclassmembers class ** {
#  @com.google.gson.annotations.SerializedName *;
#}
## avoid notes, probably can be removed
#-dontnote com.google.gson.internal.UnsafeAllocator

## Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# avoid notes, probably can be removed
-dontnote com.bumptech.glide.**

# avoid random notes
#-keep class com.google.android.material.** { *; }
-dontnote com.google.android.material.**
-dontnote android.widget.Space

###############################
### Code ######################
###############################
