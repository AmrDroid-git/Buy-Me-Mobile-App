plugins {
    id("com.android.application") version "8.11.1" apply false
    kotlin("android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}

// Optional: enforce JDK 17 toolchain everywhere
subprojects {
    plugins.withId("org.jetbrains.kotlin.android") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            jvmToolchain(17)
        }
    }
}
