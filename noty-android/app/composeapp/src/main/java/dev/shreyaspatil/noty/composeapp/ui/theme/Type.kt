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

package dev.shreyaspatil.noty.composeapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.shreyaspatil.noty.composeapp.R

private val gilroy =
    FontFamily(
        Font(R.font.gilroy_regular),
        Font(R.font.gilroy_semibold, FontWeight.W600),
        Font(R.font.gilroy_bold, FontWeight.Bold),
    )

private val universalStd =
    FontFamily(
        Font(R.font.universal_std),
    )

// Material 3 Typography
val typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W600,
                fontSize = 30.sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W600,
                fontSize = 24.sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W600,
                fontSize = 20.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W600,
                fontSize = 20.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W600,
                fontSize = 18.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W600,
                fontSize = 16.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W600,
                fontSize = 16.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W500,
                fontSize = 14.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W500,
                fontSize = 14.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = universalStd,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = universalStd,
                fontSize = 14.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = universalStd,
                fontSize = 12.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W500,
                fontSize = 14.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = gilroy,
                fontWeight = FontWeight.W500,
                fontSize = 12.sp,
            ),
    )
