import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                outputFileName = "KtorMonitorSample.js"
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "KtorMonitorSample.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        webMain.dependencies {
            implementation(projects.sample.ktor.shared)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(devNpm("copy-webpack-plugin", libs.versions.webpack.get()))
        }
    }
}

