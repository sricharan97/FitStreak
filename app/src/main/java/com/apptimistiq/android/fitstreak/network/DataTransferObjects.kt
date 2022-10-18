package com.apptimistiq.android.fitstreak.network

import com.apptimistiq.android.fitstreak.main.data.domain.Recipe
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import com.squareup.moshi.Json

/**
 * JsonObject that contains the calorie nutrition information
 */
data class Calorie(
    @Json(name = "name", ignore = true) val nutritionType: String = "Calories",
    @Json(name = "amount") val calorieCount: Double,
    @Json(name = "unit", ignore = true) val calorieUnit: String = "kcal"
)

/**
 * JsonArray that contains the information for
 * all the nutrients that are included in the call
 */
data class Nutrient(val nutrients: List<Calorie>)


/**
 * JsonObject that contains all the information
 * related to the recipe
 */
data class RecipeItem(
    val id: Int,
    val title: String,
    @Json(name = "image") val imgUrl: String,
    @Json(ignore = true) val imageType: String = "jpg",
    val nutrition: Nutrient
)


/**
 * Root JsonObject of the the json response from the spoonacular api
 */
data class JsonRecipe(
    val results: List<RecipeItem>,
    val offset: Int,
    val number: Int,
    val totalResults: Int
)


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