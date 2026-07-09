import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

compose.resources {
    packageOfResClass = "ro.cosminmihu.ktor.monitor.sample.resources"
}

kotlin {

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.http4k.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.http4k.core)
            implementation(libs.http4k.multipart)
            implementation(projects.http4k.libraryHttp4k)
            implementation(projects.sample.shared)
//            implementation("ro.cosminmihu.ktor:ktor-monitor-http4k-filter:1.14.3")
//            implementation(projects.http4k.libraryHttp4kNoOp)
//            implementation("ro.cosminmihu.ktor:ktor-monitor-http4k-filter-no-op:1.14.3")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "ro.cosminmihu.ktor.monitor.sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ro.cosminmihu.ktor.monitor.sample.http4k"
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
}

compose.desktop {
    application {
        mainClass = "ro.cosminmihu.ktor.monitor.sample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "ro.cosminmihu.ktor.monitor.sample.http4k"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
