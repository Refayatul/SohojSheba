plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose.compiler) // ADD THIS BACK - It's required for Kotlin 2.0+
}

android {
    namespace = "com.bonfire.shohojsheba"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bonfire.shohojsheba"
        minSdk = 26
        targetSdk = 36
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

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // CHANGE: 11 → 17
        targetCompatibility = JavaVersion.VERSION_17 // CHANGE: 11 → 17
    }

    kotlinOptions {
        jvmTarget = "17" // CHANGE: 11 → 17
    }

    // Compose options - version is automatically handled by Kotlin 2.0+
    composeOptions {
        // No need to specify kotlinCompilerExtensionVersion - handled automatically
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose BOM and UI
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.graphics)
    implementation(libs.compose.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.material.icons.extended)

    // Lifecycle & Activity
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Debug dependencies
    debugImplementation(libs.compose.tooling)
    debugImplementation(libs.compose.test.manifest)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}