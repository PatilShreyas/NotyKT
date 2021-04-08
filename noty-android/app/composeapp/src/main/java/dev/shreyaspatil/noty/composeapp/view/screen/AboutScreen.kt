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

package dev.shreyaspatil.noty.composeapp.view.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.shreyaspatil.noty.composeapp.BuildConfig
import dev.shreyaspatil.noty.composeapp.R
import dev.shreyaspatil.noty.composeapp.utils.IntentUtils

@Composable
fun AboutScreen(navController: NavController) {

    Scaffold(
        topBar = {
            AboutAppBar(navController = navController)
        },
        content = {
            AboutColumn()
        }
    )
}

@Composable
fun AboutAppBar(navController: NavController) {
    TopAppBar(
        title = {
            Row {
                val image = painterResource(id = R.drawable.ic_noty_logo)
                Image(painter = image, contentDescription = "Noty Icon")
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Noty",
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        navigationIcon = {
            IconButton(
                modifier = Modifier.padding(4.dp, 0.dp, 0.dp, 0.dp),
                onClick = {
                    navController.navigateUp()
                }
            ) {
                Icon(
                    painterResource(R.drawable.ic_baseline_arrow_back),
                    "Back",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        },
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onPrimary,
        elevation = 0.dp
    )
}

@Composable
fun AboutColumn() {
    LazyColumn(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
        item {
            val image = painterResource(id = R.drawable.noty_app_logo)
            Image(
                modifier = Modifier.size(92.dp, 92.dp),
                painter = image,
                contentDescription = "About Noty app",
                alignment = Alignment.Center
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            Text(
                text = "Noty",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onBackground
                )
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Text(
                text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle2
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            LicenseCard()
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            VisitCard()
        }
    }
}

@Composable
fun LicenseCard() {
    Card(shape = RoundedCornerShape(4.dp), elevation = 2.dp) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {

            val licenseTitle = stringResource(id = R.string.text_license_title)
            Text(
                text = licenseTitle,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val license = stringResource(id = R.string.text_license)
            Text(
                text = license,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun VisitCard() {
    Card(shape = RoundedCornerShape(4.dp), elevation = 2.dp) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {

            val visit = stringResource(id = R.string.text_visit)
            Text(
                text = visit,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val context = LocalContext.current
            val visitUrl = stringResource(id = R.string.text_repo_url)
            Box(
                Modifier.clickable(
                    onClick = {
                        IntentUtils.launchBrowser(context = context, visitUrl)
                    }
                )
            ) {
                Text(
                    text = visitUrl,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.primaryColor),
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                )
            }
        }
    }
}
