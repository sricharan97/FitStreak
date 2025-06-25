package com.apptimistiq.android.fitstreak.data

import com.apptimistiq.android.fitstreak.main.data.RecipeRemoteDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.Recipe
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * A fake implementation of [RecipeRemoteDataSource] for use in tests.
 * This version allows for configurable data and error conditions.
 */
class FakeRecipeRemoteDataSource : RecipeRemoteDataSource {

    private val dietTypeFlow = MutableStateFlow("Vegetarian")
    private var shouldReturnError = false
    private var errorDelay = false // Optional: to simulate network delay before error

    // Data is stored in a nested map: Map<DietType, Map<MealType, List<Recipe>>>
    private val recipeData = mutableMapOf<String, MutableMap<String, List<Recipe>>>()
    private val recipeUrls = mutableMapOf<Int, String?>()

    override fun getRecipes(dietTypeOfRecipe: String, mealType: String): Flow<RecipeTrackUiState> {
        return flow {
            if (shouldReturnError) {
                throw Exception("Test exception: Failed to fetch recipes")
            }

            val recipesForDiet = recipeData[dietTypeOfRecipe] ?: emptyMap()
            emit(
                RecipeTrackUiState(
                    recipeType = mealType,
                    recipes = recipesForDiet[mealType] ?: emptyList()
                )
            )
        }
    }

    override fun getRecipeUrl(recipeId: Int): Flow<String?> {
        return flow {
            if (shouldReturnError) {
                throw Exception("Test exception: Failed to fetch recipe URL")
            }
            emit(recipeUrls[recipeId])
        }
    }

    override fun getRecipeDietType(): Flow<String> {
        return dietTypeFlow.map {
            if (shouldReturnError) throw Exception("Test exception: Failed to get diet type")
            it
        }
    }

    override suspend fun updateRecipeDietType(dietType: String) {
        if (shouldReturnError) {
            throw Exception("Test exception: Failed to update diet type")
        }
        dietTypeFlow.value = dietType
    }

    /**
     * Helper function to add recipes for a specific diet and meal type.
     */
    fun addRecipes(dietType: String, mealType: String, recipes: List<Recipe>) {
        recipeData.getOrPut(dietType) { mutableMapOf() }[mealType] = recipes
    }

    /**
     * Helper function to add a URL for a specific recipe ID.
     */
    fun addRecipeUrl(recipeId: Int, url: String?) {
        recipeUrls[recipeId] = url
    }

    /**
     * Helper function to clear all mock data.
     */
    fun clearData() {
        recipeData.clear()
        recipeUrls.clear()
        dietTypeFlow.value = "Vegetarian" // Reset to default
    }

    /**
     * Configure whether this data source should throw exceptions.
     */
    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }
}