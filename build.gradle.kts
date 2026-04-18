import java.time.Year

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinx.atomicfu) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.binary.compatibility.validator) apply false
    alias(libs.plugins.dokka)
}

dependencies {
    dokka(project(":ktor:library-ktor"))
    dokka(project(":okhttp:library-okhttp"))
}

dokka {
    val docsDir = File(rootDir, "docs/docs")

    moduleName = "Ktor Monitor"
    moduleVersion = "1.10.3"

    dokkaPublications.html {
        outputDirectory.set(File(docsDir, "api"))
    }

    pluginsConfiguration.html {
        customAssets.from(File(docsDir, "images/logo-icon.svg"))
        footerMessage.set("© ${Year.now().value} Cosmin Mihu")
    }
}
