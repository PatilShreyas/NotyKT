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
import dev.shreyaspatil.noty.api.di.module.SecretKey
import javax.inject.Inject

interface JWTController {

    val verifier: JWTVerifier

    /**
     * Generates JWT Token from [data]
     */
    fun sign(data: String): String
}

/**
 * Simple implementation for providing JWT Authentication mechanism.
 * Use [sign] method to generate token.
 */
class NotyJWTController @Inject constructor(@SecretKey secret: String) : JWTController {
    private val algorithm = Algorithm.HMAC256(secret)
    override val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    /**
     * Generates JWT token from [userId].
     */
    override fun sign(data: String): String = JWT
        .create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim(CLAIM, data)
        .sign(algorithm)

    companion object {
        private const val ISSUER = "NotyKT-JWT-Issuer"
        private const val AUDIENCE = "https://notykt-production.up.railway.app"
        const val CLAIM = "userId"
    }
}
