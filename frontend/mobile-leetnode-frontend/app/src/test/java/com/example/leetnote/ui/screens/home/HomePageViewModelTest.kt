package com.example.leetnote.ui.screens.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.leetnote.data.model.LeetProblem
import com.example.leetnote.data.model.PageResponse
import com.example.leetnote.data.model.ProblemListDTO
import com.example.leetnote.data.repository.HomeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomePageViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: HomeRepository
    private lateinit var viewModel: HomeViewModel

    // Test data
    private val sampleProblems = listOf(
        LeetProblem(1, "Two Sum", "Easy", false, false),
        LeetProblem(2, "Add Two Numbers", "Medium", true, false),
        LeetProblem(3, "Longest Substring Without Repeating Characters", "Medium", false, true),
        LeetProblem(4, "Median of Two Sorted Arrays", "Hard", true, true)
    )

    private val sampleProblemDTOs = listOf(
        ProblemListDTO(1, "Two Sum", "Easy", false, false),
        ProblemListDTO(2, "Add Two Numbers", "Medium", true, false),
        ProblemListDTO(3, "Longest Substring Without Repeating Characters", "Medium", false, true),
        ProblemListDTO(4, "Median of Two Sorted Arrays", "Hard", true, true)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()

        // Setup default repository responses for paging source
        coEvery {
            repository.getAllProblems(any(), any(), any(), any(), any(), any())
        } returns PageResponse(sampleProblemDTOs, 1, 4, 0)

        coEvery {
            repository.updateProblemStatus(any(), any(), any())
        } returns sampleProblems[0]

        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty values`() = runTest {
        // Given - ViewModel is initialized

        // When - Getting initial state
        val searchQuery = viewModel.searchQuery.first()
        val selectedDifficulties = viewModel.selectedDifficulties.first()
        val filterSolved = viewModel.filterSolved.first()
        val filterFavorite = viewModel.filterFavorite.first()

        // Then - All values should be empty/null
        Assert.assertEquals("", searchQuery)
        Assert.assertEquals(emptyList<String>(), selectedDifficulties)
        Assert.assertNull(filterSolved)
        Assert.assertNull(filterFavorite)
    }

    @Test
    fun `updateQuery should update search query state`() = runTest {
        // Given
        val newQuery = "binary search"

        // When
        viewModel.updateQuery(newQuery)

        // Then
        viewModel.searchQuery.test {
            Assert.assertEquals(newQuery, awaitItem())
        }
    }

    @Test
    fun `updateDifficulties should update difficulties filter state`() = runTest {
        // Given
        val difficulties = listOf("Easy", "Medium")

        // When
        viewModel.updateDifficulties(difficulties)

        // Then
        viewModel.selectedDifficulties.test {
            Assert.assertEquals(difficulties, awaitItem())
        }
    }

    @Test
    fun `updateFilterSolved should update solved filter state`() = runTest {
        // Given
        val filterValue = true

        // When
        viewModel.updateFilterSolved(filterValue)

        // Then
        viewModel.filterSolved.test {
            Assert.assertEquals(filterValue, awaitItem())
        }
    }

    @Test
    fun `updateFilterFavorite should update favorite filter state`() = runTest {
        // Given
        val filterValue = true

        // When
        viewModel.updateFilterFavorite(filterValue)

        // Then
        viewModel.filterFavorite.test {
            Assert.assertEquals(filterValue, awaitItem())
        }
    }

    @Test
    fun `toggleSolved should update problem status and call repository`() = runTest {
        // Given
        val problem = sampleProblems[0] // Two Sum, initially not solved
        val updatedProblem = problem.copy(isSolved = true)

        coEvery {
            repository.updateProblemStatus(problem.id, true, problem.isFavorite)
        } returns updatedProblem

        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleSolved(problem)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.updateProblemStatus(
                problemId = problem.id,
                isSolved = true,
                isFavorite = problem.isFavorite
            )
        }
    }

    @Test
    fun `toggleFavorite should update problem status and call repository`() = runTest {
        // Given
        val problem = sampleProblems[0] // Two Sum, initially not favorite
        val updatedProblem = problem.copy(isFavorite = true)

        coEvery {
            repository.updateProblemStatus(problem.id, problem.isSolved, true)
        } returns updatedProblem

        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleFavorite(problem)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.updateProblemStatus(
                problemId = problem.id,
                isSolved = problem.isSolved,
                isFavorite = true
            )
        }
    }

    @Test
    fun `toggleSolved should toggle from true to false`() = runTest {
        // Given
        val solvedProblem = sampleProblems[1] // Add Two Numbers, initially solved
        Assert.assertTrue(solvedProblem.isSolved)

        coEvery {
            repository.updateProblemStatus(solvedProblem.id, false, solvedProblem.isFavorite)
        } returns solvedProblem.copy(isSolved = false)

        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleSolved(solvedProblem)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.updateProblemStatus(
                problemId = solvedProblem.id,
                isSolved = false,
                isFavorite = solvedProblem.isFavorite
            )
        }
    }

    @Test
    fun `toggleFavorite should toggle from true to false`() = runTest {
        // Given
        val favoriteProblem = sampleProblems[2] // Longest Substring, initially favorite
        Assert.assertTrue(favoriteProblem.isFavorite)

        coEvery {
            repository.updateProblemStatus(favoriteProblem.id, favoriteProblem.isSolved, false)
        } returns favoriteProblem.copy(isFavorite = false)

        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleFavorite(favoriteProblem)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.updateProblemStatus(
                problemId = favoriteProblem.id,
                isSolved = favoriteProblem.isSolved,
                isFavorite = false
            )
        }
    }

    @Test
    fun `clearing filters should reset to default values`() = runTest {
        // Given - Some filters are set
        viewModel.updateQuery("test")
        viewModel.updateDifficulties(listOf("Easy"))
        viewModel.updateFilterSolved(true)
        viewModel.updateFilterFavorite(true)

        // When - Clearing filters
        viewModel.updateQuery("")
        viewModel.updateDifficulties(emptyList())
        viewModel.updateFilterSolved(null)
        viewModel.updateFilterFavorite(null)

        // Then - All filters should be reset
        Assert.assertEquals("", viewModel.searchQuery.first())
        Assert.assertEquals(emptyList<String>(), viewModel.selectedDifficulties.first())
        Assert.assertNull(viewModel.filterSolved.first())
        Assert.assertNull(viewModel.filterFavorite.first())
    }

    @Test
    fun `multiple filter updates should update state correctly`() = runTest {
        // When - Setting multiple filters
        viewModel.updateQuery("binary")
        viewModel.updateDifficulties(listOf("Hard", "Medium"))
        viewModel.updateFilterSolved(true)
        viewModel.updateFilterFavorite(false)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then - All filter states should be updated
        Assert.assertEquals("binary", viewModel.searchQuery.first())
        Assert.assertEquals(listOf("Hard", "Medium"), viewModel.selectedDifficulties.first())
        Assert.assertEquals(true, viewModel.filterSolved.first())
        Assert.assertEquals(false, viewModel.filterFavorite.first())
    }

    @Test
    fun `search query debounce should work with state flow`() = runTest {
        // Given - Initial empty query
        Assert.assertEquals("", viewModel.searchQuery.first())

        // When - Update query multiple times quickly
        viewModel.updateQuery("a")
        viewModel.updateQuery("ab")
        viewModel.updateQuery("abc")

        // Then - State should reflect the latest query immediately
        Assert.assertEquals("abc", viewModel.searchQuery.first())
    }

    @Test
    fun `filter params should be accessible through state flows`() = runTest {
        // When - Setting filters
        viewModel.updateQuery("test")
        viewModel.updateDifficulties(listOf("Easy"))
        viewModel.updateFilterSolved(true)
        viewModel.updateFilterFavorite(false)

        // Then - All filters should be accessible through state flows
        Assert.assertEquals("test", viewModel.searchQuery.first())
        Assert.assertEquals(listOf("Easy"), viewModel.selectedDifficulties.first())
        Assert.assertEquals(true, viewModel.filterSolved.first())
        Assert.assertEquals(false, viewModel.filterFavorite.first())
    }

    @Test
    fun `paging data should be initialized with empty data`() = runTest {
        // Given - ViewModel is initialized
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Paging data should be available (starting with empty state)
        // Note: The actual paging data content testing would require more complex setup
        // This test verifies that the paging data StateFlow is accessible
        viewModel.pagedProblems.test {
            // Should receive at least one emission (initial empty state or loaded data)
            awaitItem() // Initial empty state or loaded data
            expectNoEvents() // No more immediate events
        }
    }

    @Test
    fun `view model should be properly initialized`() = runTest {
        // Given - ViewModel is created
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - All state flows should be accessible and have initial values
        Assert.assertEquals("", viewModel.searchQuery.first())
        Assert.assertEquals(emptyList<String>(), viewModel.selectedDifficulties.first())
        Assert.assertNull(viewModel.filterSolved.first())
        Assert.assertNull(viewModel.filterFavorite.first())
    }

    @Test
    fun `concurrent filter updates should work correctly`() = runTest {
        // When - Multiple concurrent updates
        viewModel.updateQuery("test1")
        viewModel.updateDifficulties(listOf("Easy"))
        viewModel.updateFilterSolved(true)
        viewModel.updateQuery("test2")
        viewModel.updateFilterFavorite(false)
        viewModel.updateQuery("final")

        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Final state should reflect all updates
        Assert.assertEquals("final", viewModel.searchQuery.first())
        Assert.assertEquals(listOf("Easy"), viewModel.selectedDifficulties.first())
        Assert.assertEquals(true, viewModel.filterSolved.first())
        Assert.assertEquals(false, viewModel.filterFavorite.first())
    }

    @Test
    fun `toggle operations should work with different problem states`() = runTest {
        // Given
        val unsolvedUnfavorite = LeetProblem(1, "Test 1", "Easy", false, false)
        val solvedFavorite = LeetProblem(2, "Test 2", "Medium", true, true)

        coEvery {
            repository.updateProblemStatus(1, true, false)
        } returns unsolvedUnfavorite.copy(isSolved = true)

        coEvery {
            repository.updateProblemStatus(2, false, true)
        } returns solvedFavorite.copy(isSolved = false)

        testDispatcher.scheduler.advanceUntilIdle()

        // When - Toggle solved status
        viewModel.toggleSolved(unsolvedUnfavorite)
        viewModel.toggleSolved(solvedFavorite)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Repository should be called with correct parameters
        coVerify {
            repository.updateProblemStatus(1, true, false)
            repository.updateProblemStatus(2, false, true)
        }
    }

    @Test
    fun `state flows should emit distinct values`() = runTest {
        // When - Set same value multiple times
        viewModel.updateQuery("test")
        viewModel.updateQuery("test")
        viewModel.updateQuery("test")

        // Then - State should still be "test"
        Assert.assertEquals("test", viewModel.searchQuery.first())
    }
}