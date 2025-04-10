package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import com.apptimistiq.android.fitstreak.network.SpoonacularApiService
import com.apptimistiq.android.fitstreak.network.asDomainModel
import com.apptimistiq.android.fitstreak.utils.parseRecipeUrl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private const val LOG_TAG = "RecipeRemoteRepository"

/**
 * Repository for handling recipe data operations with external API.
 *
 * This class is responsible for fetching recipes from the Spoonacular API based on user preferences
 * and converting the responses to domain models. It also manages recipe-related user preferences
 * through the [UserProfileDataSource].
 *
 * @property retrofitService The API service for making requests to the Spoonacular API
 * @property userProfileDataSource Data source for user profile preferences
 * @property ioDispatcher Dispatcher used for IO operations, defaults to Dispatchers.IO
 */
@Singleton
class RecipeRemoteRepository @Inject constructor(
    private val retrofitService: SpoonacularApiService,
    private val userProfileDataSource: UserProfileDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RecipeRemoteDataSource {

    /**
     * Fetches recipes from the API based on diet and meal type.
     *
     * @param dietTypeOfRecipe The diet type filter (e.g., "vegan", "vegetarian")
     * @param mealType The meal type filter (e.g., "breakfast", "lunch", "dinner")
     * @return Flow emitting the recipe data transformed to UI state
     */
    override fun getRecipes(
        dietTypeOfRecipe: String,
        mealType: String
    ): Flow<RecipeTrackUiState> {
        return flow {
            emit(
                retrofitService.getRecipes(
                    5, dietTypeOfRecipe,
                    mealType
                ).results.asDomainModel(mealType)
            )
        }.flowOn(ioDispatcher).conflate()
    }

    /**
     * Retrieves the URL for a specific recipe by its ID.
     *
     * @param recipeId The unique identifier of the recipe
     * @return Flow emitting the recipe URL or null if recipe ID is 0
     */
    override fun getRecipeUrl(recipeId: Int): Flow<String?> {
        return flow {
            if (recipeId == 0) {
                emit(null)
            } else {
                emit(
                    parseRecipeUrl(JSONObject(retrofitService.getRecipeUrl(recipeId)))
                )
            }
        }.flowOn(ioDispatcher).conflate()
    }

    /**
     * Gets the user's preferred diet type.
     *
     * @return Flow emitting the user's selected diet type
     */
    override fun getRecipeDietType(): Flow<String> {
        return userProfileDataSource.dietSelection.flowOn(ioDispatcher)
    }

    /**
     * Updates the user's preferred diet type.
     *
     * @param dietType The new diet type selection to save
     */
    override suspend fun updateRecipeDietType(dietType: String) {
        withContext(ioDispatcher) {
            userProfileDataSource.updateDietSelection(dietType)
        }
    }
}
