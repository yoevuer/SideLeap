import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "hunoia.sideleap"
    compileSdk = 35

    defaultConfig {
        applicationId = "hunoia.sideleap"
        minSdk = 24
        targetSdk = 35
        versionCode = 10504
        versionName = "1.5.4"
        resourceConfigurations += listOf("en", "zh-rCN", "zh-rTW")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        val localProperties = project.rootProject.file("local.properties")
        if (localProperties.exists()) {
            val properties = Properties()
            localProperties.inputStream().use { properties.load(it) }
            val storeFileName = properties.getProperty("STORE_FILE_NAME")?.trim().orEmpty()
            val keystorePassword = properties.getProperty("KEYSTORE_PASSWORD")?.trim().orEmpty()
            val storeAlias = properties.getProperty("STORE_ALIAS")?.trim().orEmpty()
            val keyPasswordValue = properties.getProperty("KEY_PASSWORD")?.trim().orEmpty()
            val hasCompleteSigningFields =
                storeFileName.isNotEmpty() &&
                        keystorePassword.isNotEmpty() &&
                        storeAlias.isNotEmpty() &&
                        keyPasswordValue.isNotEmpty()
            val releaseStoreFile = if (hasCompleteSigningFields) {
                file(storeFileName)
            } else {
                null
            }
            if (releaseStoreFile?.exists() == true) {
                register("release") {
                    storeFile = releaseStoreFile
                    storePassword = keystorePassword
                    keyAlias = storeAlias
                    keyPassword = keyPasswordValue
                    enableV1Signing = true
                    enableV2Signing = true
                    enableV3Signing = true
                    enableV4Signing = false
                }
            }
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
            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
        create("perf") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".perf"
            resValue("string", "app_name", "@string/app_name")
            resValue("string", "home_title", "@string/app_name")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        abortOnError = false
        lintConfig = file("lint.xml")
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
        aidl = true
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.com.aaronzzx.fastcompose.compose)
    implementation(libs.com.aaronzzx.fastcompose.compose.accessibility)
    implementation(libs.com.tiann.freereflection)
    implementation(libs.jetbrains.kotlin.serialization)
    implementation(libs.androidx.datastore)
    implementation(libs.compose.colorpicker)
    implementation(libs.material.icons.extended)
    implementation(libs.pinyin)
    implementation(libs.rikka.shizuku.api)
    implementation(libs.rikka.shizuku.provider)
}
