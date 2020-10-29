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

package dev.shreyaspatil.noty.core.preference

import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Preference Manager for the application.
 * Currently it just keeps UI mode preference such as Light mode or dark mode.
 */
@Singleton
interface PreferenceManager {
    val uiModeFlow: Flow<Boolean>

    /**
     * Updates the preference for UI mode.
     *
     * @param enable Sets Dark Mode if true otherwise Light mode.
     */
    suspend fun setDarkMode(enable: Boolean)
}
