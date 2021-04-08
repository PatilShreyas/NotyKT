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

package dev.shreyaspatil.noty.preference

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dev.shreyaspatil.noty.core.preference.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.uiModePrefDataStore by preferencesDataStore("ui_mode_pref")

class PreferenceManagerImpl(context: Context) : PreferenceManager {

    private val dataStore = context.uiModePrefDataStore

    override val uiModeFlow: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preference -> preference[IS_DARK_MODE] ?: false }

    override suspend fun setDarkMode(enable: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = enable
        }
    }

    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("dark_mode")
    }
}
