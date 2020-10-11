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

import io.ktor.config.*
import io.ktor.util.*

/**
 * Class containing Configuration values or secret key which will be provided from
 * application.conf (from environment variables).
 */
@Suppress("PropertyName")
@KtorExperimentalAPI
class Config constructor(config: ApplicationConfig) {
    val SECRET_KEY = config.property("key.secret").getString()

    val DATABASE_HOST = config.property("database.host").getString()
    val DATABASE_PORT = config.property("database.port").getString()
    val DATABASE_NAME = config.property("database.name").getString()
    val DATABASE_USER = config.property("database.user").getString()
    val DATABASE_PASSWORD = config.property("database.password").getString()
}