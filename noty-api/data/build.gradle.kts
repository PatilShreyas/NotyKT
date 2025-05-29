
plugins {
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

group = "dev.shreyaspatil.noty.data"
version = "0.1.0"

sourceSets {
    main {
        kotlin.srcDirs("src")
        java.srcDirs("src")
        resources.srcDirs("resources")
    }
    test {
        kotlin.srcDirs("test")
        java.srcDirs("test")
        resources.srcDirs("testresources")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    // Exposed
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.jodatime)

    // PostgreSQL
    implementation(libs.postgres)

    // Dagger
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    implementation(libs.hikari)

    // Testing
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.property)
}
