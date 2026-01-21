plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Firebase plugin (google-services)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.funfriday"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.funfriday"
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

    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ---------------------------
    // Firebase Dependencies
    // ---------------------------

    // Firebase BOM — manages Firebase versions automatically
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase Auth (Email/Password)
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firestore for saving user profile and lunch cards
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Coroutines for Firebase await()
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation(libs.androidx.ui)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.gson)

    // ---------------------------
    // Testing
    // ---------------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // <<< ADD THIS: Firebase Storage KTX >>>
    implementation("com.google.firebase:firebase-storage-ktx")


    implementation("io.coil-kt:coil-compose:2.4.0")


}
