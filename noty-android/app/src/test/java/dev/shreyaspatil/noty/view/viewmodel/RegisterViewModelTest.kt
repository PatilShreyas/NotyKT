package dev.shreyaspatil.noty.view.viewmodel

import dev.shreyaspatil.noty.base.ViewModelBehaviorSpec
import dev.shreyaspatil.noty.core.model.AuthCredential
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyUserRepository
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.testUtils.currentStateShouldBe
import dev.shreyaspatil.noty.testUtils.withState
import dev.shreyaspatil.noty.view.state.RegisterState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest : ViewModelBehaviorSpec() {

    private lateinit var repository: NotyUserRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: RegisterViewModel

    @BeforeEach
    override fun setUp() {
        super.setUp()
        repository = mockk()
        sessionManager = mockk(relaxUnitFun = true)
        viewModel = RegisterViewModel(repository, sessionManager)
    }

    @Test
    fun `initial state should be valid`() {
        val expectedState = RegisterState(
            isLoading = false,
            isLoggedIn = false,
            error = null,
            username = "",
            password = "",
            confirmPassword = "",
            isValidUsername = null,
            isValidPassword = null,
            isValidConfirmPassword = null,
        )
        viewModel currentStateShouldBe expectedState
    }

    @Test
    fun `username should be updated in the current state`() {
        val username = "johndoe"
        viewModel.setUsername(username)
        viewModel.withState { assertEquals(username, this.username) }
    }

    @Test
    fun `password should be updated in the current state`() {
        val password = "eodnhoj"
        viewModel.setPassword(password)
        viewModel.withState { assertEquals(password, this.password) }
    }

    @Test
    fun `confirm password should be updated in the current state`() {
        val confirmPassword = "eodnhojabcd"
        viewModel.setConfirmPassword(confirmPassword)
        viewModel.withState { assertEquals(confirmPassword, this.confirmPassword) }
    }

    @Test
    fun `user should not be created and state should include invalid credentials when user provides incomplete credentials`() {
        val username = "joh"
        val password = "doe"
        val confirmPassword = ""

        viewModel.setUsername(username)
        viewModel.setPassword(password)
        viewModel.setConfirmPassword(confirmPassword)
        viewModel.register()

        coVerify(exactly = 0) { repository.addUser(username, password) }
        viewModel.withState {
            assertFalse(isValidUsername!!)
            assertFalse(isValidPassword!!)
            assertFalse(isValidConfirmPassword!!)
        }
    }

    @Test
    fun `user should be created and UI state should include error message when repository fails`() {
        val username = "john"
        val password = "doe12345"

        viewModel.setUsername(username)
        viewModel.setPassword(password)
        viewModel.setConfirmPassword(password)
        coEvery { repository.addUser(username, password) } returns Either.error("Invalid credentials")

        viewModel.register()

        coVerify { repository.addUser(username, password) }
        viewModel.withState { assertEquals("Invalid credentials", error) }
    }

    @Test
    fun `user should be created, token saved and UI state updated when credentials are valid`() {
        val username = "johndoe"
        val password = "eodnhoj1234"
        val token = "Bearer TOKEN_ABC"

        viewModel.setUsername(username)
        viewModel.setPassword(password)
        viewModel.setConfirmPassword(password)
        coEvery { repository.addUser(username, password) } returns Either.success(AuthCredential(token))

        viewModel.register()

        coVerify { repository.addUser(username, password) }
        verify { sessionManager.saveToken(eq(token)) }
        viewModel.withState {
            assertTrue(isLoggedIn)
            assertNull(error)
        }
    }
}
