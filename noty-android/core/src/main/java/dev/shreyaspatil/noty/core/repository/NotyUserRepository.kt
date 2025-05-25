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

package dev.shreyaspatil.noty.core.repository

import dev.shreyaspatil.noty.core.model.AuthCredential
import javax.inject.Singleton

/**
 * Network Repository for user authorization of noty.
 */
@Singleton
interface NotyUserRepository {
    /**
     * Register/Create a new user using [username] and [password]
     */
    suspend fun addUser(
        username: String,
        password: String,
    ): Either<AuthCredential>

    /**
     * Sign ins a user using [username] and [password] which is already exists.
     */
    suspend fun getUserByUsernameAndPassword(
        username: String,
        password: String,
    ): Either<AuthCredential>
}
