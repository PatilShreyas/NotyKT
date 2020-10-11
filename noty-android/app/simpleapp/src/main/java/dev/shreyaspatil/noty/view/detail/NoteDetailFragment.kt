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

package dev.shreyaspatil.noty.view.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.R
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.databinding.NoteDetailFragmentBinding
import dev.shreyaspatil.noty.view.base.BaseFragment
import dev.shreyaspatil.noty.view.viewmodel.NoteDetailViewModel
import javax.inject.Inject
import kotlinx.android.synthetic.main.content_note_layout.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NoteDetailFragment : BaseFragment<NoteDetailFragmentBinding, NoteDetailViewModel>() {

    private val args: NoteDetailFragmentArgs by navArgs()

    @Inject
    lateinit var myViewModelAssistedFactory: NoteDetailViewModel.AssistedFactory

    override val viewModel: NoteDetailViewModel by viewModels {
        args.noteId?.let { noteId ->
            NoteDetailViewModel.provideFactory(myViewModelAssistedFactory, noteId)
        } ?: throw IllegalStateException("'noteId' shouldn't be null")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        observeNote()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun initViews() {
        binding.fabSave.setOnClickListener {
            val (title, note) = binding.noteLayout.let {
                Pair(
                    it.fieldTitle.text.toString(),
                    it.fieldNote.text.toString()
                )
            }
            viewModel.updateNote(title, note)
        }
    }

    private fun observeNote() {
        viewModel.run {
            noteLiveData.observe(viewLifecycleOwner) {
                binding.run {
                    fieldTitle.setText(it.title)
                    fieldNote.setText(it.note)
                    fabSave.isEnabled = true
                }
            }

            updateNoteState.observe(viewLifecycleOwner) { viewState ->
                when (viewState) {
                    is ViewState.Loading -> {
                        // TODO Show Loading
                    }
                    is ViewState.Success -> findNavController().navigateUp()
                    is ViewState.Failed -> {
                        // TODO Show error message
                    }
                }
            }

            deleteNoteState.observe(viewLifecycleOwner) { viewState ->
                when (viewState) {
                    is ViewState.Loading -> {
                        // TODO Show Loading
                    }
                    is ViewState.Success -> findNavController().navigateUp()
                    is ViewState.Failed -> {
                        // TODO Show error message
                    }
                }
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = NoteDetailFragmentBinding.inflate(inflater, container, false)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.note_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> viewModel.deleteNote()
        }
        return super.onOptionsItemSelected(item)
    }
}
