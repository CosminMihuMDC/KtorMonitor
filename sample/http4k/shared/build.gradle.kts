plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {

    android {
        namespace = "ro.cosminmihu.ktor.monitor.sample.http4k.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        localDependencySelection {
            selectBuildTypeFrom.set(listOf("debug", "release"))
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.http4k.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.http4k.core)
            implementation(libs.http4k.multipart)
            implementation(projects.http4k.libraryHttp4k)
            implementation(projects.sample.shared)
//            implementation("ro.cosminmihu.ktor:ktor-monitor-http4k-filter:1.14.4")
//            implementation(projects.http4k.libraryHttp4kNoOp)
//            implementation("ro.cosminmihu.ktor:ktor-monitor-http4k-filter-no-op:1.14.4")
        }
    }
}

