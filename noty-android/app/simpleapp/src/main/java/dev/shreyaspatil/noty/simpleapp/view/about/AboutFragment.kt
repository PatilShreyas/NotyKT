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

package dev.shreyaspatil.noty.simpleapp.view.about

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.simpleapp.BuildConfig
import dev.shreyaspatil.noty.simpleapp.R
import dev.shreyaspatil.noty.simpleapp.databinding.FragmentAboutBinding
import dev.shreyaspatil.noty.simpleapp.view.base.BaseFragment
import dev.shreyaspatil.noty.simpleapp.view.hiltNotyMainNavGraphViewModels
import dev.shreyaspatil.noty.view.state.AboutState
import dev.shreyaspatil.noty.view.viewmodel.AboutViewModel

@AndroidEntryPoint
class AboutFragment : BaseFragment<FragmentAboutBinding, AboutState, AboutViewModel>() {
    override val viewModel: AboutViewModel by hiltNotyMainNavGraphViewModels()

    override fun initView() {
        binding.run {
            textAppVersion.text = getString(
                R.string.text_app_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            )
            licenseCardView.setOnClickListener {
                launchBrowser(URL_LICENSE)
            }

            repoCardView.setOnClickListener {
                launchBrowser(URL_REPO)
            }
        }
    }

    override fun render(state: AboutState) {}

    private fun launchBrowser(url: String) = Intent(Intent.ACTION_VIEW, Uri.parse(url)).also {
        startActivity(it)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAboutBinding.inflate(inflater, container, false)

    companion object {
        const val URL_REPO = "https://github.com/PatilShreyas/NotyKT"
        const val URL_LICENSE = "https://github.com/PatilShreyas/NotyKT/blob/master/LICENSE"
    }
}
