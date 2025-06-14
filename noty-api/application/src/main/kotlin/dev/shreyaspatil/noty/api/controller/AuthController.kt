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

package dev.shreyaspatil.noty.api.controller

import dev.shreyaspatil.noty.api.auth.Encryptor
import dev.shreyaspatil.noty.api.auth.JWTController
import dev.shreyaspatil.noty.api.exception.BadRequestException
import dev.shreyaspatil.noty.api.model.response.AuthResponse
import dev.shreyaspatil.noty.api.utils.isAlphaNumeric
import dev.shreyaspatil.noty.data.dao.UserDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller for authentication i.e. User's management
 */
@Singleton
class AuthController @Inject constructor(
    private val userDao: UserDao,
    private val jwt: JWTController,
    private val encryptor: Encryptor,
) {

    fun register(username: String, password: String): AuthResponse {
        return try {
            validateCredentialsOrThrowException(username, password)

            if (!userDao.isUsernameAvailable(username)) {
                throw BadRequestException("Username is not available")
            }

            val user = userDao.addUser(username, encryptor.encrypt(password))
            AuthResponse.success(jwt.sign(user.id), "Registration successful")
        } catch (bre: BadRequestException) {
            AuthResponse.failed(bre.message)
        }
    }

    fun login(username: String, password: String): AuthResponse {
        return try {
            validateCredentialsOrThrowException(username, password)

            val user = userDao.findByUsernameAndPassword(username, encryptor.encrypt(password))
                ?: throw BadRequestException("Invalid credentials")

            AuthResponse.success(jwt.sign(user.id), "Login successful")
        } catch (bre: BadRequestException) {
            AuthResponse.failed(bre.message)
        }
    }

    private fun validateCredentialsOrThrowException(username: String, password: String) {
        val message = when {
            (username.isBlank() or password.isBlank()) -> "Username or password should not be blank"
            (username.length !in (4..30)) -> "Username should be of min 4 and max 30 character in length"
            (password.length !in (8..50)) -> "Password should be of min 8 and max 50 character in length"
            (!username.isAlphaNumeric()) -> "No special characters allowed in username"
            else -> return
        }

        throw BadRequestException(message)
    }
}
