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

package dev.shreyaspatil.noty.view.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.databinding.FragmentAboutBinding
import dev.shreyaspatil.noty.view.base.BaseFragment
import dev.shreyaspatil.noty.view.viewmodel.AboutViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AboutFragment : BaseFragment<FragmentAboutBinding, AboutViewModel>() {
    override val viewModel: AboutViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.run {
            licenseCardView.setOnClickListener {
                viewModel.setURL(LICENSE)
            }

            repoCardView.setOnClickListener {
                viewModel.setURL(REPO)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        observeURL()
    }

    private fun observeURL() {
        viewModel.url.observe(viewLifecycleOwner) {
            openURL(it.toString())
        }
    }

    private fun openURL(url: String) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(i)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAboutBinding.inflate(inflater, container, false)

    companion object {
        const val REPO = "https://github.com/PatilShreyas/NotyKT"
        const val LICENSE = "https://github.com/PatilShreyas/NotyKT/blob/master/LICENSE"
    }
}