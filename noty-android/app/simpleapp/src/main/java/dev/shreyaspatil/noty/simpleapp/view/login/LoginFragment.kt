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

package dev.shreyaspatil.noty.simpleapp.view.login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.simpleapp.R
import dev.shreyaspatil.noty.simpleapp.databinding.LoginFragmentBinding
import dev.shreyaspatil.noty.simpleapp.view.base.BaseFragment
import dev.shreyaspatil.noty.simpleapp.view.hiltNotyMainNavGraphViewModels
import dev.shreyaspatil.noty.utils.ext.setError
import dev.shreyaspatil.noty.utils.ext.toStringOrEmpty
import dev.shreyaspatil.noty.view.state.LoginState
import dev.shreyaspatil.noty.view.viewmodel.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : BaseFragment<LoginFragmentBinding, LoginState, LoginViewModel>() {

    override val viewModel: LoginViewModel by hiltNotyMainNavGraphViewModels()

    override fun initView() {
        with(binding) {
            buttonLogin.setOnClickListener { viewModel.login() }
            textSignUpButton.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            textFieldUsername.editText?.addTextChangedListener {
                viewModel.setUsername(it.toStringOrEmpty())
            }
            textFieldPassword.editText?.addTextChangedListener {
                viewModel.setPassword(it.toStringOrEmpty())
            }
        }
    }

    override fun render(state: LoginState) {
        showProgressDialog(state.isLoading)

        binding.textFieldUsername.setError(state.isValidUsername == false) {
            getString(R.string.message_field_username_invalid)
        }

        binding.textFieldPassword.setError(state.isValidPassword == false) {
            getString(R.string.message_field_password_invalid)
        }

        if (state.isLoggedIn) {
            navigateToNotesScreen()
        }

        val errorMessage = state.error
        if (errorMessage != null) {
            showErrorDialog(
                title = getString(R.string.dialog_title_login_failed),
                message = errorMessage
            )
        }
    }

    private fun navigateToNotesScreen() {
        findNavController().navigate(R.id.action_loginFragment_to_notesFragment)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = LoginFragmentBinding.inflate(inflater, container, false)
}
