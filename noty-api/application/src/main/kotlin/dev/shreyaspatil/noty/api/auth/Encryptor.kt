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

package dev.shreyaspatil.noty.api.auth

import dev.shreyaspatil.noty.api.di.module.SecretKey
import io.ktor.util.hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

interface Encryptor {
    /**
     * Encrypts [data] and returns
     */
    fun encrypt(data: String): String
}

class NotyEncryptor @Inject constructor(@SecretKey secret: String) : Encryptor {

    private val hmacKey: SecretKeySpec = SecretKeySpec(secret.toByteArray(), ALGORITHM)

    override fun encrypt(data: String): String {
        val hmac = Mac.getInstance(ALGORITHM)
        hmac.init(hmacKey)
        return hex(hmac.doFinal(data.toByteArray(Charsets.UTF_8)))
    }

    companion object {
        private const val ALGORITHM = "HmacSHA256"
    }
}