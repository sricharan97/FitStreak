package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import com.apptimistiq.android.fitstreak.network.SpoonacularApiService
import com.apptimistiq.android.fitstreak.network.asDomainModel
import com.apptimistiq.android.fitstreak.utils.parseRecipeUrl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private const val LOG_TAG = "RecipeRemoteRepository"
// @Inject tells Dagger how to provide instances of this type
@Singleton
class RecipeRemoteRepository @Inject constructor(
    private val retrofitService: SpoonacularApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RecipeRemoteDataSource {

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
        }.flowOn(ioDispatcher)


    }


    override fun getRecipeUrl(recipeId: Int): Flow<String?> {

        return flow {
            if (recipeId == 0) {
                emit(null)
            } else {
                emit(
                    parseRecipeUrl(JSONObject(retrofitService.getRecipeUrl(recipeId)))
                )
            }
        }.flowOn(ioDispatcher)

    }


}