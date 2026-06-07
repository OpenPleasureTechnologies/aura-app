```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.opt.aura"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.opt.aura"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    
    // BLE
    implementation("no.nordicsemi.android:ble-common:2.6.0")
    implementation("no.nordicsemi.android:ble-ktx:2.6.0")
    
    // TFLite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
}
