package com.apptimistiq.android.fitstreak.network

import com.apptimistiq.android.fitstreak.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query


interface SpoonacularApiService {

    @GET("recipes/complexSearch?sort=calories&sortDirection=asc&${BuildConfig.CONSUMER_KEY}=${BuildConfig.SECRET}")
    suspend fun getRecipes(
        @Query("number") numberOfResults: Int,
        @Query("diet") dietTypeOfRecipe: String,
        @Query("type") mealType: String
    ): JsonRecipe

}

