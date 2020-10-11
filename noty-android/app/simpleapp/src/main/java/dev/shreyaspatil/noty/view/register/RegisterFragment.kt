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

package dev.shreyaspatil.noty.view.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.R
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.databinding.RegisterFragmentBinding
import dev.shreyaspatil.noty.view.base.BaseFragment
import dev.shreyaspatil.noty.view.viewmodel.RegisterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RegisterFragment : BaseFragment<RegisterFragmentBinding, RegisterViewModel>() {

    override val viewModel: RegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initViews()
    }

    private fun initData() {
        viewModel.authLiveData.observe(viewLifecycleOwner) { viewState ->
            when (viewState) {
                is ViewState.Loading -> {
                    // TODO Show loading progress
                }
                is ViewState.Success -> onAuthSuccess()
                is ViewState.Failed -> {
                    // TODO Show error message
                }
            }
        }
    }

    private fun initViews() {
        binding.buttonRegister.setOnClickListener { onRegisterClicked() }
    }

    private fun onRegisterClicked() {
        val username = binding.textFieldUsername.editText?.text.toString()
        val password = binding.textFieldPassword.editText?.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.editText?.text.toString()

        if (validate(username, password, confirmPassword)) {
            viewModel.register(username, password)
        }
    }

    private fun onAuthSuccess() {
        findNavController().navigate(R.id.action_registerFragment_to_notesFragment)
    }

    private fun validate(username: String, password: String, confirmPassword: String): Boolean {
        return with(binding) {
            when {
                username.isBlank() -> {
                    textFieldUsername.error = "Username should be valid!"
                    false
                }

                password.isBlank() -> {
                    textFieldPassword.error = "Should not blank"
                    false
                }

                confirmPassword.isBlank() -> {
                    textFieldConfirmPassword.error = "Should not blank"
                    false
                }

                password != confirmPassword -> {
                    textFieldConfirmPassword.error = "Password and confirm password not matching"
                    false
                }
                else -> true
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = RegisterFragmentBinding.inflate(inflater, container, false)
}
