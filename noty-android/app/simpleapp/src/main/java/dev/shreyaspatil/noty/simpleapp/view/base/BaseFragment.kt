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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import dev.shreyaspatil.noty.simpleapp.view.custom.ProgressDialog
import dev.shreyaspatil.noty.simpleapp.view.custom.ErrorDialog

abstract class BaseFragment<VB : ViewBinding, VM : ViewModel> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

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

    fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog()
        }
        progressDialog?.let {
            if (!it.isVisible) {
                it.show(requireActivity().supportFragmentManager, TAG_PROGRESS_DIALOG)
            }
        }
    }

    fun hideProgressDialog() = progressDialog?.dismiss()

    fun showErrorDialog(title: String, message: String) {
        if (errorDialog == null) {
            errorDialog = ErrorDialog()
        }
        errorDialog?.apply {
            this.title = title
            this.message = message
        }
        errorDialog?.let {
            if (!it.isVisible) {
                it.show(requireActivity().supportFragmentManager, TAG_ERROR_DIALOG)
            }
        }
    }

    fun toast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun applicationContext(): Context = requireActivity().applicationContext

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        progressDialog?.dismiss()
        progressDialog = null

        errorDialog?.dismiss()
        errorDialog = null
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    companion object {
        private const val TAG_PROGRESS_DIALOG = "progress_dialog"
        private const val TAG_ERROR_DIALOG = "error_dialog"
    }
}
