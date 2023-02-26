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

package dev.shreyaspatil.noty.simpleapp.view.notes

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.simpleapp.R
import dev.shreyaspatil.noty.simpleapp.databinding.NotesFragmentBinding
import dev.shreyaspatil.noty.simpleapp.view.base.BaseFragment
import dev.shreyaspatil.noty.simpleapp.view.hiltNotyMainNavGraphViewModels
import dev.shreyaspatil.noty.simpleapp.view.notes.adapter.NotesListAdapter
import dev.shreyaspatil.noty.utils.autoCleaned
import dev.shreyaspatil.noty.utils.ext.hide
import dev.shreyaspatil.noty.utils.ext.setDrawableLeft
import dev.shreyaspatil.noty.utils.ext.show
import dev.shreyaspatil.noty.utils.ext.showDialog
import dev.shreyaspatil.noty.view.state.NotesState
import dev.shreyaspatil.noty.view.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesFragment : BaseFragment<NotesFragmentBinding, NotesState, NotesViewModel>() {

    override val viewModel: NotesViewModel by hiltNotyMainNavGraphViewModels()

    private val notesListAdapter by autoCleaned(initializer = { NotesListAdapter(::onNoteClicked) })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
    }

    override fun initView() {
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
                setOnRefreshListener { viewModel.syncNotes() }
            }
        }
    }

    override fun render(state: NotesState) {
        binding.swipeRefreshNotes.isRefreshing = state.isLoading
        binding.swipeRefreshNotes.isEnabled = state.isConnectivityAvailable == true

        val errorMessage = state.error
        if (errorMessage != null) {
            toast("Error: $errorMessage")
        }

        val notes = state.notes
        if (notes.isNotEmpty()) {
            onNotesLoaded(notes)
        }

        if (state.isUserLoggedIn == false) {
            logout()
        }

        val isConnectivityAvailable = state.isConnectivityAvailable
        if (isConnectivityAvailable != null) {
            if (isConnectivityAvailable) {
                onConnectivityAvailable()
            } else {
                onConnectivityUnavailable()
            }
        }
    }

    private fun onNotesLoaded(data: List<Note>) {
        binding.emptyStateLayout.run {
            if (data.isEmpty()) show() else hide()
        }
        notesListAdapter.submitList(data)
    }

    private fun onNoteClicked(note: Note) {
        findNavController().navigate(
            NotesFragmentDirections.actionNotesFragmentToNoteDetailFragment(note.id)
        )
    }

    private fun onConnectivityUnavailable() {
        with(binding) {
            textNetworkStatus.apply {
                setDrawableLeft(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_connectivity_unavailable
                    )
                )
                text = getString(R.string.text_no_connectivity)
            }

            networkStatusLayout.apply {
                setBackgroundColor(
                    ResourcesCompat.getColor(resources, R.color.error, requireActivity().theme)
                )
            }.also { it.show() }
        }
    }

    private fun onConnectivityAvailable() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            if (shouldSyncNotes()) { viewModel.syncNotes() }
        }
        with(binding) {
            textNetworkStatus.apply {
                setDrawableLeft(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_connectivity_available
                    )
                )
                text = getString(R.string.text_connectivity)
            }

            networkStatusLayout.apply {
                setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.success,
                        requireActivity().theme
                    )
                )
            }.also {
                it.animate()
                    .alpha(1f)
                    .setStartDelay(ANIMATION_DURATION)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            it.hide()
                        }
                    })
            }
        }
    }

    private fun isConnected(): Boolean? = viewModel.currentState.isConnectivityAvailable

    private fun shouldSyncNotes() = viewModel.currentState.error != null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = NotesFragmentBinding.inflate(inflater, container, false)

    private fun confirmLogout() {
        showDialog(
            title = "Logout?",
            message = "Sure want to logout?",
            positiveActionText = "Yes",
            positiveAction = { _, _ -> viewModel.logout() },
            negativeActionText = "No",
            negativeAction = { dialog, _ ->
                dialog.dismiss()
            }
        )
    }

    private fun logout() {
        val destination = NotesFragmentDirections.actionNotesFragmentToLoginFragment()
        if (isAdded) {
            with(findNavController()) {
                currentDestination?.getAction(destination.actionId)?.let {
                    navigate(destination)
                }
            }
        } else {
            return
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            object : MenuProvider {

                override fun onPrepareMenu(menu: Menu) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        when (viewModel.isDarkModeEnabled()) {
                            true -> {
                                menu.findItem(R.id.action_dark_mode).isVisible = false
                            }
                            false -> {
                                menu.findItem(R.id.action_light_mode).isVisible = false
                            }
                        }
                        super.onPrepareMenu(menu)
                    }
                }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.main_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.action_light_mode -> viewModel.setDarkMode(false)
                        R.id.action_dark_mode -> viewModel.setDarkMode(true)
                        R.id.action_about ->
                            findNavController().navigate(R.id.action_notesFragment_to_aboutFragment)
                        R.id.action_logout -> confirmLogout()
                    }
                    return false
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    companion object {
        const val ANIMATION_DURATION = 2000L
    }
}
