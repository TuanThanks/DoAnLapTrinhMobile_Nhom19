plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.appandroid"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.appandroid"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("io.github.jan-tennert.supabase:bom:2.1.0"))
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")

// ------------------------
// Ktor
// ------------------------
    implementation("io.ktor:ktor-client-android:2.3.8")

// ------------------------
// JSON
// ------------------------
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

// ------------------------
// Google Auth (chọn 1 bản mới nhất)
// ------------------------
    implementation("com.google.android.gms:play-services-auth:21.4.0")

// ------------------------
// Navigation Compose
// ------------------------
    implementation("androidx.navigation:navigation-compose:2.8.0")

// ------------------------
// Coil
// ------------------------
    implementation("io.coil-kt:coil-compose:2.5.0")

// ------------------------
// Konfetti
// ------------------------
    implementation("nl.dionsegijn:konfetti-compose:2.0.4")

// ------------------------
// Hilt Navigation
// ------------------------
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// ------------------------
// Core AndroidX libs
// ------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

// Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation("androidx.compose.material:material-icons-extended")

// Logging
    implementation("org.slf4j:slf4j-simple:2.0.13")

// TEST
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

// DEBUG
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}