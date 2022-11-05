package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import kotlinx.coroutines.flow.Flow

interface RecipeRemoteDataSource {

    fun getRecipes(dietTypeOfRecipe: String, mealType: String): Flow<RecipeTrackUiState>

    fun getRecipeUrl(recipeId: Int): Flow<String?>

}