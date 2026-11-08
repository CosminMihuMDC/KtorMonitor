plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.maven.publish)
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    val artifact = "ktor-monitor-mcp-companion"
    coordinates(group.toString(), artifact, version.toString())

    pom {
        name.set("Ktor Monitor - MCP Companion")
        description.set("""Local MCP server that lets AI agents query the HTTP calls captured by Ktor Monitor. Run the "-all" JAR with Java 11+.""".trimMargin())
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

base.archivesName = "ktor-monitor-mcp-companion"

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }

    // Wire protocol shared with `library-mcp` (source-only, not a published module).
    sourceSets.main {
        kotlin.srcDir("../protocol/src/commonMain/kotlin")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.websockets)
    // Logs go to stderr; stdout is reserved for the MCP stdio transport.
    runtimeOnly(libs.slf4j.simple)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.cio)
    testImplementation(libs.ktor.client.websockets)
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "ro.cosminmihu.ktor.monitor.mcp.companion.MainKt",
            "Implementation-Version" to project.version.toString(),
        )
    }
}

tasks.shadowJar {
    archiveClassifier.set("all")
    mergeServiceFiles()
    manifest {
        attributes(
            "Main-Class" to "ro.cosminmihu.ktor.monitor.mcp.companion.MainKt",
            "Implementation-Version" to project.version.toString(),
        )
    }
}

// Publish the executable fat JAR next to the regular one, as `ktor-monitor-mcp-companion-<version>-all.jar`.
afterEvaluate {
    publishing.publications.withType<MavenPublication>().configureEach {
        artifact(tasks.shadowJar)
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
