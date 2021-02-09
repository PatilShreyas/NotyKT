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

buildscript {
    ext {
        kotlinVersion = "1.4.21-2"
        kotlinSerializerVersion = "1.0.1"
        ktlintVersion = "9.3.0"
        coroutinesVersion = "1.4.2"
        ktorVersion = "1.4.3"
        exposedVersion = "0.28.1"
        postgresVersion = "42.2.18"
        logbackVersion = "1.2.3"
        daggerVersion = "2.30.1"
        testContainerVersion = "1.15.1"
        kotestVersion = "4.4.0"
    }

    repositories {
        jcenter()
        maven {
            url("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
        classpath "org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion"
        classpath "org.jlleitschuh.gradle:ktlint-gradle:$ktlintVersion"
    }
}

apply plugin: 'kotlin'
apply plugin: 'jacoco'

version "0.1.0"
group "dev.shreyaspatil.noty"

repositories {
    mavenCentral()
    jcenter()
}

subprojects {
    apply(plugin: 'org.jetbrains.kotlin.jvm')
    apply(plugin: 'org.jetbrains.kotlin.kapt')
    apply(plugin: 'jacoco')
    apply(plugin: 'org.jlleitschuh.gradle.ktlint')

    sourceCompatibility = "11"
    targetCompatibility = "11"

    sourceSets {
        main.kotlin.srcDirs = main.java.srcDirs = ['src']
        test.kotlin.srcDirs = test.java.srcDirs = ['test']
        main.resources.srcDirs = ['resources']
        test.resources.srcDirs = ['testresources']
    }

    test {
        useJUnitPlatform()
        jacoco {
            destinationFile = file("${buildDir}/jacoco/test.exec")
        }
    }

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    ktlint {
        debug = true
        verbose = true
        outputToConsole = true
        outputColorName = "RED"
    }

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }

    dependencies {
        // Kotlin
        implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
        implementation "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"

        // Dagger
        implementation "com.google.dagger:dagger:$daggerVersion"
        kapt "com.google.dagger:dagger-compiler:$daggerVersion"

        // Testing
        testImplementation "io.kotest:kotest-runner-junit5-jvm:$kotestVersion"
        testImplementation "io.kotest:kotest-assertions-core-jvm:$kotestVersion"
        testImplementation "io.kotest:kotest-property-jvm:$kotestVersion"
    }
}

// Code Coverage Report (Jacoco)
task codeCoverageReport(type: JacocoReport) {
    // Gather execution data from all subprojects
    executionData fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec")

    subprojects.each {
        sourceSets it.sourceSets.main
    }

    reports {
        xml.enabled true
        html.enabled true
        csv.enabled false
    }
}
