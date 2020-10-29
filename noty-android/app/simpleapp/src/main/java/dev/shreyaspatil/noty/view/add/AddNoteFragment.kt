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

package dev.shreyaspatil.noty.view.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.trimmedLength
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.databinding.AddNoteFragmentBinding
import dev.shreyaspatil.noty.utils.NetworkUtils
import dev.shreyaspatil.noty.utils.hide
import dev.shreyaspatil.noty.utils.show
import dev.shreyaspatil.noty.view.base.BaseFragment
import dev.shreyaspatil.noty.view.viewmodel.AddNoteViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddNoteFragment : BaseFragment<AddNoteFragmentBinding, AddNoteViewModel>() {

    override val viewModel: AddNoteViewModel by viewModels()

    private val connectivityLiveData by lazy {
        NetworkUtils.observeConnectivity(applicationContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onStart() {
        super.onStart()
        observeAddNoteResult()
    }

    private fun initViews() {
        binding.run {
            fabSave.setOnClickListener { saveNote() }
            noteLayout.run {
                fieldTitle.addTextChangedListener { onNoteContentChanged() }
                fieldNote.addTextChangedListener { onNoteContentChanged() }
            }
        }
    }

    private fun onNoteContentChanged() {
        val (title, note) = getNoteContent()

        binding.fabSave.let { fab ->
            if (title.trimmedLength() < 4 || note.isBlank()) fab.hide() else fab.show()
        }
    }

    private fun saveNote() {
        if (connectivityLiveData.value != null && connectivityLiveData.value == false) {
            toast("No Internet! Try later")
            return
        }
        val (title, note) = getNoteContent()

        viewModel.addNote(title, note)
    }

    private fun getNoteContent() = binding.noteLayout.let {
        Pair(
            it.fieldTitle.text.toString(),
            it.fieldNote.text.toString()
        )
    }

    private fun observeAddNoteResult() {
        viewModel.addNoteState.observe(viewLifecycleOwner) { viewState ->
            when (viewState) {
                is ViewState.Loading -> binding.progressBar.show()

                is ViewState.Success -> {
                    binding.progressBar.hide()
                    findNavController().navigateUp()
                }

                is ViewState.Failed -> {
                    binding.progressBar.hide()
                    toast("Error ${viewState.message}")
                }
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = AddNoteFragmentBinding.inflate(inflater, container, false)
}
