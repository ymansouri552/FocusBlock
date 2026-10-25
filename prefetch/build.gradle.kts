// Standalone helper module: downloads every artifact the FocusBlock build needs
// into the local Gradle cache. Run on a fast connection:
//
//   .\gradlew.bat :prefetch:prefetch :prefetch:prefetchRisky
//
// Then copy %USERPROFILE%\.gradle\caches\modules-2 to the slow machine.

val prefetchDeps by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val prefetchRiskyDeps by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val pluginDeps by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    // --- Build plugins (marker artifacts; transitives pulled in automatically) ---
    pluginDeps("com.android.application:com.android.application.gradle.plugin:8.10.1")
    pluginDeps("org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:1.9.24")
    pluginDeps("com.google.dagger.hilt.android:com.google.dagger.hilt.android.gradle.plugin:2.52")

    // Compose compiler embeddable (used via kotlinCompilerExtensionVersion = 1.6.11)
    prefetchDeps("org.jetbrains.kotlin:kotlin-compose-compiler-plugin-embeddable:1.6.11")

    // --- App dependencies (mirrors app/build.gradle.kts) ---
    prefetchDeps(platform("androidx.compose:compose-bom:2024.10.00"))
    prefetchDeps("androidx.compose.ui:ui")
    prefetchDeps("androidx.compose.ui:ui-graphics")
    prefetchDeps("androidx.compose.foundation:foundation")
    prefetchDeps("androidx.compose.material3:material3")
    prefetchDeps("androidx.compose.material:material-icons-extended")
    prefetchDeps("androidx.compose.ui:ui-tooling")
    prefetchDeps("androidx.compose.ui:ui-tooling-preview")
    prefetchDeps("androidx.activity:activity-compose:1.9.3")
    prefetchDeps("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    prefetchDeps("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    prefetchDeps("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    prefetchDeps("androidx.core:core-ktx:1.13.1")
    prefetchDeps("androidx.navigation:navigation-compose:2.8.3")
    prefetchDeps("androidx.hilt:hilt-navigation-compose:1.2.0")
    prefetchDeps("io.coil-kt.coil:coil-compose:2.7.0")
    prefetchDeps("com.google.dagger:hilt-android:2.52")
    prefetchDeps("androidx.room:room-runtime:2.6.1")
    prefetchDeps("androidx.room:room-ktx:2.6.1")
    prefetchDeps("androidx.datastore:datastore-preferences:1.1.1")
    prefetchDeps("androidx.datastore:datastore-core:1.1.1")
    prefetchDeps("androidx.work:work-runtime-ktx:2.10.0")
    prefetchDeps("androidx.preference:preference-ktx:1.3.1")
    prefetchDeps("androidx.startup:startup-runtime:1.1.1")
    prefetchDeps("com.google.android.material:material:1.12.0")

    // kapt / annotation processors
    prefetchDeps("com.google.dagger:hilt-android-compiler:2.52")
    prefetchDeps("androidx.room:room-compiler:2.6.1")

    // --- Unit test dependencies ---
    prefetchDeps("junit:junit:4.13.2")
    prefetchDeps("org.robolectric:robolectric:4.11.1")
    prefetchRiskyDeps("org.robolectric:android-all-instrumented:14-robolectric-10818077-i7")
    prefetchDeps("androidx.test:core:1.6.1")
    prefetchDeps("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    prefetchDeps("org.jetbrains.kotlin:kotlin-test-junit:1.9.24")
    prefetchDeps("org.jetbrains.kotlin:kotlin-test:1.9.24")

    // --- Instrumented test dependencies ---
    prefetchDeps("androidx.test.espresso:espresso-core:3.6.1")
    prefetchDeps("androidx.test:rules:1.6.1")
}

tasks.register<Copy>("prefetch") {
    from(prefetchDeps)
    from(pluginDeps)
    into(layout.buildDirectory.dir("prefetched"))
    // Forcing configuration resolution is what downloads artifacts + their POMs
    // into the Gradle cache; the Copy output is not actually needed.
    outputs.upToDateWhen { false }
}

tasks.register<Copy>("prefetchRisky") {
    from(prefetchRiskyDeps)
    into(layout.buildDirectory.dir("prefetched-risky"))
    outputs.upToDateWhen { false }
}
