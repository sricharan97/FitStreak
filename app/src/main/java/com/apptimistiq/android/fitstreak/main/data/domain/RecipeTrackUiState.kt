package com.apptimistiq.android.fitstreak.main.data.domain

data class RecipeTrackUiState(
    val recipeType: String,
    val isFetchingRecipes: Boolean = false,
    val userMessage: String? = null,
    val recipes: List<Recipe>,
)