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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.R
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.databinding.NotesFragmentBinding
import dev.shreyaspatil.noty.utils.NetworkUtils
import dev.shreyaspatil.noty.utils.hide
import dev.shreyaspatil.noty.utils.show
import dev.shreyaspatil.noty.view.base.BaseFragment
import dev.shreyaspatil.noty.view.notes.adapter.NotesListAdapter
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NotesFragment : BaseFragment<NotesFragmentBinding, NotesViewModel>() {

    override val viewModel: NotesViewModel by viewModels()

    private val notesListAdapter = NotesListAdapter(::onNoteClicked)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        checkAuthentication()
        observeNotes()
        observeConnectivity()
        loadNotes()
    }

    private fun initViews() {
        binding.run {
            notesRecyclerView.adapter = notesListAdapter
            fabNew.setOnClickListener {
                findNavController().navigate(R.id.action_notesFragment_to_addNoteFragment)
            }
            swipeRefreshNotes.apply {
                setColorSchemeColors(
                    ContextCompat.getColor(requireContext(), R.color.secondaryColor),
                    ContextCompat.getColor(requireContext(), R.color.onSecondary)
                )
                setOnRefreshListener {
                    viewModel.getAllNotes()
                }
            }
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
                is ViewState.Loading -> binding.swipeRefreshNotes.isRefreshing = true
                is ViewState.Success -> onNotesLoaded(it.data).also {
                    binding.swipeRefreshNotes.isRefreshing = false
                }

                is ViewState.Failed -> {
                    binding.swipeRefreshNotes.isRefreshing = false
                    Log.e(javaClass.simpleName, it.message)
                    toast("Error: ${it.message}")
                }
            }
        }
    }

    private fun onNotesLoaded(data: List<Note>) {
        binding.emptyStateLayout.run {
            if (data.isEmpty()) show() else hide()
        }
        notesListAdapter.submitList(data)
    }

    private fun observeConnectivity() {
        NetworkUtils.observeConnectivity(applicationContext())
            .observe(viewLifecycleOwner) { isConnected ->
                if (isConnected) onConnectivityAvailable() else onConnectivityUnavailable()
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

    private fun onConnectivityUnavailable() {
        binding.run {
            // Get icon from drawable & Set [ic_connectivity_unavailable] icon to the start of the textView
            val offlineDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_connectivity_unavailable)
            binding.textNetworkStatus.setCompoundDrawablesWithIntrinsicBounds(
                offlineDrawable,
                null,
                null,
                null
            )
            textNetworkStatus.text = getString(R.string.text_no_connectivity)
            networkStatusLayout.apply {
                show()
                setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.error,
                        requireActivity().theme
                    )
                )
            }
        }
    }

    private fun onConnectivityAvailable() {
        if (viewModel.notesState.value is ViewState.Failed ||
            notesListAdapter.itemCount == 0
        ) {
            viewModel.getAllNotes()
        }
        binding.run {
            // Get icon from the drawable & Set [ic_connectivity_available] icon to the start of the textView
            val onlineDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_connectivity_available)
            binding.textNetworkStatus.setCompoundDrawablesWithIntrinsicBounds(
                onlineDrawable,
                null,
                null,
                null
            )
            textNetworkStatus.text = getString(R.string.text_connectivity)
            networkStatusLayout.apply {
                setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.success,
                        requireActivity().theme
                    )
                )
                animate()
                    .alpha(1f)
                    .setStartDelay(ANIMATION_DURATION)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            hide()
                        }
                    })
                    .start()
            }
        }
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

    companion object {
        const val ANIMATION_DURATION = 2000L
    }
}
