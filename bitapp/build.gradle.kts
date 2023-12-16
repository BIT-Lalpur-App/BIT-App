plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.atech.bit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.atech.bit"
        minSdk = 24
        targetSdk = 34
        versionCode = 65
        versionName = "5.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    bundle {
        storeArchive {
            enable = false
        }
    }
    flavorDimensions += "type"
    productFlavors {
        create("global") {
            dimension = "type"
            versionNameSuffix = "-global"
        }
        create("beta") {
            dimension = "type"
            versionNameSuffix = "-beta"
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file("keystore/keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(project(mapOf("path" to ":bitapp:attendance")))
    implementation(project(mapOf("path" to ":bitapp:course")))
    implementation(project(mapOf("path" to ":bitapp:login")))
    implementation(project(":core"))
    implementation(project(":syllabus"))
    implementation(project(":theme"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.fragment)
    implementation(libs.androidx.navigation.compose)

//    implementation(libs.swipe)


    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
}
