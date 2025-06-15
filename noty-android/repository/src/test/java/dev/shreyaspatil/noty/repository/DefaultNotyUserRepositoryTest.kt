package dev.shreyaspatil.noty.repository

import com.squareup.moshi.adapter
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.utils.moshi
import dev.shreyaspatil.noty.data.remote.api.NotyAuthService
import dev.shreyaspatil.noty.data.remote.model.request.AuthRequest
import dev.shreyaspatil.noty.data.remote.model.response.AuthResponse
import dev.shreyaspatil.noty.data.remote.model.response.State
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNotyUserRepositoryTest {

    private lateinit var authService: FakeAuthService
    private lateinit var repository: DefaultNotyUserRepository

    @BeforeEach
    fun setUp() {
        authService = spyk(FakeAuthService())
        repository = DefaultNotyUserRepository(authService)
    }

    @Test
    fun `addUser with valid credentials should return success with token`() = runTest {
        val response = repository.addUser(username = "admin", password = "admin")

        coVerify { authService.register(AuthRequest("admin", "admin")) }
        val credentials = (response as Either.Success).data
        assertEquals("Bearer ABCD", credentials.token)
    }

    @Test
    fun `addUser with invalid credentials should return error`() = runTest {
        val response = repository.addUser(username = "john", password = "doe")

        coVerify { authService.register(AuthRequest("john", "doe")) }
        val message = (response as Either.Error).message
        assertEquals("Invalid credentials", message)
    }

    @Test
    fun `getUserByUsernameAndPassword with valid credentials should return success with token`() = runTest {
        val response = repository.getUserByUsernameAndPassword(username = "admin", password = "admin")

        coVerify { authService.login(AuthRequest("admin", "admin")) }
        val credentials = (response as Either.Success).data
        assertEquals("Bearer ABCD", credentials.token)
    }

    @Test
    fun `getUserByUsernameAndPassword with invalid credentials should return error`() = runTest {
        val response = repository.getUserByUsernameAndPassword(username = "john", password = "doe")

        coVerify { authService.login(AuthRequest("john", "doe")) }
        val message = (response as Either.Error).message
        assertEquals("Invalid credentials", message)
    }
}

class FakeAuthService : NotyAuthService {
    override suspend fun register(authRequest: AuthRequest): Response<AuthResponse> {
        return fakeAuthResponse(authRequest)
    }

    override suspend fun login(authRequest: AuthRequest): Response<AuthResponse> {
        return fakeAuthResponse(authRequest)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun fakeAuthResponse(authRequest: AuthRequest): Response<AuthResponse> {
        return if (authRequest.username == "admin" && authRequest.password == "admin") {
            Response.success(AuthResponse(State.SUCCESS, "Success", "Bearer ABCD"))
        } else {
            val response = AuthResponse(State.UNAUTHORIZED, "Invalid credentials", null)
            val body = ResponseBody.create(
                "application/json".toMediaTypeOrNull(),
                moshi.adapter<AuthResponse>().toJson(response),
            )
            Response.error(401, body)
        }
    }
}
