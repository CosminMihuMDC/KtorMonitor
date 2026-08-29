import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {

    android {
        namespace = "ro.cosminmihu.ktor.monitor.sample.okhttp.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.okhttp)
            implementation(projects.okhttp.libraryOkhttp)
            implementation(projects.sample.shared)
//            implementation("ro.cosminmihu.ktor:ktor-monitor-okhttp-interceptor:1.15.0")
//            implementation(projects.okhttp.libraryOkhttpNoOp)
//            implementation("ro.cosminmihu.ktor:ktor-monitor-okhttp-interceptor-no-op:1.15.0")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "ro.cosminmihu.ktor.monitor.sample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "ro.cosminmihu.ktor.monitor.sample"
            packageVersion = "1.0.0"
        }
    }
}


