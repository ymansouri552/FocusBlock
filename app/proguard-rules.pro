# FocusBlock ProGuard / R8 rules (release minification)

# Keep Room entity fields
-keep @androidx.room.Entity class * { *; }

# Compose / Kotlin annotations
-dontwarn org.jetbrains.annotations.**
