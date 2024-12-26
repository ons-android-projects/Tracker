plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.onnetsolution.calldetect"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.onnetsolution.calldetect"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ViewModel and LiveData
    implementation (libs.androidx.lifecycle.viewmodel.ktx) // Ensure you're using the latest version

    // Coroutines (for StateFlow and other coroutine features)
    implementation (libs.kotlinx.coroutines.android)

    // Jetpack Compose ViewModel support (optional but recommended for Compose + ViewModel integration)
    implementation (libs.androidx.lifecycle.viewmodel.compose)

    //Fetching Phone number
    implementation (libs.play.services.auth)

    // Retrofit core library
    implementation (libs.retrofit)

    // Gson converter for JSON serialization/deserialization
    implementation (libs.converter.gson)

    // OkHttp for networking (optional, but often used with Retrofit)
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)

    // Coroutines dependencies
    implementation (libs.kotlinx.coroutines.android)

    //Room
    implementation (libs.androidx.room.runtime)
    // Room compiler (annotation processor for DAO classes)
    ksp (libs.androidx.room.compiler)
    // Optional - Room Kotlin extensions and Coroutines support
    implementation (libs.androidx.room.ktx)

    //Navigation
    implementation(libs.androidx.navigation.compose)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    //relaunch
    implementation ("androidx.work:work-runtime-ktx:2.9.0") // for WorkManager
}