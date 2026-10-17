# HomeBar ProGuard/R8 rules.
# Keep line numbers for readable crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx.serialization ---
# The runtime ships consumer rules, but these keep rules make reflection-based
# serializer lookup (serializer()/companion) safe under full R8 mode.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer {
    *** descriptor;
}
-keepclasseswithmembers class ** {
    public static ** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.sorokinuladzimir.homebarassistant.net.**$$serializer { *; }
-keep,includedescriptorclasses class com.sorokinuladzimir.homebarassistant.net.**$Companion { *; }
-keep,includedescriptorclasses class com.sorokinuladzimir.homebarassistant.ai.**$$serializer { *; }
-keep,includedescriptorclasses class com.sorokinuladzimir.homebarassistant.ai.**$Companion { *; }
-keep,includedescriptorclasses class com.sorokinuladzimir.homebarassistant.db.entity.**$$serializer { *; }
-keep,includedescriptorclasses class com.sorokinuladzimir.homebarassistant.db.entity.**$Companion { *; }

# --- Retrofit ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Hilt / Dagger (mostly covered by consumer rules) ---
-dontwarn dagger.hilt.**

# --- Room ---
-keep class * extends androidx.room.RoomDatabase { <init>(); }
