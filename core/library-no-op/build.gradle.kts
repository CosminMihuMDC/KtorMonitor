import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.binary.compatibility.validator)
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    val artifact = "ktor-monitor-core-no-op"
    coordinates(group.toString(), artifact, version.toString())

    pom {
        name.set("Ktor Monitor - Core (No-Op)")
        description.set("""No-op implementation of the Ktor Core plugin for Ktor Monitor.""".trimMargin())
        inceptionYear.set("2025")
        url.set("https://github.com/CosminMihuMDC/KtorMonitor")

        licenses {
            license {
                name = "The Apache Software License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                id = "Cosmin Mihu"
                name = "Cosmin Mihu"
                url = "https://www.cosminmihu.ro/"
            }
        }

        scm {
            url = "https://github.com/CosminMihuMDC/KtorMonitor.git"
            connection = "scm:git:git://github.com/CosminMihuMDC/KtorMonitor.git"
            developerConnection = "scm:git:git://github.com/CosminMihuMDC/KtorMonitor.git"
        }

        issueManagement {
            system = "GitHub Issues"
            url = "https://github.com/CosminMihuMDC/KtorMonitor/issues"
        }

        ciManagement {
            system = "GitHub Actions"
            url = "https://github.com/CosminMihuMDC/KtorMonitor/actions"
        }

        distributionManagement {
            downloadUrl = "https://github.com/CosminMihuMDC/KtorMonitor/releases"
        }
    }
}

kotlin {
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes") // TODO remove after jetbrains fix
    }

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
        }
        webMain.dependencies {
            implementation(npm("sql.js", libs.versions.sqljs.get()))
            implementation(devNpm("copy-webpack-plugin", libs.versions.webpack.get()))
        }
    }
}

android {
    namespace = "ro.cosminmihu.ktor.monitor"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}