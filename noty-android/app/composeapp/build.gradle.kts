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

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.compose.compiler.report)
    alias(libs.plugins.jetbrains.compose)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "dev.shreyaspatil.noty.composeapp"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "dev.shreyaspatil.noty.composeapp.HiltComposeApplicationRunner"
    }

    packaging {
        resources {
            excludes +=
                listOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "META-INF/LICENSE.txt",
                    "META-INF/LICENSE.md",
                    "META-INF/NOTICE.txt",
                    "META-INF/NOTICE",
                    "META-INF/LICENSE",
                    "META-INF/DEPENDENCIES",
                )
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
    }

    kotlinOptions {
        jvmTarget = libs.versions.javaVersion.get()
    }

    kotlin {
        jvmToolchain(libs.versions.javaVersion.get().toInt())
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    lint {
        abortOnError = false
    }

    namespace = "dev.shreyaspatil.noty.composeapp"
}

dependencies {
    // Kotlin Stdlib
    implementation(libs.kotlin.stdlib)

    // Core Module
    implementation(project(":core"))

    // Common App Module
    implementation(project(":app"))

    // Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.constraintlayout.compose)

    // Material Design
    implementation(libs.material)

    // Lottie Compose
    implementation(libs.lottie.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    testImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Dagger + Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    // Hilt + Jetpack
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Hilt + Navigation
    implementation(libs.androidx.hilt.navigation.fragment)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt Compose Navigation
    implementation(libs.androidx.hilt.navigation.compose)

    // Capturable
    implementation(libs.capturable)

    // Testing
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    // Android Testing
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // Needed for createComposeRule, but not createAndroidComposeRule:
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Hilt for testing
    androidTestImplementation(libs.dagger.hilt.testing)
    kspAndroidTest(libs.dagger.hilt.compiler)

    // WorkManager for testing
    androidTestImplementation(libs.androidx.work.testing)

    // Leak Canary
    // Uncomment this when have to check for leaks
    // debugImplementation(libs.leakcanary)
}

htmlComposeCompilerReport {
    name = "NotyKT"
    showOnlyUnstableComposables = true
}
