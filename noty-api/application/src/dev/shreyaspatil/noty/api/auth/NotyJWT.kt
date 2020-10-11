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

package dev.shreyaspatil.noty.api.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

/**
 * Simple implementation for providing JWT Authentication mechanism.
 * Use [sign] method to generate token.
 */
open class NotyJWT private constructor(secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    /**
     * Generates JWT token from [userId].
     */
    fun sign(userId: String): String = JWT
        .create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim(ClAIM, userId)
        .sign(algorithm)

    companion object {
        lateinit var instance: NotyJWT
            private set

        fun initialize(secret: String) {
            synchronized(this) {
                if (!this::instance.isInitialized) {
                    instance = NotyJWT(secret)
                }
            }
        }

        private const val ISSUER = "NotyKT-JWT-Issuer"
        private const val AUDIENCE = "https://noty-api.herokuapp.com"
        const val ClAIM = "userId"
    }
}