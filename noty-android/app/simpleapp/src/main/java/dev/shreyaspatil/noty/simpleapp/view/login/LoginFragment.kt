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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.simpleapp.R
import dev.shreyaspatil.noty.simpleapp.databinding.LoginFragmentBinding
import dev.shreyaspatil.noty.simpleapp.view.base.BaseFragment
import dev.shreyaspatil.noty.simpleapp.view.hiltNotyMainNavGraphViewModels
import dev.shreyaspatil.noty.utils.validator.AuthValidator
import dev.shreyaspatil.noty.view.viewmodel.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : BaseFragment<LoginFragmentBinding, LoginViewModel>() {

    override val viewModel: LoginViewModel by hiltNotyMainNavGraphViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initViews()
    }

    private fun initData() {
        viewModel.authFlow.asLiveData().observe(viewLifecycleOwner) { viewState ->
            when (viewState) {
                is ViewState.Loading -> showProgressDialog()
                is ViewState.Success -> {
                    hideProgressDialog()
                    onAuthSuccess()
                }
                is ViewState.Failed -> {
                    hideProgressDialog()
                    showErrorDialog(
                        title = getString(R.string.dialog_title_login_failed),
                        message = viewState.message
                    )
                }
            }
        }
    }

    private fun initViews() {
        binding.buttonLogin.setOnClickListener { onLoginClicked() }
        binding.textSignUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun onLoginClicked() {
        val username = binding.textFieldUsername.editText?.text.toString()
        val password = binding.textFieldPassword.editText?.text.toString()

        if (validate(username, password)) {
            viewModel.login(username, password)
        }
    }

    private fun onAuthSuccess() {
        findNavController().navigate(R.id.action_loginFragment_to_notesFragment)
    }

    private fun validate(username: String, password: String): Boolean {
        return with(binding) {
            when {
                !AuthValidator.isValidUsername(username) -> {
                    textFieldUsername.error = getString(R.string.message_field_username_invalid)
                    false
                }

                !AuthValidator.isValidPassword(password) -> {
                    textFieldPassword.error = getString(R.string.message_field_password_invalid)
                    false
                }

                else -> true
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = LoginFragmentBinding.inflate(inflater, container, false)
}
