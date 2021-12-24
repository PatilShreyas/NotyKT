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

package dev.shreyaspatil.noty.simpleapp.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.simpleapp.R
import dev.shreyaspatil.noty.core.preference.PreferenceManager
import dev.shreyaspatil.noty.simpleapp.databinding.MainActivityBinding
import dev.shreyaspatil.noty.utils.ext.hide
import dev.shreyaspatil.noty.utils.ext.show
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme) // Set AppTheme before setting content view.
        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        val navHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        observeNavElements(binding, navHostFragment.navController)

        with(navHostFragment.navController) {
            appBarConfiguration = AppBarConfiguration(graph)
            setupActionBarWithNavController(this, appBarConfiguration)
        }

        observeUiModePreferences()
    }

    private fun observeUiModePreferences() {
        preferenceManager.uiModeFlow.asLiveData().observe(this) {
            val uiMode = when (it) {
                true -> AppCompatDelegate.MODE_NIGHT_YES
                false -> AppCompatDelegate.MODE_NIGHT_NO
            }

            AppCompatDelegate.setDefaultNightMode(uiMode)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = item.onNavDestinationSelected(
        findNavController(R.id.nav_host_fragment)
    ) || super.onOptionsItemSelected(item)

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment)
        .navigateUp(appBarConfiguration)

    private fun observeNavElements(
        binding: MainActivityBinding,
        navController: NavController
    ) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> binding.topAppBar.hide()
                R.id.registerFragment -> binding.topAppBar.hide()
                else -> binding.topAppBar.show()
            }
        }
    }
}
