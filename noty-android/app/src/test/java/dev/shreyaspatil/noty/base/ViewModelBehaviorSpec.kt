package dev.shreyaspatil.noty.base

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import dev.shreyaspatil.noty.core.test.NotyCoroutineTestRule
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Base spec for testing ViewModel.
 *
 * This class provides common setup for testing ViewModels, including:
 * - [InstantTaskExecutorRule] for synchronous LiveData updates.
 * - [NotyCoroutineTestRule] for managing Coroutine dispatchers.
 * - MockK initialization and cleanup.
 */
@ExtendWith(NotyCoroutineTestRule::class)
abstract class ViewModelBehaviorSpec {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}
