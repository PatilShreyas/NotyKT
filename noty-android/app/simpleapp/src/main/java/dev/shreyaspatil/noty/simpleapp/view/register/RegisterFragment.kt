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

package dev.shreyaspatil.noty.simpleapp.view.register

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.simpleapp.R
import dev.shreyaspatil.noty.simpleapp.databinding.RegisterFragmentBinding
import dev.shreyaspatil.noty.simpleapp.view.base.BaseFragment
import dev.shreyaspatil.noty.simpleapp.view.hiltNotyMainNavGraphViewModels
import dev.shreyaspatil.noty.utils.ext.setError
import dev.shreyaspatil.noty.utils.ext.toStringOrEmpty
import dev.shreyaspatil.noty.view.state.RegisterState
import dev.shreyaspatil.noty.view.viewmodel.RegisterViewModel

@AndroidEntryPoint
class RegisterFragment : BaseFragment<RegisterFragmentBinding, RegisterState, RegisterViewModel>() {

    override val viewModel: RegisterViewModel by hiltNotyMainNavGraphViewModels()

    override fun initView() {
        with(binding) {
            buttonRegister.setOnClickListener { viewModel.register() }
            backButton.setOnClickListener { navigateUp() }
            textLoginButton.setOnClickListener { navigateUp() }
            textFieldUsername.editText?.addTextChangedListener {
                viewModel.setUsername(it.toStringOrEmpty())
            }
            textFieldPassword.editText?.addTextChangedListener {
                viewModel.setPassword(it.toStringOrEmpty())
            }
            textFieldConfirmPassword.editText?.addTextChangedListener {
                viewModel.setConfirmPassword(it.toStringOrEmpty())
            }
        }
    }

    override fun render(state: RegisterState) {
        showProgressDialog(state.isLoading)

        binding.textFieldUsername.setError(state.isValidUsername == false) {
            getString(R.string.message_field_username_invalid)
        }

        binding.textFieldPassword.setError(state.isValidPassword == false) {
            getString(R.string.message_field_password_invalid)
        }

        binding.textFieldConfirmPassword.setError(state.isValidConfirmPassword == false) {
            getString(R.string.message_password_mismatched)
        }

        if (state.isLoggedIn) {
            navigateToNotesScreen()
        }

        val errorMessage = state.error
        if (errorMessage != null) {
            showErrorDialog(
                title = getString(R.string.dialog_title_signup_failed),
                message = errorMessage,
                onDialogDismiss = viewModel::clearError
            )
        }
    }

    private fun navigateToNotesScreen() {
        findNavController().navigate(R.id.action_registerFragment_to_notesFragment)
    }

    private fun navigateUp() = findNavController().navigateUp()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = RegisterFragmentBinding.inflate(inflater, container, false)
}
