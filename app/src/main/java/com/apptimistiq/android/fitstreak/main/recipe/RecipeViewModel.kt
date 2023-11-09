package com.apptimistiq.android.fitstreak.main.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.RecipeRemoteDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class RecipeDietType {
    Vegetarian,
    Vegan,
    Keto,
    Paleo
}

enum class MealType {
    Breakfast,
    Dessert,
    Snack,
    Salad,
    MainCourse

}

@ExperimentalCoroutinesApi
// @Inject tells Dagger how to provide instances of this type
class RecipeViewModel @Inject constructor(
    private val recipeRemoteDataSource: RecipeRemoteDataSource
) : ViewModel() {


    private val recipeTypeMap = mapOf(
        RecipeDietType.Vegetarian to "Vegetarian",
        RecipeDietType.Keto to "Ketogenic",
        RecipeDietType.Paleo to "Paleo",
        RecipeDietType.Vegan to "Vegan"
    )

    private val mealTypeMap = mapOf(
        MealType.Breakfast to "breakfast",
        MealType.Dessert to "dessert",
        MealType.Salad to "salad",
        MealType.Snack to "snack",
        MealType.MainCourse to "main course"
    )


    //Use this stateflow to keep track of navigation to the recipeInstruction screen

    private val _menuItemSelection: StateFlow<String> =
        recipeRemoteDataSource.getRecipeDietType().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = recipeTypeMap[RecipeDietType.Vegetarian]!!
        )

    private val _currentRecipeId = MutableStateFlow(0)


    //for extracting the recipe Image
    val recipeUrl: StateFlow<String?> = _currentRecipeId.flatMapLatest { currentRecipeID ->
        recipeRemoteDataSource.getRecipeUrl(currentRecipeID)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )


    private val breakfastRecipes: Flow<RecipeTrackUiState> =
        _menuItemSelection.flatMapLatest { dietType ->
            recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.Breakfast]!!)
        }

    private val mainCourseRecipes: Flow<RecipeTrackUiState> =
        _menuItemSelection.flatMapLatest { dietType ->
            recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.MainCourse]!!)
        }

    private val snackRecipes: Flow<RecipeTrackUiState> =
        _menuItemSelection.flatMapLatest { dietType ->
            recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.Snack]!!)
        }

    private val saladRecipes: Flow<RecipeTrackUiState> =
        _menuItemSelection.flatMapLatest { dietType ->
            recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.Salad]!!)
        }


    //for observing the changes to recipes list
    val recipeTrackList: StateFlow<List<RecipeTrackUiState>> = combine(
        breakfastRecipes,
        mainCourseRecipes,
        snackRecipes,
        saladRecipes
    ) { bR: RecipeTrackUiState, mR: RecipeTrackUiState, sR: RecipeTrackUiState, nR: RecipeTrackUiState ->
        listOf(bR, mR, sR, nR)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    fun updateMenuDietType(diet: RecipeDietType) {

        viewModelScope.launch {
            recipeRemoteDataSource.updateRecipeDietType(recipeTypeMap[diet]!!)
        }

    }

    fun updateCurrentRecipeId(recipeId: Int) {
        _currentRecipeId.update { recipeId }
    }

    fun navigateToRecipeUrlCompleted() {
        _currentRecipeId.update { 0 }
    }


}