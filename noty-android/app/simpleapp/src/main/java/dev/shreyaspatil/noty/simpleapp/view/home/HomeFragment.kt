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

package dev.shreyaspatil.noty.simpleapp.view.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.simpleapp.R
import dev.shreyaspatil.noty.simpleapp.databinding.HomeFragmentBinding
import dev.shreyaspatil.noty.simpleapp.view.base.BaseFragment
import dev.shreyaspatil.noty.simpleapp.view.hiltNotyMainNavGraphViewModels
import dev.shreyaspatil.noty.view.state.HomeState
import dev.shreyaspatil.noty.view.viewmodel.HomeViewModel

/**
 * Currently nothing is going to performed in HomeFragment.
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeFragmentBinding, HomeState, HomeViewModel>() {

    override val viewModel: HomeViewModel by hiltNotyMainNavGraphViewModels()

    override fun initView() {}

    override fun render(state: HomeState) {
        val isLoggedIn = state.isLoggedIn ?: return

        val destination = if (isLoggedIn) {
            R.id.action_homeFragment_to_loginFragment
        } else {
            R.id.action_homeFragment_to_notesFragment
        }
        findNavController().navigate(destination)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = HomeFragmentBinding.inflate(inflater, container, false)
}
