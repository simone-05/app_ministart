plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.simonimal.ministart"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.simonimal.ministart"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            val keyPath = System.getenv("KEY_PATH")
            val keyStorePassword = System.getenv("KEYSTORE_PASSWORD")
            val keyAlias = System.getenv("KEY_ALIAS")
            val keyPassword = System.getenv("KEY_PASSWORD")

            if (!keyPath.isNullOrBlank() && !keyStorePassword.isNullOrBlank() &&
            !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()) {
                signingConfig = signingConfigs.create("release") {
                    storeFile = file(keyPath)
                    storePassword = keyStorePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                }
            } else {
                println("⚠️ WARNING: Signing environment variables are missing. Release build will not be signed.")
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.13.0")

    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
}
