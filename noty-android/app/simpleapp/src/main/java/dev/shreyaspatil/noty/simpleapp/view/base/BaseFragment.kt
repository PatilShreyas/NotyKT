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

package dev.shreyaspatil.noty.simpleapp.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import dev.shreyaspatil.noty.simpleapp.view.custom.ErrorDialog
import dev.shreyaspatil.noty.simpleapp.view.custom.ProgressDialog
import dev.shreyaspatil.noty.utils.autoCleaned
import dev.shreyaspatil.noty.view.state.State
import dev.shreyaspatil.noty.view.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Base for all fragments in the project
 */
abstract class BaseFragment<VB : ViewBinding, STATE : State, VM : BaseViewModel<STATE>> :
    Fragment() {

    private var _binding: VB by autoCleaned()
    val binding: VB get() = _binding

    protected abstract val viewModel: VM

    private var progressDialog: ProgressDialog? = null
    private var errorDialog: ErrorDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    abstract fun initView()
    abstract fun render(state: STATE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeState()
    }

    private fun observeState() {
        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> render(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    fun showProgressDialog(show: Boolean) = if (show) showProgressDialog() else hideProgressDialog()

    fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog()
        }
        progressDialog?.let {
            if (!it.isVisible && !it.isAdded) {
                it.show(requireActivity().supportFragmentManager, TAG_PROGRESS_DIALOG)
            }
        }
    }

    fun hideProgressDialog() = progressDialog?.dismiss()

    fun showErrorDialog(
        title: String,
        message: String,
        onDialogDismiss: () -> Unit = {}
    ) {
        if (errorDialog == null) {
            errorDialog = ErrorDialog()
        }
        errorDialog?.apply {
            this.title = title
            this.message = message
            this.onDialogDismiss = onDialogDismiss
        }
        errorDialog?.let {
            if (!it.isVisible && !it.isAdded) {
                it.show(requireActivity().supportFragmentManager, TAG_ERROR_DIALOG)
            }
        }
    }

    fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        progressDialog?.dismiss()
        progressDialog = null

        errorDialog?.dismiss()
        errorDialog = null

        super.onDestroyView()
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    companion object {
        private const val TAG_PROGRESS_DIALOG = "progress_dialog"
        private const val TAG_ERROR_DIALOG = "error_dialog"
    }
}
