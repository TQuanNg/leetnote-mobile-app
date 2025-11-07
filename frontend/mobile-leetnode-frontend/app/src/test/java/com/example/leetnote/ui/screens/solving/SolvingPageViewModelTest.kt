package com.example.leetnote.ui.screens.solving

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.leetnote.data.model.ProblemDetailDTO
import com.example.leetnote.data.model.SolutionDTO
import com.example.leetnote.data.model.SubmissionDTO
import com.example.leetnote.data.model.EvaluationDetail
import com.example.leetnote.data.model.EvaluationDTO
import com.example.leetnote.data.model.EvaluationListItemDTO
import com.example.leetnote.data.repository.EvaluationRepository
import com.example.leetnote.data.repository.ProblemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
        code = "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n" +
            "        // Implementation\n    }\n}",
        timeComplexity = "O(n)",
        spaceComplexity = "O(1)"
    )

    private val sampleProblemDetail = ProblemDetailDTO(
        id = 1L,
        title = "Two Sum",
        description = "Given an array of integers nums and an integer target, " +
            "return indices of the two numbers such that they add up to target.",
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
        val problemDetail = viewModel.problemDetail.value
        val solutionText = viewModel.solutionText.value
        val isLoading = viewModel.isLoading.value
        val lastSubmission = viewModel.lastSubmission.value
        val lastEvaluation = viewModel.lastEvaluation.value
        val allEvaluations = viewModel.allEvaluations.value
        val evaluationResult = viewModel.evaluationResult.value
        val error = viewModel.error.value

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
        assertEquals(newText, viewModel.solutionText.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `clearError should reset error to null`() = runTest {
        // Given - Simulate error state by loading invalid problem
        coEvery { problemRepository.getProblemDetail(any()) } throws RuntimeException("Test error")

        viewModel.loadProblemDetail(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error is set
        assertTrue(viewModel.error.value?.contains("Test error") == true)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
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
        assertEquals(sampleProblemDetail, viewModel.problemDetail.value)
        assertNull(viewModel.error.value)

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
        assertNull(viewModel.problemDetail.value)
        assertEquals("Failed to load problem detail: $errorMsg", viewModel.error.value)

        coVerify { problemRepository.getProblemDetail(problemId) }
    }

    @Test
    fun `submitSolution should not submit if solution text is blank`() = runTest {
        // Given - Empty solution text
        assertEquals("", viewModel.solutionText.value)

        // When
        viewModel.submitSolution(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - No repository calls should be made
        coVerify(exactly = 0) { evaluationRepository.createEvaluation(any()) }
        // Loading should remain false since we return early
        assertFalse(viewModel.isLoading.value)
        // Evaluation result should remain null
        assertNull(viewModel.evaluationResult.value)
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
            assertFalse(viewModel.isLoading.value)
            assertEquals(sampleEvaluation, viewModel.evaluationResult.value)
            assertNull(viewModel.error.value)

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
        // Simulate timeout by causing a delay longer than the timeout in ViewModel (10 seconds)
        coEvery { evaluationRepository.createEvaluation(any()) } coAnswers {
            kotlinx.coroutines.delay(15000) // This will cause timeout in withTimeout(10000L)
            sampleEvaluation
        }

        // When
        viewModel.submitSolution(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - timeout should result in null evaluation and error message
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.evaluationResult.value)
        assertEquals("Submission timed out. Please try again.", viewModel.error.value)

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
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.evaluationResult.value)
        assertEquals("Cannot connect to server. Please check your network.", viewModel.error.value)

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
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.evaluationResult.value)
        assertEquals("Unexpected error: $errorMsg", viewModel.error.value)

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
        assertEquals(sampleSubmission, viewModel.lastSubmission.value)
        assertFalse(viewModel.isLoading.value)

        coVerify { evaluationRepository.getLastSubmission(problemId) }
    }

    @Test
    fun `fetchLastSubmission should update solution text if empty`() = runTest {
        // Given
        val problemId = 1L
        assertEquals("", viewModel.solutionText.value) // Initially empty

        coEvery { evaluationRepository.getLastSubmission(problemId) } returns sampleSubmission

        // When
        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(sampleSubmission.solutionText, viewModel.solutionText.value)
        assertEquals(sampleSubmission, viewModel.lastSubmission.value)

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
        assertEquals(existingSolution, viewModel.solutionText.value) // Should remain unchanged
        assertEquals(sampleSubmission, viewModel.lastSubmission.value)

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
        assertNull(viewModel.lastSubmission.value)
        assertFalse(viewModel.isLoading.value)

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
        assertEquals(sampleEvaluation, viewModel.lastEvaluation.value)
        assertFalse(viewModel.isLoading.value)

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
        assertNull(viewModel.lastEvaluation.value)
        assertFalse(viewModel.isLoading.value)

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

        // When
        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - After completion, loading should be false
        assertFalse(viewModel.isLoading.value)
        assertEquals(sampleSubmission, viewModel.lastSubmission.value)
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
        assertEquals(sampleSubmission, viewModel.lastSubmission.value)
        assertEquals(sampleEvaluation, viewModel.lastEvaluation.value)
        assertFalse(viewModel.isLoading.value)

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
        assertTrue(viewModel.error.value?.contains("Test error") == true)

        // When - Change solution text
        viewModel.onSolutionTextChange("new solution")

        // Then - Error should be cleared
        assertNull(viewModel.error.value)
        assertEquals("new solution", viewModel.solutionText.value)
    }

    @Test
    fun `submitSolution with whitespace only solution should not submit`() = runTest {
        // Given - Solution text with only whitespace
        viewModel.onSolutionTextChange("   \n\t  ")

        // When
        viewModel.submitSolution(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - No repository calls should be made
        coVerify(exactly = 0) { evaluationRepository.createEvaluation(any()) }
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadProblemDetail should clear previous error before loading`() = runTest {
        // Given - Set initial error
        coEvery { problemRepository.getProblemDetail(1L) } throws RuntimeException("First error")
        viewModel.loadProblemDetail(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.error.value?.contains("First error") == true)

        // When - Load another problem successfully
        coEvery { problemRepository.getProblemDetail(2L) } returns sampleProblemDetail
        viewModel.loadProblemDetail(2L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Error should be cleared
        assertNull(viewModel.error.value)
        assertEquals(sampleProblemDetail, viewModel.problemDetail.value)
    }

    @Test
    fun `submitSolution should clear previous error before submitting`() = runTest {
        // Given - Set initial error and valid solution
        coEvery { problemRepository.getProblemDetail(any()) } throws RuntimeException("Test error")
        viewModel.loadProblemDetail(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.error.value != null)

        viewModel.onSolutionTextChange("valid solution code")
        coEvery { evaluationRepository.createEvaluation(any()) } returns sampleEvaluation

        // When
        viewModel.submitSolution(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Error should be cleared and evaluation result should be set
        assertNull(viewModel.error.value)
        assertEquals(sampleEvaluation, viewModel.evaluationResult.value)
    }

    @Test
    fun `fetchLastSubmission should return null when no previous submission exists`() = runTest {
        // Given
        val problemId = 1L
        coEvery { evaluationRepository.getLastSubmission(problemId) } returns null

        // When
        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.lastSubmission.value)
        assertEquals("", viewModel.solutionText.value) // Should remain empty
    }

    @Test
    fun `fetchLastEvaluation should return null when no previous evaluation exists`() = runTest {
        // Given
        val problemId = 1L
        coEvery { evaluationRepository.getNewEvaluation(problemId) } returns null

        // When
        viewModel.fetchLastEvaluation(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.lastEvaluation.value)
    }

    @Test
    fun `submitSolution should preserve solution text on failure`() = runTest {
        // Given
        val problemId = 1L
        val solutionText = "def solution():\n    return 'test'"

        viewModel.onSolutionTextChange(solutionText)
        coEvery { evaluationRepository.createEvaluation(any()) } throws IOException("Network error")

        // When
        viewModel.submitSolution(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Solution text should not be cleared
        assertEquals(solutionText, viewModel.solutionText.value)
        assertEquals("Cannot connect to server. Please check your network.", viewModel.error.value)
    }

    @Test
    fun `multiple loadProblemDetail calls should handle race conditions correctly`() = runTest {
        // Given
        val problem1 = sampleProblemDetail.copy(id = 1L, title = "Problem 1")
        val problem2 = sampleProblemDetail.copy(id = 2L, title = "Problem 2")

        coEvery { problemRepository.getProblemDetail(1L) } coAnswers {
            kotlinx.coroutines.delay(100)
            problem1
        }
        coEvery { problemRepository.getProblemDetail(2L) } returns problem2

        // When - Trigger two loads, second one should complete first
        viewModel.loadProblemDetail(1L)
        viewModel.loadProblemDetail(2L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should have the result from the last completed call
        val finalProblem = viewModel.problemDetail.value
        assertTrue(finalProblem?.id == 1L || finalProblem?.id == 2L)
    }

    @Test
    fun `submitSolution should handle successful submission with empty feedback`() = runTest {
        // Given
        val problemId = 1L
        val solutionText = "perfect solution"
        val evaluationWithEmptyFeedback = sampleEvaluation.copy(
            evaluation = sampleEvaluationDTO.copy(
                rating = 100,
                issue = emptyList(),
                feedback = emptyList()
            )
        )

        viewModel.onSolutionTextChange(solutionText)
        coEvery { evaluationRepository.createEvaluation(any()) } returns evaluationWithEmptyFeedback

        // When
        viewModel.navigateToEvaluation.test {
            viewModel.submitSolution(problemId)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            assertEquals(evaluationWithEmptyFeedback, viewModel.evaluationResult.value)
            assertEquals(evaluationWithEmptyFeedback, awaitItem())
            assertNull(viewModel.error.value)
        }
    }

    @Test
    fun `onSolutionTextChange with empty string should clear previous text`() = runTest {
        // Given - Set initial solution text
        viewModel.onSolutionTextChange("initial solution")
        assertEquals("initial solution", viewModel.solutionText.value)

        // When - Change to empty string
        viewModel.onSolutionTextChange("")

        // Then
        assertEquals("", viewModel.solutionText.value)
    }

    @Test
    fun `fetchLastSubmission with null response should not throw exception`() = runTest {
        // Given
        val problemId = 1L
        coEvery { evaluationRepository.getLastSubmission(problemId) } returns null

        // When
        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should complete without error
        assertNull(viewModel.lastSubmission.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loading state should be false after successful operations`() = runTest {
        // Given
        val problemId = 1L
        coEvery { problemRepository.getProblemDetail(problemId) } returns sampleProblemDetail
        coEvery { evaluationRepository.getLastSubmission(problemId) } returns sampleSubmission
        coEvery { evaluationRepository.getNewEvaluation(problemId) } returns sampleEvaluation

        // When - Execute multiple operations
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.fetchLastEvaluation(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Loading should be false after all operations complete
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loading state should be false after failed operations`() = runTest {
        // Given
        val problemId = 1L
        coEvery { problemRepository.getProblemDetail(problemId) } throws RuntimeException("Error")
        coEvery { evaluationRepository.getLastSubmission(problemId) } throws RuntimeException("Error")
        coEvery { evaluationRepository.getNewEvaluation(problemId) } throws RuntimeException("Error")

        // When - Execute multiple operations that fail
        viewModel.loadProblemDetail(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.fetchLastEvaluation(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Loading should be false after all operations complete
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `submitSolution with very long solution text should work`() = runTest {
        // Given
        val problemId = 1L
        val longSolution = "a".repeat(10000) // Very long solution
        viewModel.onSolutionTextChange(longSolution)
        coEvery { evaluationRepository.createEvaluation(any()) } returns sampleEvaluation

        // When
        viewModel.submitSolution(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(sampleEvaluation, viewModel.evaluationResult.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `submitSolution with special characters should work`() = runTest {
        // Given
        val problemId = 1L
        val specialCharSolution = "def solution():\n    return \"!@#$%^&*()_+-=[]{}|;':,.<>?/~`\""
        viewModel.onSolutionTextChange(specialCharSolution)
        coEvery { evaluationRepository.createEvaluation(any()) } returns sampleEvaluation

        // When
        viewModel.submitSolution(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(sampleEvaluation, viewModel.evaluationResult.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `clearError called multiple times should remain null`() = runTest {
        // When - Clear error multiple times
        viewModel.clearError()
        viewModel.clearError()
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
    }

    @Test
    fun `fetchLastSubmission should not override manually entered solution text`() = runTest {
        // Given
        val problemId = 1L
        val manualSolution = "manually typed solution"
        val fetchedSubmission = sampleSubmission.copy(solutionText = "fetched solution")

        // User types solution first
        viewModel.onSolutionTextChange(manualSolution)
        coEvery { evaluationRepository.getLastSubmission(problemId) } returns fetchedSubmission

        // When - Fetch last submission after user has typed
        viewModel.fetchLastSubmission(problemId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Manual solution should be preserved
        assertEquals(manualSolution, viewModel.solutionText.value)
        assertEquals(fetchedSubmission, viewModel.lastSubmission.value)
    }

    @Test
    fun `problem detail should be null before loadProblemDetail is called`() = runTest {
        // Then - Initial state
        assertNull(viewModel.problemDetail.value)
    }

    @Test
    fun `all state flows should be initialized correctly`() = runTest {
        // Then - Verify all initial states
        assertNull(viewModel.problemDetail.value)
        assertEquals("", viewModel.solutionText.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.lastSubmission.value)
        assertNull(viewModel.lastEvaluation.value)
        assertEquals(emptyList<EvaluationListItemDTO>(), viewModel.allEvaluations.value)
        assertNull(viewModel.evaluationResult.value)
        assertNull(viewModel.error.value)
    }
}
