import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/*
 * Copyright 2020 Shreyas Patil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

val kotlinVersion: String by project
val daggerVersion: String by project
val kotestVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
    kotlin("kapt")
    jacoco
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/ktor")
    maven("https://kotlin.bintray.com/kotlinx")
}

version = "0.1.0"
group = "dev.shreyaspatil.noty"

configure(subprojects) {
    configurations {
        all {
            if (!name.startsWith("ktlint")) {
                resolutionStrategy.eachDependency {
                    if (requested.group == "org.jetbrains.kotlin") {
                        useVersion(kotlinVersion)
                        because("use single kotlin version")
                    }
                }
            }
        }
    }

    plugins.let {
        it.apply("org.jetbrains.kotlin.jvm")
        it.apply("org.jetbrains.kotlin.kapt")
        it.apply("jacoco")
        it.apply("org.jlleitschuh.gradle.ktlint")
    }

    java.sourceCompatibility = JavaVersion.VERSION_11
    java.targetCompatibility = JavaVersion.VERSION_11

    kotlin.sourceSets["main"].kotlin.srcDirs("src")
    kotlin.sourceSets["test"].kotlin.srcDirs("test")

    sourceSets["main"].resources.srcDirs("resources")
    sourceSets["test"].resources.srcDirs("testresources")

    tasks.withType<Test> {
        useJUnitPlatform()
        configure<JacocoTaskExtension> {
            destinationFile.apply {
                file("$buildDir/jacoco/test.exec")
            }
        }
    }

    tasks.withType<KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    configure<KtlintExtension> {
        debug.set(true)
        verbose.set(true)
        outputToConsole.set(true)
        outputColorName.set("RED")
    }

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }

    dependencies {
        // Kotlin
        implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        // Dagger
        implementation("com.google.dagger:dagger:$daggerVersion")
        kapt("com.google.dagger:dagger-compiler:$daggerVersion")

        // Testing
        testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
        testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")
    }
}

tasks.register<JacocoReport>("codeCoverageReport") {
    executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))
    subprojects.forEach {
        sourceSets.apply {
            it.sourceSets.main
        }
    }
    reports {
        xml.isEnabled = true
        html.isEnabled = true
        csv.isEnabled = false
    }
}