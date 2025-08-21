plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")     // KSP instead of kapt
}

android {
    namespace = "com.example.buyme"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.buyme"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Java + Kotlin 17 everywhere
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug { isMinifyEnabled = false }
    }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Room (with KSP)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Material Components (for XML themes like Theme.Material3.*)
    implementation("com.google.android.material:material:1.12.0")

    // Unit tests
    testImplementation("junit:junit:4.13.2")

// Instrumented (Android) tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.room:room-ktx:2.6.1")
}

// Room build args via KSP (optional)
ksp {
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
    // keep default verifySchema (true). If you ever hit schema verification issues, you can disable:
    // arg("room.verifySchema", "false")
}
