import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.sample.http4k.shared)
    implementation(projects.core.library)
    implementation(libs.http4k.core)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.components.resources)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "ro.cosminmihu.ktor.monitor.sample.compose.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "ro.cosminmihu.ktor.monitor.sample.http4k"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register<JavaExec>("runSwing") {
    group = "application"
    description = "Runs the Swing sample entrypoint"
    mainClass.set("ro.cosminmihu.ktor.monitor.sample.swing.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("runCompose") {
    group = "application"
    description = "Runs the Compose sample entrypoint"
    mainClass.set("ro.cosminmihu.ktor.monitor.sample.compose.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
}