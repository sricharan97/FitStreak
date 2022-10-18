package com.apptimistiq.android.fitstreak.main.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.RecipeRemoteDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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

    private val _menuItemSelection = MutableStateFlow(recipeTypeMap[RecipeDietType.Vegetarian]!!)


    val breakfastRecipes: StateFlow<RecipeTrackUiState> =
        _menuItemSelection.flatMapLatest { dietType ->
            recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.Breakfast]!!)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RecipeTrackUiState(
                    recipeType = mealTypeMap[MealType.Breakfast]!!,
                    isFetchingRecipes = true, recipes = emptyList()
                )
            )


    val mainCourseRecipes: StateFlow<RecipeTrackUiState> =
        _menuItemSelection.flatMapLatest { dietType ->
            recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.MainCourse]!!)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RecipeTrackUiState(
                    recipeType = mealTypeMap[MealType.MainCourse]!!,
                    isFetchingRecipes = true, recipes = emptyList()
                )
            )


    val snackRecipes: StateFlow<RecipeTrackUiState> = _menuItemSelection.flatMapLatest { dietType ->
        recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.Snack]!!)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecipeTrackUiState(
                recipeType = mealTypeMap[MealType.Snack]!!,
                isFetchingRecipes = true, recipes = emptyList()
            )
        )

    val saladRecipes: StateFlow<RecipeTrackUiState> = _menuItemSelection.flatMapLatest { dietType ->
        recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.Salad]!!)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecipeTrackUiState(
                recipeType = mealTypeMap[MealType.Salad]!!,
                isFetchingRecipes = true, recipes = emptyList()
            )
        )


    fun updateMenuDietType(diet: RecipeDietType) {

        _menuItemSelection.update {
            recipeTypeMap[diet]!!
        }

    }


}