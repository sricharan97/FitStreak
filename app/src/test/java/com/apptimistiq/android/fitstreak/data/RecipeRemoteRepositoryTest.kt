package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.test.FakeUserProfileDataSource
import com.apptimistiq.android.fitstreak.network.Calorie
import com.apptimistiq.android.fitstreak.network.JsonRecipe
import com.apptimistiq.android.fitstreak.network.Nutrient
import com.apptimistiq.android.fitstreak.network.RecipeItem
import com.apptimistiq.android.fitstreak.network.RecipeUrlResponse
import com.apptimistiq.android.fitstreak.network.SpoonacularApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class RecipeRemoteRepositoryTest {
    // Subject under test
    private lateinit var repository: RecipeRemoteRepository

    // Mock dependencies
    private lateinit var mockSpoonacularApiService: SpoonacularApiService
    private lateinit var fakeUserProfileDataSource: FakeUserProfileDataSource

    // Test dispatcher
    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val testDietType = "Vegetarian"
    private val testMealType = "breakfast"
    private val testRecipeId = 12345

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks and fakes
        mockSpoonacularApiService = mock(SpoonacularApiService::class.java)
        fakeUserProfileDataSource = FakeUserProfileDataSource()

        // Set up default data
        fakeUserProfileDataSource.setDietSelection(testDietType)

        // Create repository with dependencies
        repository = RecipeRemoteRepository(
            retrofitService = mockSpoonacularApiService,
            userProfileDataSource = fakeUserProfileDataSource,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getRecipes_returnsRecipesFromApi() = runTest {
        // Given: API returns recipe results
        val mockResults = listOf(
            RecipeItem(
                id = 1,
                title = "Test Recipe 1",
                imgUrl = "image1.jpg",
                nutrition = Nutrient(
                    nutrients = listOf(Calorie(calorieCount = 250.0))
                )
            ),
            RecipeItem(
                id = 2,
                title = "Test Recipe 2",
                imgUrl = "image2.jpg",
                nutrition = Nutrient(
                    nutrients = listOf(Calorie(calorieCount = 350.0))
                )
            )
        )

        val mockJsonRecipe = JsonRecipe(
            results = mockResults,
            offset = 0,
            number = 2,
            totalResults = 2
        )

        `when`(mockSpoonacularApiService.getRecipes(5, testDietType, testMealType))
            .thenReturn(mockJsonRecipe)

        // When: Fetching recipes
        val result = repository.getRecipes(testDietType, testMealType).first()

        // Then: Should return domain model with expected data
        assertEquals(2, result.recipes.size)
        assertEquals(1, result.recipes[0].id)
        assertEquals("Test Recipe 1", result.recipes[0].title)
        assertEquals("image1.jpg", result.recipes[0].imgUrl)
        assertEquals(250.0, result.recipes[0].calories, 0.01)
        assertEquals("Test Recipe 2", result.recipes[1].title)
    }

    @Test
    fun getRecipes_whenApiThrowsException_propagatesError() = runTest {
        // Given: API throws exception
        `when`(mockSpoonacularApiService.getRecipes(5, testDietType, testMealType))
            .thenThrow(RuntimeException("Network error"))

        // When/Then: Exception should be propagated
        try {
            repository.getRecipes(testDietType, testMealType).first()
            // If we get here, test should fail
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun getRecipeUrl_withValidId_returnsUrlFromApi() = runTest {
        // Given: API returns recipe URL
        val expectedUrl = "https://example.com/recipe/12345"
        val mockResponse = RecipeUrlResponse(recipeUrl = expectedUrl)
        `when`(mockSpoonacularApiService.getRecipeUrl(testRecipeId))
            .thenReturn(mockResponse)

        // When: Fetching recipe URL
        val url = repository.getRecipeUrl(testRecipeId).first()

        // Then: Should return the parsed URL
        assertEquals(expectedUrl, url)
    }


    @Test
    fun getRecipeUrl_withZeroId_returnsNull() = runTest {
        // When: Fetching recipe URL with ID 0
        val result = repository.getRecipeUrl(0).first()

        // Then: Should return null
        assertNull(result)
    }

    @Test
    fun getRecipeUrl_whenApiThrowsException_propagatesError() = runTest {
        // Given: API throws exception
        `when`(mockSpoonacularApiService.getRecipeUrl(testRecipeId))
            .thenThrow(RuntimeException("Network error"))

        // When/Then: Exception should be propagated
        try {
            repository.getRecipeUrl(testRecipeId).first()
            // If we get here, test should fail
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun getRecipeDietType_returnsDietTypeFromDataSource() = runTest {
        // Given: Data source has a specific diet type
        fakeUserProfileDataSource.setDietSelection("Vegan")

        // When: Getting diet type
        val result = repository.getRecipeDietType().first()

        // Then: Should return diet type from data source
        assertEquals("Vegan", result)
    }

    @Test
    fun updateRecipeDietType_updatesDietTypeInDataSource() = runTest {
        // Given: Initial diet type
        fakeUserProfileDataSource.setDietSelection("Vegetarian")

        // When: Updating diet type
        repository.updateRecipeDietType("Paleo")

        // Then: Data source should have updated diet type
        val updatedDietType = fakeUserProfileDataSource.dietSelection.first()
        assertEquals("Paleo", updatedDietType)
    }

    @Test
    fun updateRecipeDietType_whenDataSourceThrowsException_propagatesError() = runTest {
        // Given: Data source set to throw exception
        fakeUserProfileDataSource.setShouldThrowException(true)

        // When/Then: Exception should be propagated
        try {
            repository.updateRecipeDietType("Keto")
            // If we get here, test should fail
            throw AssertionError("Expected exception was not thrown")
        } catch (e: Exception) {
            // Test passes if exception is thrown
        }
    }
}