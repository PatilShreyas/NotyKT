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

package dev.shreyaspatil.noty.composeapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import dev.shreyaspatil.noty.composeapp.R
import dev.shreyaspatil.noty.composeapp.navigation.NotyNavigation
import dev.shreyaspatil.noty.composeapp.ui.theme.NotyTheme
import dev.shreyaspatil.noty.core.preference.PreferenceManager
import dev.shreyaspatil.noty.view.viewmodel.NoteDetailViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryProvider {
        fun noteDetailViewModelFactory(): NoteDetailViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContent {
            NotyMain()
        }
        observeUiTheme()
    }

    @Composable
    private fun NotyMain() {
        val darkMode by preferenceManager.uiModeFlow.collectAsState(initial = isSystemInDarkTheme())

        NotyTheme(darkTheme = darkMode) {
            Surface {
                NotyNavigation()
            }
        }
    }

    private fun observeUiTheme() {
        lifecycleScope.launchWhenStarted {
            preferenceManager.uiModeFlow.collect { isDarkMode ->
                val mode = when (isDarkMode) {
                    true -> AppCompatDelegate.MODE_NIGHT_YES
                    false -> AppCompatDelegate.MODE_NIGHT_NO
                }
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
    }
}
