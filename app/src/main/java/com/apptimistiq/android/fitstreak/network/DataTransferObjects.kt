package com.apptimistiq.android.fitstreak.network

import com.apptimistiq.android.fitstreak.main.data.domain.Recipe
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import com.squareup.moshi.Json

/**
 * Data Transfer Objects for the recipe API.
 *
 * This file contains all the DTO classes used to parse JSON responses from the Spoonacular API.
 * These DTOs are later converted to domain models for use within the app.
 */

/**
 * Root response object from the Spoonacular API recipe search endpoint.
 * 
 * @property results List of recipe items returned
 * @property offset Pagination offset
 * @property number Number of results returned
 * @property totalResults Total number of results available
 */
data class JsonRecipe(
    val results: List<RecipeItem>,
    val offset: Int,
    val number: Int,
    val totalResults: Int
)

/**
 * Represents a single recipe item from the API response.
 * 
 * @property id Unique identifier for the recipe
 * @property title The name/title of the recipe
 * @property imgUrl URL to the recipe image
 * @property imageType The image format type (default: "jpg")
 * @property nutrition Nutritional information for the recipe
 */
data class RecipeItem(
    val id: Int,
    val title: String,
    @Json(name = "image") val imgUrl: String,
    @Json(ignore = true) val imageType: String = "jpg",
    val nutrition: Nutrient
)

/**
 * Container for nutritional information.
 * 
 * @property nutrients List of nutrients, including calories
 */
data class Nutrient(
    val nutrients: List<Calorie>
)

/**
 * Represents calorie information from the nutritional data.
 * 
 * @property nutritionType The type of nutrition (always "Calories")
 * @property calorieCount The amount of calories
 * @property calorieUnit The unit of measurement for calories (always "kcal")
 */
data class Calorie(
    @Json(name = "name", ignore = true) val nutritionType: String = "Calories",
    @Json(name = "amount") val calorieCount: Double,
    @Json(name = "unit", ignore = true) val calorieUnit: String = "kcal"
)

/**
 * Response object for the recipe card endpoint.
 * 
 * @property recipeCardImgUrl URL to the recipe card image
 */
data class RecipeCard(
    @Json(name = "url") val recipeCardImgUrl: String
)

/**
 * Extension function to convert network DTO objects to domain models.
 *
 * @param mealType The type of meal (breakfast, lunch, dinner, etc.)
 * @return RecipeTrackUiState containing the converted domain models
 */
fun List<RecipeItem>.asDomainModel(mealType: String): RecipeTrackUiState {
    val recipeList = map {
        Recipe(
            id = it.id,
            title = it.title,
            imgUrl = it.imgUrl,
            calories = it.nutrition.nutrients[0].calorieCount
        )
    }

    return RecipeTrackUiState(
        recipeType = mealType,
        isFetchingRecipes = false,
        recipes = recipeList
    )
}
