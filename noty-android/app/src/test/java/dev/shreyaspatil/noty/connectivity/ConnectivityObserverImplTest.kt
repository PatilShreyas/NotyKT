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

package dev.shreyaspatil.noty.connectivity

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import dev.shreyaspatil.noty.core.connectivity.ConnectionState
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.S])
class ConnectivityObserverImplTest {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var connectivityObserver: ConnectivityObserverImpl
    private lateinit var network: Network
    private lateinit var networkCapabilities: NetworkCapabilities

    @Before
    fun setup() {
        connectivityManager = mockk(relaxed = true)
        network = mockk()
        networkCapabilities = mockk()

        connectivityObserver = ConnectivityObserverImpl(connectivityManager)
    }

    @Test
    fun `currentConnectionState should return Available when network has internet capability and is validated`() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

        // When
        val result = connectivityObserver.currentConnectionState

        // Then
        assertEquals(ConnectionState.Available, result)
    }

    @Test
    fun `currentConnectionState should return Unavailable when network has internet capability but is not validated`() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false

        // When
        val result = connectivityObserver.currentConnectionState

        // Then
        assertEquals(ConnectionState.Unavailable, result)
    }

    @Test
    fun `currentConnectionState should return Unavailable when network has no internet capability`() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

        // When
        val result = connectivityObserver.currentConnectionState

        // Then
        assertEquals(ConnectionState.Unavailable, result)
    }

    @Test
    fun `currentConnectionState should return Unavailable when no active network`() {
        // Given
        every { connectivityManager.activeNetwork } returns null

        // When
        val result = connectivityObserver.currentConnectionState

        // Then
        assertEquals(ConnectionState.Unavailable, result)
    }

    @Test
    fun `connectionState should emit initial state based on current connectivity`() =
        runTest {
            // Given
            every { connectivityManager.activeNetwork } returns network
            every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
            every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

            // Capture the callback to simulate network events
            val callbackSlot = slot<NetworkCallback>()
            every {
                connectivityManager.registerNetworkCallback(any<NetworkRequest>(), capture(callbackSlot))
            } answers { /* do nothing */ }

            // When
            val result = connectivityObserver.connectionState.first()

            // Then
            assertEquals(ConnectionState.Available, result)
            verify { connectivityManager.registerNetworkCallback(any<NetworkRequest>(), any<NetworkCallback>()) }
        }

    @Test
    fun `connectionState should emit network state changes through callbacks`() =
        runTest {
            // Given - Initial state is Unavailable (no active network)
            every { connectivityManager.activeNetwork } returns null

            // Capture the callback to simulate network events
            val callbackSlot = slot<NetworkCallback>()
            every { connectivityManager.registerNetworkCallback(any(), capture(callbackSlot)) } just Runs

            // When - Collect flow in background
            val results = mutableListOf<ConnectionState>()
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    connectivityObserver.connectionState.take(3).toList(results)
                }

            // Then - Simulate network state changes

            // 1. Network becomes available and validated
            every { connectivityManager.activeNetwork } returns network
            every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
            every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

            callbackSlot.captured.onAvailable(network) // Triggers re-evaluation

            // 2. Network becomes unavailable
            every { connectivityManager.activeNetwork } returns null

            callbackSlot.captured.onLost(network) // Triggers re-evaluation

            // Wait for collection to complete
            collectJob.cancel()

            // Verify we got the expected sequence of states
            println("Results = $results")
            assertEquals(3, results.size)
            assertEquals(ConnectionState.Unavailable, results[0]) // Initial state
            assertEquals(ConnectionState.Available, results[1]) // After network becomes available
            assertEquals(ConnectionState.Unavailable, results[2]) // After network is lost

            verify { connectivityManager.registerNetworkCallback(any<NetworkRequest>(), any<NetworkCallback>()) }
        }

    @Test
    fun `connectionState should emit state changes when capabilities change`() =
        runTest {
            // Given - Initial state with unvalidated network
            every { connectivityManager.activeNetwork } returns network
            every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
            every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false

            // Capture the callback to simulate network events
            val callbackSlot = slot<NetworkCallback>()
            every { connectivityManager.registerNetworkCallback(any(), capture(callbackSlot)) } just Runs

            // When - Collect flow in background
            val results = mutableListOf<ConnectionState>()
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    connectivityObserver.connectionState.take(2).toList(results)
                }

            // Then - Simulate network validation
            every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

            callbackSlot.captured.onCapabilitiesChanged(network, networkCapabilities) // Triggers re-evaluation

            // Wait for collection to complete
            collectJob.cancel()

            // Verify we got the expected sequence of states
            assertEquals(2, results.size)
            assertEquals(ConnectionState.Unavailable, results[0]) // Initial state (unvalidated)
            assertEquals(ConnectionState.Available, results[1]) // After validation

            verify { connectivityManager.registerNetworkCallback(any<NetworkRequest>(), any<NetworkCallback>()) }
        }
}
