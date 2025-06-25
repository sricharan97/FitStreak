package com.apptimistiq.android.fitstreak.main.recipe

import com.apptimistiq.android.fitstreak.data.FakeRecipeRemoteDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.Recipe
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

// These enums are assumed to exist based on the RecipeViewModel code.


@ExperimentalCoroutinesApi
class RecipeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Subject under test
    private lateinit var viewModel: RecipeViewModel

    // Fake dependency
    private lateinit var fakeDataSource: FakeRecipeRemoteDataSource

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDataSource = FakeRecipeRemoteDataSource()
        viewModel = RecipeViewModel(fakeDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init - loads recipes for default diet and updates state`() = runTest {
        // Setup
        val defaultDietType = "Vegetarian"
        val breakfastRecipes = listOf(Recipe(1, "Breakfast Recipe", "url", 100.0))
        val mainCourseRecipes = listOf(Recipe(2, "Main Course Recipe", "url", 200.0))
        val snackRecipes = listOf(Recipe(3, "Snack Recipe", "url", 150.0))
        val saladRecipes = listOf(Recipe(4, "Salad Recipe", "url", 120.0))

        // Add recipes for all required meal types
        fakeDataSource.addRecipes(defaultDietType, "breakfast", breakfastRecipes)
        fakeDataSource.addRecipes(defaultDietType, "main course", mainCourseRecipes)
        fakeDataSource.addRecipes(defaultDietType, "snack", snackRecipes)
        fakeDataSource.addRecipes(defaultDietType, "salad", saladRecipes)

        // Create collectors to actively observe the flows
        val recipeListResults = mutableListOf<List<RecipeTrackUiState>>()
        val loadingStateResults = mutableListOf<Boolean>()

        // Launch collectors
        val recipeListJob = launch(testDispatcher) {
            viewModel.recipeTrackList.collect { recipeListResults.add(it) }
        }
        val loadingJob = launch(testDispatcher) {
            viewModel.isLoading.collect { loadingStateResults.add(it) }
        }

        // Allow time for all flows to emit and combine
        advanceUntilIdle()

        // Verify recipe data was loaded (should see initial empty list and then loaded data)
        assertThat(recipeListResults.last().size, `is`(4))

        // Verify loading state changed from true to false
        assertThat(loadingStateResults.first(), `is`(true))
        assertThat(loadingStateResults.last(), `is`(false))

        // Cancel the collection jobs
        recipeListJob.cancel()
        loadingJob.cancel()
    }

    @Test
    fun `updateMenuDietType - updates diet type and reloads recipes`() = runTest {
        // Setup - create data for both diet types
        val vegetarianBreakfast = listOf(Recipe(1, "Vegetarian Breakfast", "url", 100.0))
        val vegetarianMain = listOf(Recipe(2, "Vegetarian Main", "url", 200.0))
        val vegetarianSnack = listOf(Recipe(3, "Vegetarian Snack", "url", 150.0))
        val vegetarianSalad = listOf(Recipe(4, "Vegetarian Salad", "url", 120.0))

        val veganBreakfast = listOf(Recipe(5, "Vegan Breakfast", "url", 90.0))
        val veganMain = listOf(Recipe(6, "Vegan Main", "url", 180.0))
        val veganSnack = listOf(Recipe(7, "Vegan Snack", "url", 130.0))
        val veganSalad = listOf(Recipe(8, "Vegan Salad", "url", 110.0))

        // Add recipes for both diet types
        fakeDataSource.addRecipes("Vegetarian", "breakfast", vegetarianBreakfast)
        fakeDataSource.addRecipes("Vegetarian", "main course", vegetarianMain)
        fakeDataSource.addRecipes("Vegetarian", "snack", vegetarianSnack)
        fakeDataSource.addRecipes("Vegetarian", "salad", vegetarianSalad)

        fakeDataSource.addRecipes("Vegan", "breakfast", veganBreakfast)
        fakeDataSource.addRecipes("Vegan", "main course", veganMain)
        fakeDataSource.addRecipes("Vegan", "snack", veganSnack)
        fakeDataSource.addRecipes("Vegan", "salad", veganSalad)


        // Create collectors
        val dietTypeResults = mutableListOf<String>()
        val recipeListResults = mutableListOf<List<RecipeTrackUiState>>()
        val loadingStateResults = mutableListOf<Boolean>()

        // Launch collectors
        val dietTypeJob = launch(testDispatcher) {
            viewModel.currentDietType.collect { dietTypeResults.add(it) }
        }
        val recipeListJob = launch(testDispatcher) {
            viewModel.recipeTrackList.collect { recipeListResults.add(it) }
        }
        val loadingJob = launch(testDispatcher) {
            viewModel.isLoading.collect { loadingStateResults.add(it) }
        }

        // Wait for initial emissions
        advanceUntilIdle()

        // Verify initial state
        assertThat(dietTypeResults.last(), `is`("Vegetarian"))
        assertThat(recipeListResults.last().size, `is`(4))

        // Update diet type
        viewModel.updateMenuDietType(RecipeDietType.Vegan)
        advanceUntilIdle()

        // Verify diet type changed
        assertThat(dietTypeResults.last(), `is`("Vegan"))

        // Verify recipe list was updated
        val lastRecipes = recipeListResults.last()
        assertThat(lastRecipes.size, `is`(4))
        assertThat(lastRecipes.find { it.recipeType == "breakfast" }?.recipes?.first()?.title,
            `is`("Vegan Breakfast"))

        // Verify loading state changes: true -> false -> true -> false
        assertThat(loadingStateResults[0], `is`(true))
        assertThat(loadingStateResults[1], `is`(false))
        assertThat(loadingStateResults[2], `is`(true))
        assertThat(loadingStateResults.last(), `is`(false))

        // Cancel collection jobs
        dietTypeJob.cancel()
        recipeListJob.cancel()
        loadingJob.cancel()
    }

    @Test
    fun `updateCurrentRecipeId - updates recipeUrl flow`() = runTest {
        // Setup
        fakeDataSource.addRecipeUrl(123, "https://recipe-url.com/123")
        viewModel = RecipeViewModel(fakeDataSource)

        // Create collector
        val recipeUrlResults = mutableListOf<String?>()

        // Launch collector
        val recipeUrlJob = launch(testDispatcher) {
            viewModel.recipeUrl.collect { recipeUrlResults.add(it) }
        }

        // Wait for initial emission
        advanceUntilIdle()

        // Verify initial state is null
        assertThat(recipeUrlResults.last(), `is`(nullValue()))

        // Update recipe ID
        viewModel.updateCurrentRecipeId(123)
        advanceUntilIdle()

        // Verify URL was updated
        assertThat(recipeUrlResults.last(), `is`("https://recipe-url.com/123"))

        // Cancel collection job
        recipeUrlJob.cancel()
    }

    @Test
    fun `navigateToRecipeUrlCompleted - resets current recipe ID`() = runTest {
        // Setup
        fakeDataSource.addRecipeUrl(123, "https://recipe-url.com/123")
        viewModel = RecipeViewModel(fakeDataSource)

        // Create collector
        val recipeUrlResults = mutableListOf<String?>()

        // Launch collector
        val recipeUrlJob = launch(testDispatcher) {
            viewModel.recipeUrl.collect { recipeUrlResults.add(it) }
        }

        // Set recipe ID and wait for flow emission
        viewModel.updateCurrentRecipeId(123)
        advanceUntilIdle()

        // Verify URL was set
        assertThat(recipeUrlResults.last(), `is`("https://recipe-url.com/123"))

        // Call navigate completed
        viewModel.navigateToRecipeUrlCompleted()
        advanceUntilIdle()

        // Verify URL was reset to null
        assertThat(recipeUrlResults.last(), `is`(nullValue()))

        // Cancel collection job
        recipeUrlJob.cancel()
    }




    @Test
    fun `recipeUrl - updates on recipe ID change and resets`() = runTest {
        // Given a collector for the recipe URL
        val results = mutableListOf<String?>()
        val job = launch(testDispatcher) {
            viewModel.recipeUrl.collect { results.add(it) }
        }
        advanceUntilIdle() // Collect initial null

        // And a recipe URL is configured in the data source
        val testUrl = "http://example.com/recipe/123"
        fakeDataSource.addRecipeUrl(123, testUrl)

        // When current recipe ID is updated
        viewModel.updateCurrentRecipeId(123)
        advanceUntilIdle()

        // And then navigation is completed
        viewModel.navigateToRecipeUrlCompleted()
        advanceUntilIdle()

        // Then the URL flow should emit the URL and then null
        assertThat(results.size, `is`(3))
        assertThat(results[0], `is`(nullValue())) // initial value
        assertThat(results[1], `is`(testUrl))
        assertThat(results[2], `is`(nullValue())) // after reset

        job.cancel()
    }

    // New test for error handling
    @Test
    fun `init - when recipe fetching fails - still creates empty recipeList`() = runTest {
        // Setup - configure data source to return errors
        fakeDataSource.setShouldReturnError(true)

        // Create ViewModel
        viewModel = RecipeViewModel(fakeDataSource)

        // Create collectors
        val recipeListResults = mutableListOf<List<RecipeTrackUiState>>()
        val loadingStateResults = mutableListOf<Boolean>()

        // Launch collectors
        val recipeListJob = launch(testDispatcher) {
            viewModel.recipeTrackList.collect { recipeListResults.add(it) }
        }
        val loadingJob = launch(testDispatcher) {
            viewModel.isLoading.collect { loadingStateResults.add(it) }
        }

        // Allow time for emissions
        advanceUntilIdle()

        // The list itself is not empty, but each RecipeTrackUiState in the list should have empty recipes
        assertThat(recipeListResults.last().all { it.recipes.isEmpty() }, `is`(true))

        // Loading should be false after data is processed
        assertThat(loadingStateResults.last(), `is`(false))

        // Clean up
        recipeListJob.cancel()
        loadingJob.cancel()
    }

    // Test for empty recipe lists
    @Test
    fun `init - with empty recipe lists - creates valid but empty UI state`() = runTest {
        // Setup - add empty recipe lists for all meal types
        val defaultDietType = "Vegetarian"

        fakeDataSource.addRecipes(defaultDietType, "breakfast", emptyList())
        fakeDataSource.addRecipes(defaultDietType, "main course", emptyList())
        fakeDataSource.addRecipes(defaultDietType, "snack", emptyList())
        fakeDataSource.addRecipes(defaultDietType, "salad", emptyList())

        // Create ViewModel
        viewModel = RecipeViewModel(fakeDataSource)

        // Create collectors
        val recipeListResults = mutableListOf<List<RecipeTrackUiState>>()
        val loadingStateResults = mutableListOf<Boolean>()

        // Launch collectors
        val recipeListJob = launch(testDispatcher) {
            viewModel.recipeTrackList.collect { recipeListResults.add(it) }
        }
        val loadingJob = launch(testDispatcher) {
            viewModel.isLoading.collect { loadingStateResults.add(it) }
        }

        // Allow time for emissions
        advanceUntilIdle()

        // Verify all meal types exist but with empty recipe lists
        val lastState = recipeListResults.last()
        assertThat(lastState.size, `is`(4))
        lastState.forEach { recipeTrack ->
            assertThat(recipeTrack.recipes.isEmpty(), `is`(true))
        }

        // Verify loading state changed from true to false
        assertThat(loadingStateResults.first(), `is`(true))
        assertThat(loadingStateResults.last(), `is`(false))

        // Cancel collection jobs
        recipeListJob.cancel()
        loadingJob.cancel()
    }

    // Test for missing meal types
    @Test
    fun `init - with missing meal types - handles partial data correctly`() = runTest {
        // Setup - only add some meal types
        val defaultDietType = "Vegetarian"
        val breakfastRecipes = listOf(Recipe(1, "Breakfast Recipe", "url", 100.0))
        val snackRecipes = listOf(Recipe(3, "Snack Recipe", "url", 150.0))

        // Only add 2 out of 4 required meal types
        fakeDataSource.addRecipes(defaultDietType, "breakfast", breakfastRecipes)
        fakeDataSource.addRecipes(defaultDietType, "snack", snackRecipes)
        // Deliberately missing "main course" and "salad"

        // Create ViewModel
        viewModel = RecipeViewModel(fakeDataSource)

        // Create collectors
        val recipeListResults = mutableListOf<List<RecipeTrackUiState>>()

        // Launch collector
        val recipeListJob = launch(testDispatcher) {
            viewModel.recipeTrackList.collect { recipeListResults.add(it) }
        }

        // Allow time for emissions
        advanceUntilIdle()

        // Verify all 4 meal types are in the result (the missing ones should have empty lists)
        val lastState = recipeListResults.last()
        assertThat(lastState.size, `is`(4))

        // Check the meal types that were provided have data
        val breakfastState = lastState.find { it.recipeType == "breakfast" }
        assertThat(breakfastState?.recipes?.isEmpty(), `is`(false))

        val snackState = lastState.find { it.recipeType == "snack" }
        assertThat(snackState?.recipes?.isEmpty(), `is`(false))

        // Check the missing meal types have empty lists
        val mainCourseState = lastState.find { it.recipeType == "main course" }
        assertThat(mainCourseState?.recipes?.isEmpty(), `is`(true))

        val saladState = lastState.find { it.recipeType == "salad" }
        assertThat(saladState?.recipes?.isEmpty(), `is`(true))

        // Cancel collection job
        recipeListJob.cancel()
    }


}