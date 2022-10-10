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

package dev.shreyaspatil.noty.api.di.module

import dagger.Module
import dagger.Provides
import dev.shreyaspatil.noty.data.database.DatabaseConfig
import io.ktor.application.*
import io.ktor.config.*
import javax.inject.Singleton

@Module
object ConfigModule {
    @Provides
    fun applicationConfig(application: Application) = application.environment.config

    @Singleton
    @Provides
    fun databaseConfig(config: ApplicationConfig): DatabaseConfig {
        val dbConfig = config.config("database")
        return DatabaseConfig(
            host = dbConfig.property("host").getString(),
            port = dbConfig.property("port").getString(),
            name = dbConfig.property("name").getString(),
            user = dbConfig.property("user").getString(),
            password = dbConfig.property("password").getString(),
            driver = dbConfig.property("driver").getString(),
            maxPoolSize = dbConfig.property("maxPoolSize").getString().toInt()
        )
    }

    @Singleton
    @Provides
    @SecretKey
    fun secretKey(config: ApplicationConfig): String = config.property("key.secret").getString()
}