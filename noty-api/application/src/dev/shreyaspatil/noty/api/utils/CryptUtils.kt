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

package dev.shreyaspatil.noty.api.utils

import io.ktor.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Function which encrypts password and return
 */
@KtorExperimentalAPI
fun hash(password: String): String {
    val hmac = Mac.getInstance(KeyProvider.ALGORITHM)
    hmac.init(KeyProvider.hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}

/**
 * SecretKeySpec provider. Used in [hash] for encrypting password
 */
object KeyProvider {
    lateinit var hmacKey: SecretKeySpec
        private set

    fun initialize(secret: String) {
        if (!this::hmacKey.isInitialized) {
            hmacKey = SecretKeySpec(secret.toByteArray(), ALGORITHM)
        }
    }

    const val ALGORITHM = "HmacSHA256"
}