package dev.shreyaspatil.noty.view.viewmodel

import dev.shreyaspatil.noty.base.ViewModelBehaviorSpec
import dev.shreyaspatil.noty.core.model.AuthCredential
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyUserRepository
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.testUtils.currentStateShouldBe
import dev.shreyaspatil.noty.testUtils.withState
import dev.shreyaspatil.noty.view.state.LoginState
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
class LoginViewModelTest : ViewModelBehaviorSpec() {

    private lateinit var repository: NotyUserRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: LoginViewModel

    @BeforeEach
    override fun setUp() {
        super.setUp()
        repository = mockk()
        sessionManager = mockk(relaxUnitFun = true)
        viewModel = LoginViewModel(repository, sessionManager)
    }

    @Test
    fun `initial state should be valid`() {
        val expectedState = LoginState(
            isLoading = false,
            isLoggedIn = false,
            error = null,
            username = "",
            password = "",
            isValidUsername = null,
            isValidPassword = null,
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
    fun `credentials should be validated and state updated when user provides incomplete credentials`() {
        val username = "john"
        val password = "eod"

        viewModel.setUsername(username)
        viewModel.setPassword(password)
        viewModel.login()

        viewModel.withState {
            assertTrue(isValidUsername!!)
            assertFalse(isValidPassword!!)
        }
        coVerify(exactly = 0) { repository.getUserByUsernameAndPassword(username, password) }
    }

    @Test
    fun `user should be retrieved and token saved when user uses valid credentials`() {
        val username = "johndoe1234"
        val password = "4321eodnhoj"
        val token = "Bearer TOKEN_ABC"

        viewModel.setUsername(username)
        viewModel.setPassword(password)
        coEvery { repository.getUserByUsernameAndPassword(username, password) } returns Either.success(AuthCredential(token))

        viewModel.login()

        coVerify { repository.getUserByUsernameAndPassword(username, password) }
        verify { sessionManager.saveToken(eq(token)) }
        viewModel.withState {
            assertTrue(isValidUsername!!)
            assertTrue(isValidPassword!!)
            assertFalse(isLoading)
            assertTrue(isLoggedIn)
            assertNull(error)
        }
    }

    @Test
    fun `state should contain error when repository fails to fulfill request`() {
        val username = "johndoe12345"
        val password = "54321eodnhoj"

        viewModel.setUsername(username)
        viewModel.setPassword(password)
        coEvery { repository.getUserByUsernameAndPassword(username, password) } returns Either.error("User not exist")

        viewModel.login()

        coVerify { repository.getUserByUsernameAndPassword(username, password) }
        viewModel.withState {
            assertFalse(isLoggedIn)
            assertEquals("User not exist", error)
        }
    }
}
