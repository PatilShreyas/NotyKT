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

package dev.shreyaspatil.noty.api

import dev.shreyaspatil.noty.api.plugin.appComponent
import dev.shreyaspatil.noty.api.plugin.configureAuthentication
import dev.shreyaspatil.noty.api.plugin.configureCORS
import dev.shreyaspatil.noty.api.plugin.configureContentNegotiation
import dev.shreyaspatil.noty.api.plugin.configureDI
import dev.shreyaspatil.noty.api.plugin.configureRouting
import dev.shreyaspatil.noty.api.plugin.configureStatusPages
import dev.shreyaspatil.noty.data.database.initDatabase
import io.ktor.server.application.Application

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureDI()
    configureCORS()
    configureAuthentication()
    configureStatusPages()
    configureContentNegotiation()
    configureRouting()

    init()
}

fun Application.init() {
    val dbConfig = appComponent.configComponent().databaseConfig()
    initDatabase(dbConfig)
}
