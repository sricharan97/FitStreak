package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the operations for accessing recipe-related data from remote sources.
 *
 * This interface abstracts the underlying implementation details of fetching recipe data,
 * allowing the application to retrieve nutritional recipes based on dietary preferences
 * and meal types from external services or APIs.
 */
interface RecipeRemoteDataSource {


    /**
     * Retrieves recipes matching the specified dietary and meal preferences.
     *
     * @param dietTypeOfRecipe The dietary type filter (e.g., "vegan", "vegetarian", "paleo")
     * @param mealType The meal type filter (e.g., "breakfast", "lunch", "dinner")
     * @return [Flow] emitting [RecipeTrackUiState] representing the loading state and recipe results
     */
    fun getRecipes(dietTypeOfRecipe: String, mealType: String): Flow<RecipeTrackUiState>

    /**
     * Retrieves the detailed URL for a specific recipe.
     *
     * @param recipeId The unique identifier of the recipe
     * @return [Flow] emitting the URL string for the recipe details, or null if not found
     */
    fun getRecipeUrl(recipeId: Int): Flow<String?>

    /**
     * Retrieves the user's currently selected diet type preference.
     *
     * @return [Flow] emitting the current diet type as a string
     */
    fun getRecipeDietType(): Flow<String>

    //endregion

    //region Write Operations

    /**
     * Updates the user's diet type preference for recipes.
     *
     * @param dietType The new diet type preference to save
     */
    suspend fun updateRecipeDietType(dietType: String)

    //endregion
}
