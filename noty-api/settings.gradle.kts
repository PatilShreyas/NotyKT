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
pluginManagement {
    val kotlinVersion: String by settings
    val ktlintVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        kotlin("kapt") version kotlinVersion
    }
    repositories {
        maven("https://dl.bintray.com/kotlin/kotlinx")
        gradlePluginPortal()
        maven(url = "https://plugins.gradle.org/m2")
    }
}

rootProject.name = "noty-api"
include("application", "data")