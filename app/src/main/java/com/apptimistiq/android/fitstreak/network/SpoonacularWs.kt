package com.apptimistiq.android.fitstreak.network

import com.apptimistiq.android.fitstreak.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for the Spoonacular API.
 * 
 * This interface defines the API endpoints used to retrieve recipe information
 * from the Spoonacular web service. It leverages Retrofit annotations to specify
 * HTTP methods, paths, and query parameters.
 */
interface SpoonacularApiService {

    /**
     * Retrieves a list of recipes filtered by diet type and meal type.
     *
     * @param numberOfResults The maximum number of recipe results to return
     * @param dietTypeOfRecipe The diet type to filter recipes by (e.g., "vegetarian", "vegan")
     * @param mealType The type of meal to filter recipes by (e.g., "breakfast", "main course")
     * @return [JsonRecipe] containing the recipe search results
     */
    @GET("recipes/complexSearch?sort=calories&sortDirection=asc&${BuildConfig.CONSUMER_KEY}=${BuildConfig.SECRET}")
    suspend fun getRecipes(
        @Query("number") numberOfResults: Int,
        @Query("diet") dietTypeOfRecipe: String,
        @Query("type") mealType: String
    ): JsonRecipe

    /**
     * Retrieves detailed information for a specific recipe by its ID.
     *
     * @param recipeId The unique identifier of the recipe
     * @return URL of the recipe as a String
     */
    @GET("recipes/{id}/information?${BuildConfig.CONSUMER_KEY}=${BuildConfig.SECRET}")
    suspend fun getRecipeUrl(
        @Path("id") recipeId: Int
    ): String
}
