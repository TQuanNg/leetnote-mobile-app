package com.example.leetnote.ui.screens.problem

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.leetnote.data.model.ProblemDetailDTO
import com.example.leetnote.data.model.SolutionDTO
import com.example.leetnote.data.repository.HomeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class ProblemDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: HomeRepository
    private lateinit var viewModel: ProblemDetailViewModel

    // Test data
    private val sampleSolution = SolutionDTO(
        approach = "Two Pointers",
        code = "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Implementation\n    }\n}",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1)"
    )

    private val sampleProblemDetail = ProblemDetailDTO(
        id = 1L,
        title = "Two Sum",
        description = "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
        difficulty = "Easy",
        isFavorite = false,
        isSolved = false,
        solution = sampleSolution
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()

        // Mock Android Log to prevent "Log not mocked" errors
        mockkStatic(android.util.Log::class)
        coEvery { android.util.Log.e(any(), any(), any<Throwable>()) } returns 0

        viewModel = ProblemDetailViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `initial state should have null values and no loading`() = runTest {
        // Given - ViewModel is initialized

        // When - Getting initial state
        val problemDetail = viewModel.problemDetail.first()
        val isLoading = viewModel.isDetailLoading.first()
        val errorMessage = viewModel.errorMessage.first()

        // Then - All values should be in initial state
        assertNull(problemDetail)
        assertFalse(isLoading)
        assertNull(errorMessage)
    }

    @Test
    fun `loadProblemDetail should update loading state correctly during successful load`() = runTest {
        // Given
        val problemId = 1L
        coEvery { repository.getProblemDetail(problemId) } returns sampleProblemDetail

        // When - Initial state check
        assertFalse(viewModel.isDetailLoading.first())

        // When - Start loading
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Loading should be complete
        assertFalse(viewModel.isDetailLoading.first())
        assertEquals(sampleProblemDetail, viewModel.problemDetail.first())
        assertNull(viewModel.errorMessage.first())
    }

    @Test
    fun `loadProblemDetail should load problem detail successfully`() = runTest {
        // Given
        val problemId = 1L
        coEvery { repository.getProblemDetail(problemId) } returns sampleProblemDetail

        // When
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(sampleProblemDetail, viewModel.problemDetail.first())
        assertFalse(viewModel.isDetailLoading.first())
        assertNull(viewModel.errorMessage.first())

        coVerify { repository.getProblemDetail(problemId) }
    }

    @Test
    fun `loadProblemDetail should handle repository exception and set error message`() = runTest {
        // Given
        val problemId = 1L
        val errorMsg = "Network error occurred"
        coEvery { repository.getProblemDetail(problemId) } throws RuntimeException(errorMsg)

        // When
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.problemDetail.first())
        assertFalse(viewModel.isDetailLoading.first())
        assertEquals("Failed to load problem detail: $errorMsg", viewModel.errorMessage.first())

        coVerify { repository.getProblemDetail(problemId) }
    }

    @Test
    fun `loadProblemDetail should handle IOException and set appropriate error message`() = runTest {
        // Given
        val problemId = 1L
        val ioException = IOException("Connection failed")
        coEvery { repository.getProblemDetail(problemId) } throws ioException

        // When
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.problemDetail.first())
        assertFalse(viewModel.isDetailLoading.first())
        assertEquals("Failed to load problem detail: Connection failed", viewModel.errorMessage.first())

        coVerify { repository.getProblemDetail(problemId) }
    }

    @Test
    fun `loadProblemDetail should handle timeout exception`() = runTest {
        // Given
        val problemId = 1L
        val timeoutException = SocketTimeoutException("Request timeout")
        coEvery { repository.getProblemDetail(problemId) } throws timeoutException

        // When
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.problemDetail.first())
        assertFalse(viewModel.isDetailLoading.first())
        assertEquals("Failed to load problem detail: Request timeout", viewModel.errorMessage.first())

        coVerify { repository.getProblemDetail(problemId) }
    }

    @Test
    fun `loadProblemDetail should clear previous error before loading`() = runTest {
        // Given - Set initial error state
        val problemId = 1L
        coEvery { repository.getProblemDetail(problemId) } throws RuntimeException("First error")

        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error is set
        assertEquals("Failed to load problem detail: First error", viewModel.errorMessage.first())

        // When - Load successfully
        coEvery { repository.getProblemDetail(problemId) } returns sampleProblemDetail

        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Error should be cleared
        assertEquals(sampleProblemDetail, viewModel.problemDetail.first())
        assertNull(viewModel.errorMessage.first())
    }

    @Test
    fun `loadProblemDetail should handle different problem IDs correctly`() = runTest {
        // Given
        val problemId1 = 1L
        val problemId2 = 2L

        val problemDetail2 = sampleProblemDetail.copy(
            id = 2L,
            title = "Add Two Numbers",
            difficulty = "Medium"
        )

        coEvery { repository.getProblemDetail(problemId1) } returns sampleProblemDetail
        coEvery { repository.getProblemDetail(problemId2) } returns problemDetail2

        // When - Load first problem
        viewModel.loadProblemDetail(problemId1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(sampleProblemDetail, viewModel.problemDetail.first())

        // When - Load second problem
        viewModel.loadProblemDetail(problemId2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(problemDetail2, viewModel.problemDetail.first())

        coVerify { repository.getProblemDetail(problemId1) }
        coVerify { repository.getProblemDetail(problemId2) }
    }

    @Test
    fun `multiple loadProblemDetail calls should handle correctly`() = runTest {
        // Given
        val problemId = 1L
        coEvery { repository.getProblemDetail(problemId) } returns sampleProblemDetail

        // When - Make multiple rapid calls
        viewModel.loadProblemDetail(problemId)
        viewModel.loadProblemDetail(problemId)
        viewModel.loadProblemDetail(problemId)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Final state should be correct
        assertEquals(sampleProblemDetail, viewModel.problemDetail.first())
        assertFalse(viewModel.isDetailLoading.first())
        assertNull(viewModel.errorMessage.first())

        // Repository should be called multiple times
        coVerify(exactly = 3) { repository.getProblemDetail(problemId) }
    }

    @Test
    fun `error message flow should emit distinct values`() = runTest {
        // Given
        val problemId = 1L
        val errorMsg = "Same error"
        coEvery { repository.getProblemDetail(problemId) } throws RuntimeException(errorMsg)

        // When
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Error message should be set correctly
        assertEquals("Failed to load problem detail: $errorMsg", viewModel.errorMessage.first())

        // When - Load again with same error
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Error message should still be there
        assertEquals("Failed to load problem detail: $errorMsg", viewModel.errorMessage.first())
    }

    @Test
    fun `problem detail flow should emit distinct values`() = runTest {
        // Given
        val problemId = 1L
        coEvery { repository.getProblemDetail(problemId) } returns sampleProblemDetail

        // When - Initial state
        assertNull(viewModel.problemDetail.first())

        // When - Load problem detail
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Problem detail should be loaded
        assertEquals(sampleProblemDetail, viewModel.problemDetail.first())
    }

    @Test
    fun `loadProblemDetail should handle edge case with zero problem ID`() = runTest {
        // Given
        val problemId = 0L
        coEvery { repository.getProblemDetail(problemId) } returns sampleProblemDetail.copy(id = 0L)

        // When
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(0L, viewModel.problemDetail.first()?.id)
        assertFalse(viewModel.isDetailLoading.first())
        assertNull(viewModel.errorMessage.first())

        coVerify { repository.getProblemDetail(problemId) }
    }

    @Test
    fun `loadProblemDetail should handle edge case with negative problem ID`() = runTest {
        // Given
        val problemId = -1L
        val errorMsg = "Invalid problem ID"
        coEvery { repository.getProblemDetail(problemId) } throws IllegalArgumentException(errorMsg)

        // When
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.problemDetail.first())
        assertFalse(viewModel.isDetailLoading.first())
        assertEquals("Failed to load problem detail: $errorMsg", viewModel.errorMessage.first())

        coVerify { repository.getProblemDetail(problemId) }
    }

    @Test
    fun `loadProblemDetail should handle problem with all solution fields`() = runTest {
        // Given
        val problemId = 1L
        val complexSolution = SolutionDTO(
            approach = "Dynamic Programming with Memoization",
            code = "class Solution {\n    private Map<String, Integer> memo = new HashMap<>();\n    public int solve(int[] arr) {\n        // Complex implementation\n        return result;\n    }\n}",
            timeComplexity = "O(n * m)",
            spaceComplexity = "O(n * m)"
        )

        val complexProblem = sampleProblemDetail.copy(
            title = "Complex Problem",
            difficulty = "Hard",
            isFavorite = true,
            isSolved = true,
            solution = complexSolution
        )

        coEvery { repository.getProblemDetail(problemId) } returns complexProblem

        // When
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val result = viewModel.problemDetail.first()
        assertEquals(complexProblem, result)
        assertEquals("Dynamic Programming with Memoization", result?.solution?.approach)
        assertEquals("O(n * m)", result?.solution?.timeComplexity)
        assertEquals("O(n * m)", result?.solution?.spaceComplexity)
        assertTrue(result?.isFavorite == true)
        assertTrue(result?.isSolved == true)
    }

    @Test
    fun `state flows should be properly exposed`() = runTest {
        // Given - ViewModel is initialized

        // Then - All state flows should be accessible and have correct initial values
        assertNull(viewModel.problemDetail.first())
        assertFalse(viewModel.isDetailLoading.first())
        assertNull(viewModel.errorMessage.first())
    }

    @Test
    fun `loadProblemDetail should handle concurrent calls correctly`() = runTest {
        // Given
        val problemId1 = 1L
        val problemId2 = 2L

        val problem2 = sampleProblemDetail.copy(id = 2L, title = "Second Problem")

        coEvery { repository.getProblemDetail(problemId1) } coAnswers {
            kotlinx.coroutines.delay(100) // Simulate network delay
            sampleProblemDetail
        }

        coEvery { repository.getProblemDetail(problemId2) } returns problem2

        // When - Start first call and immediately start second call
        viewModel.loadProblemDetail(problemId1)
        viewModel.loadProblemDetail(problemId2) // This should execute while first is still running

        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Both calls should complete, latest result should be visible
        assertFalse(viewModel.isDetailLoading.first())
        assertNull(viewModel.errorMessage.first())

        // Both repository calls should have been made
        coVerify { repository.getProblemDetail(problemId1) }
        coVerify { repository.getProblemDetail(problemId2) }
    }
}
