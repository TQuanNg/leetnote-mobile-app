package com.example.leetnote.ui.screens.solving

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.leetnote.data.model.ProblemDetailDTO
import com.example.leetnote.data.model.SolutionDTO
import com.example.leetnote.data.model.SubmissionDTO
import com.example.leetnote.data.repository.EvaluationDetail
import com.example.leetnote.data.repository.EvaluationDTO
import com.example.leetnote.data.repository.EvaluationDetailDTO
import com.example.leetnote.data.repository.EvaluationListItemDTO
import com.example.leetnote.data.repository.EvaluationRepository
import com.example.leetnote.data.repository.ProblemRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class SolvingPageViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var evaluationRepository: EvaluationRepository
    private lateinit var problemRepository: ProblemRepository
    private lateinit var viewModel: SolvingPageViewModel

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

    private val sampleSubmission = SubmissionDTO.Submission(
        id = 1L,
        problemId = 1L,
        solutionText = "def twoSum(nums, target):\n    # solution code\n    pass",
        createdAt = "2025-10-14T12:00:00Z"
    )

    private val sampleEvaluationDTO = EvaluationDTO(
        rating = 85,
        issue = listOf("Missing edge case handling"),
        feedback = listOf("Good approach", "Consider optimization")
    )

    private val sampleEvaluation = EvaluationDetail(
        id = 1L,
        version = 1,
        evaluation = sampleEvaluationDTO,
        createdAt = "2025-10-14T12:05:00Z"
    )

    private val sampleEvaluationDetailDTO = EvaluationDetailDTO(
        evaluationId = 1L,
        problemId = 1L,
        problemTitle = "Two Sum",
        difficulty = "Easy",
        createdAt = "2025-10-14T12:05:00Z",
        evaluation = sampleEvaluationDTO,
        solutionText = "def twoSum(nums, target):\n    # solution code\n    pass"
    )

    private val sampleEvaluationListItem = EvaluationListItemDTO(
        evaluationId = 1L,
        problemId = 1L,
        problemTitle = "Two Sum",
        createdAt = "2025-10-14T12:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        evaluationRepository = mockk()
        problemRepository = mockk()

        // Mock Android Log to prevent "Log not mocked" errors
        mockkStatic(android.util.Log::class)
        coEvery { android.util.Log.e(any(), any(), any<Throwable>()) } returns 0
        coEvery { android.util.Log.d(any(), any()) } returns 0

        viewModel = SolvingPageViewModel(evaluationRepository, problemRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `initial state should have empty and null values`() = runTest {
        // Given - ViewModel is initialized

        // When - Getting initial state
        val problemDetail = viewModel.problemDetail.first()
        val solutionText = viewModel.solutionText.first()
        val isLoading = viewModel.isLoading.first()
        val lastSubmission = viewModel.lastSubmission.first()
        val lastEvaluation = viewModel.lastEvaluation.first()
        val allEvaluations = viewModel.allEvaluations.first()
        val evaluationResult = viewModel.evaluationResult.first()
        val error = viewModel.error.first()

        // Then - All values should be in initial state
        assertNull(problemDetail)
        assertEquals("", solutionText)
        assertFalse(isLoading)
        assertNull(lastSubmission)
        assertNull(lastEvaluation)
        assertEquals(emptyList<EvaluationListItemDTO>(), allEvaluations)
        assertNull(evaluationResult)
        assertNull(error)
    }

    @Test
    fun `onSolutionTextChange should update solution text and clear error`() = runTest {
        // Given
        val newText = "def solution():\n    return 42"

        // Set initial error
        viewModel.clearError()
        viewModel.onSolutionTextChange("test")

        // When
        viewModel.onSolutionTextChange(newText)

        // Then
        assertEquals(newText, viewModel.solutionText.first())
        assertNull(viewModel.error.first())
    }

    @Test
    fun `clearError should reset error to null`() = runTest {
        // Given - Simulate error state by loading invalid problem
        coEvery { problemRepository.getProblemDetail(any()) } throws RuntimeException("Test error")

        viewModel.loadProblemDetail(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error is set
        assertTrue(viewModel.error.first()?.contains("Test error") == true)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.first())
    }

    @Test
    fun `loadProblemDetail should load problem successfully`() = runTest {
        // Given
        val problemId = 1L
        coEvery { problemRepository.getProblemDetail(problemId) } returns sampleProblemDetail

        // When
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(sampleProblemDetail, viewModel.problemDetail.first())
        assertNull(viewModel.error.first())

        coVerify { problemRepository.getProblemDetail(problemId) }
    }

    @Test
    fun `loadProblemDetail should handle exception and set error message`() = runTest {
        // Given
        val problemId = 1L
        val errorMsg = "Network error"
        coEvery { problemRepository.getProblemDetail(problemId) } throws RuntimeException(errorMsg)

        // When
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.problemDetail.first())
        assertEquals("Failed to load problem detail: $errorMsg", viewModel.error.first())

        coVerify { problemRepository.getProblemDetail(problemId) }
    }

    @Test
    fun `submitSolution should not submit if solution text is blank`() = runTest {
        // Given - Empty solution text
        assertEquals("", viewModel.solutionText.first())

        // When
        viewModel.submitSolution(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - No repository calls should be made
        coVerify(exactly = 0) { evaluationRepository.createEvaluation(any()) }
        assertFalse(viewModel.isLoading.first())
    }

    @Test
    fun `submitSolution should submit successfully and navigate to evaluation`() = runTest {
        // Given
        val problemId = 1L
        val solutionText = "def solution():\n    return 'answer'"

        viewModel.onSolutionTextChange(solutionText)
        coEvery { evaluationRepository.createEvaluation(any()) } returns sampleEvaluation

        // When & Then - Test navigation flow
        viewModel.navigateToEvaluation.test {
            viewModel.submitSolution(problemId)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            assertFalse(viewModel.isLoading.first())
            assertEquals(sampleEvaluation, viewModel.evaluationResult.first())
            assertNull(viewModel.error.first())

            // Should emit navigation event
            assertEquals(sampleEvaluation, awaitItem())

            coVerify {
                evaluationRepository.createEvaluation(
                    SubmissionDTO.SubmissionRequest(problemId, solutionText)
                )
            }
        }
    }

    @Test
    fun `submitSolution should handle timeout and set error message`() = runTest {
        // Given
        val problemId = 1L
        val solutionText = "def solution():\n    return 'test'"

        viewModel.onSolutionTextChange(solutionText)
        // Simulate a timeout by throwing a custom timeout exception that will be caught by the outer catch block
        coEvery { evaluationRepository.createEvaluation(any()) } throws RuntimeException("Request timeout")

        // When
        viewModel.submitSolution(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoading.first())
        assertNull(viewModel.evaluationResult.first())
        assertEquals("Unexpected error: Request timeout", viewModel.error.first())

        coVerify { evaluationRepository.createEvaluation(any()) }
    }

    @Test
    fun `submitSolution should handle IOException and set network error message`() = runTest {
        // Given
        val problemId = 1L
        val solutionText = "def solution():\n    return 'test'"

        viewModel.onSolutionTextChange(solutionText)
        coEvery { evaluationRepository.createEvaluation(any()) } throws IOException("Connection failed")

        // When
        viewModel.submitSolution(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoading.first())
        assertNull(viewModel.evaluationResult.first())
        assertEquals("Cannot connect to server. Please check your network.", viewModel.error.first())

        coVerify { evaluationRepository.createEvaluation(any()) }
    }

    @Test
    fun `submitSolution should handle generic exception and set error message`() = runTest {
        // Given
        val problemId = 1L
        val solutionText = "def solution():\n    return 'test'"
        val errorMsg = "Unexpected error occurred"

        viewModel.onSolutionTextChange(solutionText)
        coEvery { evaluationRepository.createEvaluation(any()) } throws RuntimeException(errorMsg)

        // When
        viewModel.submitSolution(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoading.first())
        assertNull(viewModel.evaluationResult.first())
        assertEquals("Unexpected error: $errorMsg", viewModel.error.first())

        coVerify { evaluationRepository.createEvaluation(any()) }
    }

    @Test
    fun `fetchLastSubmission should load submission successfully`() = runTest {
        // Given
        val problemId = 1L
        coEvery { evaluationRepository.getLastSubmission(problemId) } returns sampleSubmission

        // When
        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(sampleSubmission, viewModel.lastSubmission.first())
        assertFalse(viewModel.isLoading.first())

        coVerify { evaluationRepository.getLastSubmission(problemId) }
    }

    @Test
    fun `fetchLastSubmission should update solution text if empty`() = runTest {
        // Given
        val problemId = 1L
        assertEquals("", viewModel.solutionText.first()) // Initially empty

        coEvery { evaluationRepository.getLastSubmission(problemId) } returns sampleSubmission

        // When
        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(sampleSubmission.solutionText, viewModel.solutionText.first())
        assertEquals(sampleSubmission, viewModel.lastSubmission.first())

        coVerify { evaluationRepository.getLastSubmission(problemId) }
    }

    @Test
    fun `fetchLastSubmission should not update solution text if not empty`() = runTest {
        // Given
        val problemId = 1L
        val existingSolution = "existing solution code"

        viewModel.onSolutionTextChange(existingSolution)
        coEvery { evaluationRepository.getLastSubmission(problemId) } returns sampleSubmission

        // When
        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(existingSolution, viewModel.solutionText.first()) // Should remain unchanged
        assertEquals(sampleSubmission, viewModel.lastSubmission.first())

        coVerify { evaluationRepository.getLastSubmission(problemId) }
    }

    @Test
    fun `fetchLastSubmission should handle exception and set null`() = runTest {
        // Given
        val problemId = 1L
        coEvery { evaluationRepository.getLastSubmission(problemId) } throws RuntimeException("API error")

        // When
        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.lastSubmission.first())
        assertFalse(viewModel.isLoading.first())

        coVerify { evaluationRepository.getLastSubmission(problemId) }
    }

    @Test
    fun `fetchLastEvaluation should load evaluation successfully`() = runTest {
        // Given
        val problemId = 1L
        coEvery { evaluationRepository.getNewEvaluation(problemId) } returns sampleEvaluation

        // When
        viewModel.fetchLastEvaluation(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(sampleEvaluation, viewModel.lastEvaluation.first())
        assertFalse(viewModel.isLoading.first())

        coVerify { evaluationRepository.getNewEvaluation(problemId) }
    }

    @Test
    fun `fetchLastEvaluation should handle exception and set null`() = runTest {
        // Given
        val problemId = 1L
        coEvery { evaluationRepository.getNewEvaluation(problemId) } throws RuntimeException("API error")

        // When
        viewModel.fetchLastEvaluation(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.lastEvaluation.first())
        assertFalse(viewModel.isLoading.first())

        coVerify { evaluationRepository.getNewEvaluation(problemId) }
    }

    @Test
    fun `loading state should be managed correctly during operations`() = runTest {
        // Given
        val problemId = 1L
        coEvery { evaluationRepository.getLastSubmission(problemId) } coAnswers {
            kotlinx.coroutines.delay(100) // Simulate network delay
            sampleSubmission
        }

        // When & Then - Test loading state flow
        viewModel.isLoading.test {
            assertEquals(false, awaitItem()) // Initial state

            viewModel.fetchLastSubmission(problemId)

            // Note: Due to how coroutines work with test dispatcher,
            // we need to advance time to see loading state changes
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(false, awaitItem()) // Final state after completion
        }
    }

    @Test
    fun `multiple concurrent operations should handle loading state correctly`() = runTest {
        // Given
        val problemId = 1L
        coEvery { evaluationRepository.getLastSubmission(problemId) } returns sampleSubmission
        coEvery { evaluationRepository.getNewEvaluation(problemId) } returns sampleEvaluation

        // When - Start multiple operations
        viewModel.fetchLastSubmission(problemId)
        viewModel.fetchLastEvaluation(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Both operations should complete successfully
        assertEquals(sampleSubmission, viewModel.lastSubmission.first())
        assertEquals(sampleEvaluation, viewModel.lastEvaluation.first())
        assertFalse(viewModel.isLoading.first())

        coVerify { evaluationRepository.getLastSubmission(problemId) }
        coVerify { evaluationRepository.getNewEvaluation(problemId) }
    }

    @Test
    fun `solution text changes should clear error messages`() = runTest {
        // Given - Set initial error
        coEvery { problemRepository.getProblemDetail(any()) } throws RuntimeException("Test error")
        viewModel.loadProblemDetail(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error is set
        assertTrue(viewModel.error.first()?.contains("Test error") == true)

        // When - Change solution text
        viewModel.onSolutionTextChange("new solution")

        // Then - Error should be cleared
        assertNull(viewModel.error.first())
        assertEquals("new solution", viewModel.solutionText.first())
    }
}
