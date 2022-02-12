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

import com.google.android.material.textfield.TextInputLayout

/**
 * Sets the [message] as error if [isError] is true. If it's false, error is cleared
 */
inline fun TextInputLayout.setError(isError: Boolean, message: () -> String) {
    error = if (isError) message() else null
}
