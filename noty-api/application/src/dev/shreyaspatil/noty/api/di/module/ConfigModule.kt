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

import com.zaxxer.hikari.HikariConfig
import dagger.Module
import dagger.Provides
import io.ktor.application.*
import io.ktor.config.*
import javax.inject.Singleton

@Module
object ConfigModule {
    @Provides
    fun applicationConfig(application: Application) = application.environment.config

    @Singleton
    @Provides
    fun databaseConfig(config: ApplicationConfig): HikariConfig {
        val dbConfig = config.config("database")
        val host = dbConfig.property("host").getString()
        val port = dbConfig.property("port").getString()
        val name = dbConfig.property("name").getString()

        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://$host:$port/$name"
        config.password = dbConfig.property("password").getString()
        config.username = dbConfig.property("user").getString()
        config.maximumPoolSize = dbConfig.property("maxPoolSize").getString().toInt()
        config.driverClassName = dbConfig.property("driver").getString()
        return config
    }

    @Singleton
    @Provides
    @SecretKey
    fun secretKey(config: ApplicationConfig): String = config.property("key.secret").getString()
}