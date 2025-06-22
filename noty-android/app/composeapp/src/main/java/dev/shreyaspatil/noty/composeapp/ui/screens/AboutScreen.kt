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

package dev.shreyaspatil.noty.composeapp.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.noty.composeapp.BuildConfig
import dev.shreyaspatil.noty.composeapp.R
import dev.shreyaspatil.noty.composeapp.component.NotyIcon
import dev.shreyaspatil.noty.composeapp.component.scaffold.NotyScaffold
import dev.shreyaspatil.noty.composeapp.component.scaffold.NotyTopAppBar
import dev.shreyaspatil.noty.composeapp.ui.theme.typography
import dev.shreyaspatil.noty.composeapp.utils.IntentUtils
import dev.shreyaspatil.noty.composeapp.utils.NotyPreview

@Composable
fun AboutScreen(onNavigateUp: () -> Unit) {
    NotyScaffold(
        notyTopAppBar = {
            NotyTopAppBar(onNavigateUp = onNavigateUp, title = "About", showNotyIcon = false)
        },
        content = {
            AboutContent(Modifier.padding(it))
        },
    )
}

@Composable
fun AboutContent(modifier: Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NotyIcon(Modifier.requiredSize(92.dp))

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Noty",
            style = MaterialTheme.typography.displayMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.titleSmall,
        )

        Spacer(modifier = Modifier.height(24.dp))

        LicenseCard()

        Spacer(modifier = Modifier.height(8.dp))

        VisitCard()
    }
}

@Composable
fun LicenseCard() {
    Card {
        Column(
            modifier =
                Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
        ) {
            val licenseTitle = stringResource(id = R.string.text_license_title)
            Text(
                text = licenseTitle,
                style = typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))

            val license = stringResource(id = R.string.text_license)
            Text(
                text = license,
                textAlign = TextAlign.Center,
                style = typography.bodyMedium,
            )
        }
    }
}

@Composable
fun VisitCard() {
    Card {
        Column(
            modifier =
                Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
        ) {
            val visit = stringResource(id = R.string.text_visit)
            Text(
                text = visit,
                style = typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val context = LocalContext.current
            val visitUrl = stringResource(id = R.string.text_repo_url)
            Box(
                Modifier.clickable(
                    onClick = {
                        IntentUtils.launchBrowser(context = context, visitUrl)
                    },
                ),
            ) {
                Text(
                    text = visitUrl,
                    color = MaterialTheme.colorScheme.primary,
                    style = typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                )
            }
        }
    }
}

@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewAboutScreen() {
    NotyPreview {
        AboutScreen { }
    }
}
