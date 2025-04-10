package com.apptimistiq.android.fitstreak.main.data.domain

/**
 * Represents the UI state for the recipe tracking screen.
 *
 * This data class encapsulates all relevant information needed to render the recipe tracking UI,
 * including loading state, available recipes and user feedback messages.
 *
 * @property recipeType The category or type of recipes being displayed
 * @property isFetchingRecipes Boolean flag indicating whether recipes are currently being loaded
 * @property userMessage Optional message to display to the user (errors, confirmations, etc.)
 * @property recipes List of recipe items to display in the UI
 */
data class RecipeTrackUiState(
    val recipeType: String,
    val recipes: List<Recipe>,
    val isFetchingRecipes: Boolean = false,
    val userMessage: String? = null
)
