plugins {
    application
    kotlin("plugin.serialization")
}
group = "dev.shreyaspatil.noty.application"
version = "0.1.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

val coroutinesVersion: String by project
val kotlinSerializerVersion: String by project
val ktorVersion: String by project
val logbackVersion: String by project
val testContainerVersion: String by project

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven("https://kotlin.bintray.com/ktor")
}

dependencies {
    // Data module
    implementation(project(":data"))

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // Serializer
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializerVersion")

    // Ktor
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Testing
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainerVersion")
    testImplementation("org.testcontainers:postgresql:$testContainerVersion")
}

task("stage") {
    dependsOn("installDist")
}