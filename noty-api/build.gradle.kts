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
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover) apply false
}

version = "0.1.0"
group = "dev.shreyaspatil.noty"

val jvmPluginId = libs.plugins.kotlin.jvm.get().pluginId
val ktlintPluginId = libs.plugins.ktlint.get().pluginId

subprojects {
    apply(plugin = jvmPluginId)
    apply(plugin = ktlintPluginId)

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
        verbose.set(true)
        outputToConsole.set(true)
        outputColorName.set("RED")
    }
}
