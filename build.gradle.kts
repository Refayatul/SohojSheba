plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.google.services) apply false
    //id("com.google.gms.google-services") version "4.4.4" apply false
}