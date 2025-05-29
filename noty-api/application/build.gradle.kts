plugins {
    application
    alias(libs.plugins.kotlin.serialization)
}

group = "dev.shreyaspatil.noty.application"
version = "1.0.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
    }
}

dependencies {
    // Data module
    implementation(project(":data"))

    // Coroutines
    implementation(libs.coroutines.core)

    // Serializer
    implementation(libs.kotlin.serialization)

    // Ktor
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization)

    // Logging
    implementation(libs.logback)

    // Testing
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.postgres)
}

tasks.named("build") {
    finalizedBy("installDist")
}
