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

package dev.shreyaspatil.noty.utils.ext

import androidx.work.Data

fun <T> Data.Builder.putEnum(key: String, value: T) = apply { putString(key, value.toString()) }

inline fun <reified T : Enum<T>> Data.getEnum(key: String): T? {
    val enumValue = getString(key)
    return runCatching { enumValueOf<T>(enumValue!!) }.getOrNull()
}
