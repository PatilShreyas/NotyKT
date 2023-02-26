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

package dev.shreyaspatil.noty.simpleapp.view.detail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.simpleapp.R
import dev.shreyaspatil.noty.simpleapp.databinding.NoteDetailFragmentBinding
import dev.shreyaspatil.noty.simpleapp.view.base.BaseFragment
import dev.shreyaspatil.noty.utils.ext.hideKeyboard
import dev.shreyaspatil.noty.utils.ext.showDialog
import dev.shreyaspatil.noty.utils.ext.toStringOrEmpty
import dev.shreyaspatil.noty.utils.saveBitmap
import dev.shreyaspatil.noty.utils.share.shareImage
import dev.shreyaspatil.noty.utils.share.shareNoteText
import dev.shreyaspatil.noty.view.state.NoteDetailState
import dev.shreyaspatil.noty.view.viewmodel.NoteDetailViewModel
import javax.inject.Inject

@AndroidEntryPoint
class NoteDetailFragment :
    BaseFragment<NoteDetailFragmentBinding, NoteDetailState, NoteDetailViewModel>() {

    private val args: NoteDetailFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelAssistedFactory: NoteDetailViewModel.Factory

    /**
     * Since we are continuously listening to the [NoteDetailState] for state updates, we get
     * initial title and note from this model. Also, we continuously tell [NoteDetailViewModel]
     * about changes to title and note and that ViewModel again let us know about the changes
     * through the new state. So this forms a continuous cycle of events which can then lead
     * to the issues. So using this field, we can make sure that whether note is loaded or not.
     * Once the note is loaded initially, we won't respect further state changes of title and notes.
     */
    private var isNoteLoaded = false

    private var pinMenuItem: MenuItem? = null

    override val viewModel: NoteDetailViewModel by viewModels {
        args.noteId?.let { noteId ->
            NoteDetailViewModel.provideFactory(viewModelAssistedFactory, noteId)
        } ?: throw IllegalStateException("'noteId' shouldn't be null")
    }

    private val requestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            shareImage()
        } else {
            showErrorDialog(
                title = getString(R.string.dialog_title_failed_image_share),
                message = getString(R.string.dialog_message_failed_image_share)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
    }

    override fun initView() {
        binding.run {
            fabSave.setOnClickListener { viewModel.save() }
            noteLayout.run {
                fieldTitle.addTextChangedListener { viewModel.setTitle(it.toStringOrEmpty()) }
                fieldNote.addTextChangedListener { viewModel.setNote(it.toStringOrEmpty()) }
            }
        }
    }

    override fun render(state: NoteDetailState) {
        binding.fabSave.isVisible = state.showSave

        val title = state.title
        val note = state.note

        if (title != null && note != null && !isNoteLoaded) {
            isNoteLoaded = true
            binding.noteLayout.fieldTitle.setText(title)
            binding.noteLayout.fieldNote.setText(note)
        }

        if (state.finished) {
            hideKeyboard()
            findNavController().navigateUp()
        }

        val errorMessage = state.error
        if (errorMessage != null) {
            toast("Error: $errorMessage")
        }

        updatePinnedIcon(state.isPinned)
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onPrepareMenu(menu: Menu) {
                    pinMenuItem = menu.findItem(R.id.action_pin)

                    super.onPrepareMenu(menu)
                }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.note_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.action_delete -> confirmNoteDeletion()
                        R.id.action_pin -> viewModel.togglePin()
                        R.id.action_share_text -> shareText()
                        R.id.action_share_image -> shareImage()
                    }
                    return false
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    private fun updatePinnedIcon(isPinned: Boolean) {
        pinMenuItem?.run {
            val icon = if (isPinned) R.drawable.ic_pinned else R.drawable.ic_unpinned
            setIcon(icon)
        }
    }

    private fun shareText() {
        val title = binding.noteLayout.fieldTitle.text.toString()
        val note = binding.noteLayout.fieldNote.text.toString()

        requireContext().shareNoteText(title, note)
    }

    private fun shareImage() {
        if (!isStoragePermissionGranted()) {
            requestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }

        val imageUri = binding.noteLayout.noteContentLayout.drawToBitmap().let { bitmap ->
            saveBitmap(requireActivity(), bitmap)
        } ?: run {
            toast("Error occurred!")
            return
        }

        requireContext().shareImage(imageUri)
    }

    private fun isStoragePermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = NoteDetailFragmentBinding.inflate(inflater, container, false)

    private fun confirmNoteDeletion() {
        showDialog(
            title = "Delete?",
            message = "Sure want to delete the note?",
            positiveActionText = "Yes",
            positiveAction = { _, _ ->
                viewModel.delete()
            },
            negativeActionText = "No",
            negativeAction = { dialog, _ ->
                dialog.dismiss()
            }
        )
    }
}
