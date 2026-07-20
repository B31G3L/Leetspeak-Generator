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

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Gson nutzt Reflection über TypeToken<...>() {} (anonyme Subklassen) für generische
# Typen (siehe LeetManager.loadLeets / HistoryPreferences.parse). Ohne diese Regeln
# kann R8 die generische Signatur der anonymen TypeToken-Subklassen wegoptimieren,
# wodurch Gson zur Laufzeit den falschen (Raw-)Typ deserialisiert.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Modelle, die per Gson (de)serialisiert werden, müssen ihre Feldnamen behalten,
# da Gson standardmäßig über Reflection nach den Java/Kotlin-Feldnamen matcht
# (kein @SerializedName im Einsatz). Ohne -keep würden Felder beim Minify
# umbenannt und bereits gespeichertes JSON ließe sich nach einem Update nicht
# mehr korrekt einlesen (stille Datenverluste bei Custom Leets / Verlauf).

# CustomLeet (LeetManager: Liste der Custom Leets, "LeetSpeakProfiles" Prefs)
-keep class com.beigel.leetSpeak_Generator.data.CustomLeet { *; }
-keepclassmembers class com.beigel.leetSpeak_Generator.data.CustomLeet { *; }

# HistoryEntry (HistoryPreferences: Übersetzungsverlauf, DataStore)
-keep class com.beigel.leetSpeak_Generator.data.HistoryEntry { *; }
-keepclassmembers class com.beigel.leetSpeak_Generator.data.HistoryEntry { *; }

# Gson selbst nutzt intern sun.misc.Unsafe für die Instanziierung ohne No-Arg-
# Konstruktor; R8 warnt darüber, das ist aber unbedenklich.
-dontwarn sun.misc.Unsafe
-dontwarn com.google.gson.**