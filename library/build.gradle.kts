import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.Year

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinx.atomicfu)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.dokka)
}

val module = "ktor-monitor"
val artifact = "ktor-monitor-logging"
group = "ro.cosminmihu.ktor"
version = "1.9.3"

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), artifact, version.toString())

    pom {
        name.set("Ktor Monitor")
        description.set("""Powerful tools to log Ktor Client requests and responses, making it easier to debug and analyze network communication.""".trimMargin())
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

apiValidation {
    ignoredPackages.add("ro.cosminmihu.ktor.monitor.db.sqldelight")

    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
        strictValidation = true
    }
}


dokka {
    moduleName = module
    moduleVersion = project.version.toString()
    val docsDir = File(rootDir, "docs")

    dokkaSourceSets.configureEach {
        perPackageOption {
            matchingRegex.set("ro.cosminmihu.ktor.monitor.db.sqldelight")
            suppress.set(true)
        }

        perPackageOption {
            matchingRegex.set("ro.cosminmihu.ktor.monitor.ui.resources")
            suppress.set(true)
        }
    }

    dokkaPublications.html {
        outputDirectory.set(File(docsDir, "html"))
    }

    pluginsConfiguration.html {
        customAssets.from(File(docsDir, "logo-icon.svg"))
        footerMessage.set("© ${Year.now().value} Cosmin Mihu")
    }
}

sqldelight {
    databases {
        create("LibraryDatabase") {
            packageName.set("ro.cosminmihu.ktor.monitor.db.sqldelight")
            generateAsync = true
        }
    }
    linkSqlite = true
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "ro.cosminmihu.ktor.monitor.ui.resources"
}

kotlin {
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes") // TODO remove after jetbrains fix
    }

    js {
        outputModuleName = "KtorMonitor"
        browser {
            commonWebpackConfig {
                outputFileName = "KtorMonitor.js"
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "KtorMonitor"
        browser {
            commonWebpackConfig {
                outputFileName = "KtorMonitor.js"
            }
        }
        binaries.executable()
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("debug", "release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts("-lsqlite3")
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.android.permisssions)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android)
            implementation(libs.koin.android)
            implementation(libs.coil.gif)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.adaptive)
            implementation(libs.compose.adaptive.layout)
            implementation(libs.compose.adaptive.navigation)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.primitive.adapters)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.datetime)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.coil.svg)
            implementation(libs.kotlinx.atomicfu)
            implementation(libs.jsontree)
            implementation(libs.ksoup)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.cio)
            implementation(libs.sqldelight.jvm)
            implementation(libs.slf4j.simple)
        }

        webMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.sqldelight.web)
            implementation(npm("sql.js", libs.versions.sqljs.get()))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
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
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        buildConfig = true
    }
}