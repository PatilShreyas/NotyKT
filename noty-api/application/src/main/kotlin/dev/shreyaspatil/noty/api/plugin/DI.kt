/*
 * Copyright 2021 Shreyas Patil
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

package dev.shreyaspatil.noty.api.plugin

import dev.shreyaspatil.noty.api.di.component.AppComponent
import dev.shreyaspatil.noty.api.di.component.ControllerComponent
import dev.shreyaspatil.noty.api.di.component.DaggerAppComponent
import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.AttributeKey

fun Application.configureDI() {
    val appComponent = DaggerAppComponent.builder().withApplication(this).build()

    attributes.put(appComponentKey, appComponent)
    attributes.put(controllerComponentKey, appComponent.controllerComponent())
}

val controllerComponentKey = AttributeKey<ControllerComponent>("NOTY_CONTROLLER_COMPONENT")
val appComponentKey = AttributeKey<AppComponent>("NOTY_APP_COMPONENT")

/**
 * Retrieves [ControllerComponent] from Application scope
 */
val Application.controllers: ControllerComponent get() = attributes[controllerComponentKey]

/**
 * Retrieves [AppComponent] from Application scope
 */
val Application.appComponent: AppComponent get() = attributes[appComponentKey]

/**
 * Retrieves [ControllerComponent] from Route scope
 */
val Route.controllers: ControllerComponent get() = application.attributes[controllerComponentKey]
