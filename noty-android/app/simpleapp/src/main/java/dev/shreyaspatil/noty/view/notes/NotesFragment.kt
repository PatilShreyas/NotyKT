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

package dev.shreyaspatil.noty.view.notes

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.R
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.databinding.NotesFragmentBinding
import dev.shreyaspatil.noty.view.base.BaseFragment
import dev.shreyaspatil.noty.view.notes.adapter.NotesListAdapter
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NotesFragment : BaseFragment<NotesFragmentBinding, NotesViewModel>() {

    override val viewModel: NotesViewModel by viewModels()

    private val notesListAdapter by lazy { NotesListAdapter(::onNoteClicked) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeNotes()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        handleOnBackPressed()
        loadNotes()
    }

    override fun onStart() {
        super.onStart()
        checkAuthentication()
    }

    override fun onResume() {
        super.onResume()
        showActionBar()
    }

    private fun initViews() {
        binding.notesRecyclerView.adapter = notesListAdapter
        binding.fabNew.setOnClickListener {
            findNavController().navigate(R.id.action_notesFragment_to_addNoteFragment)
        }
    }

    private fun loadNotes() {
        viewModel.notesState.value.let { notesState ->
            if (notesState is ViewState.Success) {
                notesListAdapter.submitList(notesState.data)
            } else {
                viewModel.getAllNotes()
            }
        }
    }

    private fun observeNotes() {
        viewModel.notesState.observe(viewLifecycleOwner) {
            when (it) {
                is ViewState.Loading -> binding.progressBar.show()
                is ViewState.Success -> {
                    binding.progressBar.hide()
                    notesListAdapter.submitList(it.data)
                }
                is ViewState.Failed -> {
                    binding.progressBar.hide()
                    Log.e(javaClass.simpleName, it.message)
                    activity?.toast("Error ${it.message}")
                }
            }
        }
    }

    private fun checkAuthentication() {
        if (!viewModel.isUserLoggedIn()) {
            logout()
        }
    }

    private fun onNoteClicked(note: Note) {
        findNavController().navigate(
            NotesFragmentDirections.actionNotesFragmentToNoteDetailFragment(note.id)
        )
    }

    private fun logout() {
        findNavController().navigate(R.id.action_notesFragment_to_loginFragment)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = NotesFragmentBinding.inflate(inflater, container, false)

    override fun onPrepareOptionsMenu(menu: Menu) {
        viewLifecycleOwner.lifecycleScope.launch {
            when (viewModel.isDarkModeEnabled()) {
                true -> {
                    menu.findItem(R.id.action_dark_mode).isVisible = false
                }
                false -> {
                    menu.findItem(R.id.action_light_mode).isVisible = false
                }
            }
            super.onPrepareOptionsMenu(menu)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_light_mode -> viewModel.setDarkMode(false)

            R.id.action_dark_mode -> viewModel.setDarkMode(true)

            R.id.action_logout -> {
                viewModel.clearUserSession()
                logout()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun handleOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback {
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Exit?")
                .setMessage("Are you sure want to exit?")
                .setPositiveButton("YES") { dialogInterface: DialogInterface, i: Int ->
                    requireActivity().finish()
                }
                .setNegativeButton("NO") { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                }
                .show()
        }
    }
}
