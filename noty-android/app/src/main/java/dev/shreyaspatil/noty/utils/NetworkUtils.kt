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

package dev.shreyaspatil.noty.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dev.shreyaspatil.noty.core.connectivity.ConnectionState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

val Context.connectivityManager get(): ConnectivityManager {
    return getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}

/**
 * Network Utility to observe availability or unavailability of Internet connection
 */
fun ConnectivityManager.observeConnectivityAsFlow() =
    callbackFlow {
        trySend(currentConnectivityState.also { println("Initial state = $it") })

        val callback =
            NetworkCallback {
                trySend(evaluateNetworkState(this@observeConnectivityAsFlow, activeNetwork))
            }

        val networkRequest =
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

        registerNetworkCallback(networkRequest, callback)

        awaitClose {
            unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

/**
 * Network utility to get current state of internet connection
 */
val ConnectivityManager.currentConnectivityState: ConnectionState
    get() = evaluateNetworkState(this, activeNetwork)

/**
 * Helper function to evaluate network connectivity state
 */
private fun evaluateNetworkState(
    connectivityManager: ConnectivityManager,
    network: Network?,
): ConnectionState {
    if (network == null) {
        return ConnectionState.Unavailable
    }

    val capabilities = connectivityManager.getNetworkCapabilities(network)
    val hasInternetCapability = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    val isValidated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

    return if (hasInternetCapability && isValidated) {
        ConnectionState.Available
    } else {
        ConnectionState.Unavailable
    }
}

@Suppress("FunctionName")
fun NetworkCallback(callback: () -> Unit): ConnectivityManager.NetworkCallback {
    return object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            callback()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            callback()
        }

        override fun onLost(network: Network) {
            callback()
        }

        override fun onUnavailable() {
            callback()
        }
    }
}
