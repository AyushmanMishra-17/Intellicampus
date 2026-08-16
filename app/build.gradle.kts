import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// ---------------------------------------------------------
// Local properties
// ---------------------------------------------------------

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    FileInputStream(localPropertiesFile).use {
        localProperties.load(it)
    }
}

val freeLlmApiBaseUrl = localProperties.getProperty(
    "FREELLMAPI_BASE_URL",
    "https://intellicampus-ai-zy8i.onrender.com/v1"
)

val freeLlmApiKey = localProperties.getProperty(
    "FREELLMAPI_KEY",
    ""
)

// ---------------------------------------------------------
// Android
// ---------------------------------------------------------

android {
    namespace = "com.ayushman.intellicampus"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.ayushman.intellicampus"

        minSdk = 24
        targetSdk = 36

        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // -------------------------------------------------
        // FreeLLMAPI configuration
        // -------------------------------------------------

        buildConfigField(
            "String",
            "FREELLMAPI_BASE_URL",
            "\"${freeLlmApiBaseUrl.replace("\"", "\\\"")}\""
        )

        buildConfigField(
            "String",
            "FREELLMAPI_KEY",
            "\"${freeLlmApiKey.replace("\"", "\\\"")}\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

// ---------------------------------------------------------
// Dependencies
// ---------------------------------------------------------

dependencies {

    // AndroidX
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.runtime)

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.9.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.gridlayout:gridlayout:1.1.0")

    // Material
    implementation(libs.material)
    implementation("com.google.android.material:material:1.12.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}