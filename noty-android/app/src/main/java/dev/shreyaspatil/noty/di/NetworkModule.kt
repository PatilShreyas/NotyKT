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

package dev.shreyaspatil.noty.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.shreyaspatil.noty.core.utils.moshi
import dev.shreyaspatil.noty.data.remote.Constant
import dev.shreyaspatil.noty.data.remote.api.NotyAuthService
import dev.shreyaspatil.noty.data.remote.api.NotyService
import dev.shreyaspatil.noty.data.remote.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    private val baseRetrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(Constant.API_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))

    private val okHttpClientBuilder: OkHttpClient.Builder =
        OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)

    @Provides
    fun provideNotyService(authInterceptor: AuthInterceptor): NotyService {
        return baseRetrofitBuilder
            .client(okHttpClientBuilder.addInterceptor(authInterceptor).build())
            .build()
            .create(NotyService::class.java)
    }

    @Provides
    fun provideNotyAuthService(): NotyAuthService {
        return baseRetrofitBuilder
            .client(okHttpClientBuilder.build())
            .build()
            .create(NotyAuthService::class.java)
    }
}
