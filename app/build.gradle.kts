@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("androidx.room") version "2.6.1" // Add this line for Room
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
}

android {
    namespace = "com.adista.destour_middle"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.adista.destour_middle"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true // Enable View Binding
        dataBinding = true // Enable Data Binding
    }

    // Correctly set up Room schema directory
    room {
        schemaDirectory("$projectDir/schemas") // This line is not necessary if already set in kapt
    }
}

dependencies {
    // AndroidX Core Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.androidcoreproject.v300beta7)

    // Material Design Components
    implementation(libs.material)

    // Lifecycle Components
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v251)
    implementation(libs.androidx.lifecycle.livedata.ktx.v251)

    // RecyclerView Support
    implementation(libs.androidx.recyclerview)

    // Room Database Components
    implementation(libs.androidx.room.runtime.v250)
    implementation(libs.room.ktx)
    implementation(libs.play.services.analytics.impl)
    kapt(libs.room.compiler)

    // Hilt Dependency Injection
    implementation(libs.hilt.android.v252)
    kapt(libs.hilt.android.compiler)

    // Hilt viewModel specific dependencies (if needed)
    implementation(libs.androidx.hilt.navigation.fragment)

    // Firebase Analytics and Crashlytics
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Data Binding Runtime
    implementation(libs.androidx.databinding.runtime)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.coreCrocodic)

    // Other Libraries (e.g., Glide, SDP, SSP)
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)


}

kapt {
    correctErrorTypes = true
}