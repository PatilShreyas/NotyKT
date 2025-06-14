plugins {
    application
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
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
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization)

    // Logging
    implementation(libs.logback)

    // Dagger
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    // Testing
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.postgres)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.property)
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("build") {
    finalizedBy("installDist")
}
