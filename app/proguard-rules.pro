# Keep project classes
-keep class com.prathamchikitse.** { *; }

# Keep annotations
-keepattributes *Annotation*,Signature,SourceFile,LineNumberTable,EnclosingMethod

# kotlinx serialization
-keepclassmembers class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.json.internal.** { *; }
-dontwarn kotlinx.serialization.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * { @androidx.room.PrimaryKey <fields>; }
-keep @androidx.room.TypeConverter class *
-keepclassmembers class * { @androidx.room.TypeConverter *; }

# Hilt/Dagger
-keep class dagger.hilt.** { *; }
-keepclasseswithmembers class * { @javax.inject.Inject <init>(...); }
-keepclasseswithmembers class * { @dagger.hilt.android.lifecycle.HiltViewModel <init>(...); }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# TextToSpeech (TTS) - keep UtteranceProgressListener
-keep class android.speech.tts.UtteranceProgressListener { *; }
-keepclassmembers class * extends android.speech.tts.UtteranceProgressListener { *; }

# Keep serialized names
-keepclassmembers class * { @kotlinx.serialization.SerialName <fields>; }
