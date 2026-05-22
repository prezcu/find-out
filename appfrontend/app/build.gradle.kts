plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.secrets)
}

android {
    namespace = "dev.andrei.app_frontend"
    compileSdk  = 36
    defaultConfig {
        applicationId = "dev.andrei.app_frontend"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // loopback for android emulator
        // BACKEND_BASE_URL is injected by the secrets-gradle-plugin from local.properties
        buildConfigField("String", "EMULATOR_BASE_URL", "\"http://10.0.2.2:8080/\"")
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // retrofit for networking
    implementation(libs.retrofit)
    // Gson converter to translate JSON into Kotlin Data Classes
    implementation(libs.converter.gson)
    // ViewModel integration for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    // Hilt Core
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // Hilt for Jetpack Compose
    implementation(libs.androidx.hilt.navigation.compose)
    // Google Play service - location
    implementation(libs.google.play.services.location)
    // Jetpack Navigation for Compose
    implementation(libs.androidx.navigation.compose)
    // OkHttp (for the auth interceptor + Retrofit client)
    implementation(libs.okhttp)
    // Encrypted SharedPreferences for storing JWT
    implementation(libs.androidx.security.crypto)
}