plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "ro.cosminmihu.ktor.monitor.sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ro.cosminmihu.ktor.monitor.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }


    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {
            storeFile = File(project.rootDir, "extra/sample/sample-ktor-monitor.jks")
            storePassword = "ktor-monitor-sample"
            keyAlias = "ktor-monitor-sample"
            keyPassword = "ktor-monitor-sample"
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.sample.ktor.shared)
    implementation(libs.androidx.activity.compose)
    coreLibraryDesugaring(libs.desugar)
}

