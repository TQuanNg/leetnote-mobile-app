package com.example.leetnote.ui.screens.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.leetnote.data.model.EvaluationDTO
import com.example.leetnote.data.model.EvaluationDetailDTO
import com.example.leetnote.data.model.EvaluationListItemDTO
import com.example.leetnote.data.model.LeetcodeStatsDTO
import com.example.leetnote.data.model.UserProfileDTO
import com.example.leetnote.data.repository.EvaluationRepository
import com.example.leetnote.data.repository.LeetcodeRepository
import com.example.leetnote.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userRepository: UserRepository
    private lateinit var leetcodeRepository: LeetcodeRepository
    private lateinit var evaluationRepository: EvaluationRepository
    private lateinit var viewModel: ProfileViewModel

    // Test data
    private val sampleUserProfile = UserProfileDTO(
        id = 1L,
        email = "test@example.com",
        username = "testuser",
        profileUrl = "https://example.com/profile.jpg"
    )

    private val sampleLeetcodeStats = LeetcodeStatsDTO(
        username = "leetcodeuser",
        totalSolved = 150,
        easySolved = 50,
        mediumSolved = 75,
        hardSolved = 25
    )

    private val sampleEvaluationListItem = EvaluationListItemDTO(
        evaluationId = 1L,
        problemId = 1L,
        problemTitle = "Two Sum",
        createdAt = "2025-01-15"
    )

    private val sampleEvaluationDetail = EvaluationDetailDTO(
        evaluationId = 1L,
        problemId = 1L,
        problemTitle = "Two Sum",
        difficulty = "Easy",
        createdAt = "2025-01-15",
        evaluation = EvaluationDTO(
            rating = 8,
            issue = listOf("Could optimize further"),
            feedback = listOf("Good approach")
        ),
        solutionText = "class Solution { /* code */ }"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk()
        leetcodeRepository = mockk()
        evaluationRepository = mockk()

        // Mock default behavior for init calls
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============================================================
    // Initial State Tests
    // ============================================================

    @Test
    fun `initial state should have default values`() = runTest {
        // Mock init calls
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(sampleUserProfile.email, state.email)
            assertEquals(sampleUserProfile.username, state.username)
            assertEquals(sampleUserProfile.profileUrl, state.profileImageUrl)
            assertEquals(0, state.selectedTabIndex)
            assertTrue(state.evaluations.isEmpty())
            assertNull(state.selectedEvaluationDetail)
        }
    }

    @Test
    fun `initial loading state should be false after init`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.isLoading.test {
            assertFalse(awaitItem())
        }
    }

    // ============================================================
    // User Profile Tests
    // ============================================================

    @Test
    fun `loadUserProfile should update state with user data on success`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(sampleUserProfile.email, state.email)
            assertEquals(sampleUserProfile.username, state.username)
            assertEquals(sampleUserProfile.profileUrl, state.profileImageUrl)
        }

        coVerify { userRepository.getUserProfile() }
    }

    @Test
    fun `loadUserProfile should set error on failure`() = runTest {
        val errorMessage = "Network error"
        coEvery { userRepository.getUserProfile() } throws IOException(errorMessage)
        // Make leetcode profile succeed so it doesn't override the error
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns null

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Since loadLeetCodeProfile silently fails and clears error, we won't see the user profile error
        // Let's test by calling loadUserProfile again explicitly
        viewModel.loadUserProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals(errorMessage, awaitItem())
        }
    }

    @Test
    fun `loadUserProfile should set loading to false after completion`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.isLoading.test {
            assertFalse(awaitItem())
        }
    }

    // ============================================================
    // Update Username Tests
    // ============================================================

    @Test
    fun `updateUsername should update state on success`() = runTest {
        val newUsername = "newuser"
        val updatedProfile = sampleUserProfile.copy(username = newUsername)

        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { userRepository.setUsername(newUsername) } returns updatedProfile

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateUsername(newUsername)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(newUsername, state.username)
        }

        coVerify { userRepository.setUsername(newUsername) }
    }

    @Test
    fun `updateUsername should set error on failure`() = runTest {
        val errorMessage = "Username already exists"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { userRepository.setUsername(any()) } throws RuntimeException(errorMessage)

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateUsername("newuser")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals(errorMessage, awaitItem())
        }
    }

    // ============================================================
    // Profile Image Tests
    // ============================================================

    @Test
    fun `uploadProfileImage should update state on success`() = runTest {
        val imageUrl = "https://example.com/new-image.jpg"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { userRepository.uploadProfileImage(imageUrl) } returns imageUrl

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uploadProfileImage(imageUrl)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(imageUrl, state.profileImageUrl)
        }

        coVerify { userRepository.uploadProfileImage(imageUrl) }
    }

    @Test
    fun `uploadProfileImage should set error on failure`() = runTest {
        val errorMessage = "Upload failed"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { userRepository.uploadProfileImage(any()) } throws IOException(errorMessage)

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uploadProfileImage("https://example.com/image.jpg")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals(errorMessage, awaitItem())
        }
    }

    @Test
    fun `deleteProfileImage should clear profile image on success`() = runTest {
        val profileWithImage = sampleUserProfile.copy(profileUrl = "https://example.com/image.jpg")
        coEvery { userRepository.getUserProfile() } returns profileWithImage
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { userRepository.deleteProfileImage(any()) } returns Unit

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteProfileImage()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.profileImageUrl)
        }

        coVerify { userRepository.deleteProfileImage(profileWithImage.profileUrl!!) }
    }

    @Test
    fun `deleteProfileImage should not call repository when image is null`() = runTest {
        val profileWithoutImage = sampleUserProfile.copy(profileUrl = null)
        coEvery { userRepository.getUserProfile() } returns profileWithoutImage
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteProfileImage()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.profileImageUrl)
        }

        coVerify(exactly = 0) { userRepository.deleteProfileImage(any()) }
    }

    @Test
    fun `deleteProfileImage should set error on failure`() = runTest {
        val errorMessage = "Delete failed"
        val profileWithImage = sampleUserProfile.copy(profileUrl = "https://example.com/image.jpg")
        coEvery { userRepository.getUserProfile() } returns profileWithImage
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { userRepository.deleteProfileImage(any()) } throws IOException(errorMessage)

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteProfileImage()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals(errorMessage, awaitItem())
        }
    }

    // ============================================================
    // LeetCode Profile Tests
    // ============================================================

    @Test
    fun `loadLeetCodeProfile should update state on success`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(sampleLeetcodeStats.username, state.leetcodeUsername)
            assertEquals(sampleLeetcodeStats.totalSolved, state.solvedCount)
            assertEquals(sampleLeetcodeStats.easySolved, state.solvedEasy)
            assertEquals(sampleLeetcodeStats.mediumSolved, state.solvedMedium)
            assertEquals(sampleLeetcodeStats.hardSolved, state.solvedHard)
        }

        coVerify { leetcodeRepository.getLeetcodeProfile() }
    }

    @Test
    fun `loadLeetCodeProfile should handle null response silently`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns null

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.leetcodeUsername)
            assertNull(state.solvedCount)
        }

        viewModel.error.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `loadLeetCodeProfile should silently fail on exception`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } throws IOException("Network error")

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should not set error (silently fails)
        viewModel.error.test {
            assertNull(awaitItem())
        }
    }

    // ============================================================
    // Set LeetCode Username Tests
    // ============================================================

    @Test
    fun `setLeetCodeUsername should update state on success`() = runTest {
        val username = "newleetcodeuser"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns null
        coEvery { leetcodeRepository.setLeetcodeUsername(username) } returns sampleLeetcodeStats.copy(username = username)

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setLeetCodeUsername(username)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(username, state.leetcodeUsername)
            assertEquals(sampleLeetcodeStats.totalSolved, state.solvedCount)
        }

        coVerify { leetcodeRepository.setLeetcodeUsername(username) }
    }

    @Test
    fun `setLeetCodeUsername should set error when username is blank`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns null

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setLeetCodeUsername("")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals("LeetCode username can't be empty", awaitItem())
        }

        coVerify(exactly = 0) { leetcodeRepository.setLeetcodeUsername(any()) }
    }

    @Test
    fun `setLeetCodeUsername should set error when repository returns null`() = runTest {
        val username = "invaliduser"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns null
        coEvery { leetcodeRepository.setLeetcodeUsername(username) } returns null

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setLeetCodeUsername(username)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals("Failed to fetch LeetCode stats. Please check the username.", awaitItem())
        }
    }

    @Test
    fun `setLeetCodeUsername should set error on exception`() = runTest {
        val errorMessage = "Network error"
        val username = "testuser"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns null
        coEvery { leetcodeRepository.setLeetcodeUsername(username) } throws IOException(errorMessage)

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setLeetCodeUsername(username)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals("Error: $errorMessage", awaitItem())
        }
    }

    // ============================================================
    // Update LeetCode Username Tests
    // ============================================================

    @Test
    fun `updateLeetCodeUsername should update state on success`() = runTest {
        val newUsername = "updateduser"
        val updatedStats = sampleLeetcodeStats.copy(username = newUsername)
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { leetcodeRepository.updateLeetcodeUsername(newUsername) } returns updatedStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateLeetCodeUsername(newUsername)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(newUsername, state.leetcodeUsername)
        }

        coVerify { leetcodeRepository.updateLeetcodeUsername(newUsername) }
    }

    @Test
    fun `updateLeetCodeUsername should set error when username is blank`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateLeetCodeUsername("  ")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals("LeetCode username can't be empty", awaitItem())
        }

        coVerify(exactly = 0) { leetcodeRepository.updateLeetcodeUsername(any()) }
    }

    @Test
    fun `updateLeetCodeUsername should set error when repository returns null`() = runTest {
        val username = "invaliduser"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { leetcodeRepository.updateLeetcodeUsername(username) } returns null

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateLeetCodeUsername(username)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals("Failed to update LeetCode username. Please check the username.", awaitItem())
        }
    }

    @Test
    fun `updateLeetCodeUsername should set error on exception`() = runTest {
        val errorMessage = "Connection timeout"
        val username = "testuser"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { leetcodeRepository.updateLeetcodeUsername(username) } throws IOException(errorMessage)

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateLeetCodeUsername(username)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals("Error: $errorMessage", awaitItem())
        }
    }

    // ============================================================
    // Refresh LeetCode Stats Tests
    // ============================================================

    @Test
    fun `refreshLeetCodeStats should update state on success`() = runTest {
        val refreshedStats = sampleLeetcodeStats.copy(totalSolved = 200)
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { leetcodeRepository.refreshLeetcodeStats() } returns refreshedStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refreshLeetCodeStats()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(200, state.solvedCount)
        }

        coVerify { leetcodeRepository.refreshLeetcodeStats() }
    }

    @Test
    fun `refreshLeetCodeStats should set error when repository returns null`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { leetcodeRepository.refreshLeetcodeStats() } returns null

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refreshLeetCodeStats()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals("Failed to refresh LeetCode stats", awaitItem())
        }
    }

    @Test
    fun `refreshLeetCodeStats should set error on exception`() = runTest {
        val errorMessage = "API rate limit exceeded"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { leetcodeRepository.refreshLeetcodeStats() } throws RuntimeException(errorMessage)

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refreshLeetCodeStats()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals("Error: $errorMessage", awaitItem())
        }
    }

    // ============================================================
    // Evaluation Tests
    // ============================================================

    @Test
    fun `loadAllUserEvaluations should update state on success`() = runTest {
        val evaluations = listOf(sampleEvaluationListItem)
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { evaluationRepository.getAllUserEvaluations() } returns evaluations

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadAllUserEvaluations()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.evaluations.size)
            assertEquals(sampleEvaluationListItem, state.evaluations[0])
        }

        coVerify { evaluationRepository.getAllUserEvaluations() }
    }

    @Test
    fun `loadAllUserEvaluations should set error on failure`() = runTest {
        val errorMessage = "Failed to fetch evaluations"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { evaluationRepository.getAllUserEvaluations() } throws IOException(errorMessage)

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadAllUserEvaluations()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals("Failed to load evaluations: $errorMessage", awaitItem())
        }
    }

    @Test
    fun `loadAllUserEvaluations should set loading states correctly`() = runTest {
        val evaluations = listOf(sampleEvaluationListItem)
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { evaluationRepository.getAllUserEvaluations() } returns evaluations

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadAllUserEvaluations()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.isLoading.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `getEvaluationDetail should update state on success`() = runTest {
        val evaluationId = 1L
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { evaluationRepository.getEvaluationById(evaluationId) } returns sampleEvaluationDetail

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getEvaluationDetail(evaluationId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(sampleEvaluationDetail, state.selectedEvaluationDetail)
        }

        coVerify { evaluationRepository.getEvaluationById(evaluationId) }
    }

    @Test
    fun `getEvaluationDetail should set error on failure`() = runTest {
        val evaluationId = 1L
        val errorMessage = "Evaluation not found"
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats
        coEvery { evaluationRepository.getEvaluationById(evaluationId) } throws RuntimeException(errorMessage)

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getEvaluationDetail(evaluationId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.error.test {
            assertEquals("Failed to load evaluation detail: $errorMessage", awaitItem())
        }
    }

    // ============================================================
    // Tab Selection Tests
    // ============================================================

    @Test
    fun `selectTab should update selected tab index`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectTab(1)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.selectedTabIndex)
        }
    }

    @Test
    fun `updateTabIndex should update selected tab index`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateTabIndex(2)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.selectedTabIndex)
        }
    }

    @Test
    fun `multiple tab selections should update correctly`() = runTest {
        coEvery { userRepository.getUserProfile() } returns sampleUserProfile
        coEvery { leetcodeRepository.getLeetcodeProfile() } returns sampleLeetcodeStats

        viewModel = ProfileViewModel(userRepository, leetcodeRepository, evaluationRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectTab(1)
        viewModel.selectTab(0)
        viewModel.selectTab(2)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.selectedTabIndex)
        }
    }
}

