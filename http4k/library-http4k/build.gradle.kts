plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.dokka)
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    val artifact = "ktor-monitor-http4k-filter"
    coordinates(group.toString(), artifact, version.toString())

    pom {
        name.set("Ktor Monitor - http4k Filter")
        description.set("""http4k Filter that provides the capability to log HTTP calls for Ktor Monitor.""".trimMargin())
        inceptionYear.set("2026")
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
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
        strictValidation = true
    }
}

dokka {
    moduleName = "http4k Filter"
    moduleVersion = project.version.toString()
}

dependencies {
    dokka(projects.core.library)
}

kotlin {
    explicitApi()

    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }

    android {
        namespace = "ro.cosminmihu.ktor.monitor.http4k"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.core.library)
            implementation(libs.http4k.core)
        }
    }
}


