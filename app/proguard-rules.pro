# FocusBlock ProGuard / R8 rules (release minification)

# Keep accessibility service classes referenced from the manifest
-keep class com.focusblock.service.accessibility.** { *; }

# Keep device admin receiver
-keep class com.focusblock.service.admin.** { *; }

# Keep broadcast receivers
-keep class com.focusblock.service.receiver.** { *; }

# Hilt / dagger generates reflective lookups
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room entities are accessed via generated code, keep the model field names
-keepclassmembers class com.focusblock.domain.model.** { *; }

# Compose / Kotlin metadata
-dontwarn org.jetbrains.annotations.**
