package com.apptimistiq.android.fitstreak.main.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.RecipeRemoteDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.get

/**
 * Enum class representing different diet types for recipes.
 */
enum class RecipeDietType {
    Vegetarian,
    Vegan,
    Keto,
    Paleo
}

/**
 * Enum class representing different meal types.
 */
enum class MealType {
    Breakfast,
    Dessert,
    Snack,
    Salad,
    MainCourse
}
/**
 * Maps the RecipeDietType enum to API-compatible string values.
 */
val recipeTypeMap = mapOf(
    RecipeDietType.Vegetarian to "Vegetarian",
    RecipeDietType.Keto to "Ketogenic",
    RecipeDietType.Paleo to "Paleo",
    RecipeDietType.Vegan to "Vegan"
)


/**
 * Maps the MealType enum to API-compatible string values.
 */
val mealTypeMap = mapOf(
    MealType.Breakfast to "breakfast",
    MealType.Dessert to "dessert",
    MealType.Salad to "salad",
    MealType.Snack to "snack",
    MealType.MainCourse to "main course"
)

/**
 * ViewModel responsible for managing recipe data and user interactions.
 * 
 * This class communicates with [RecipeRemoteDataSource] to fetch recipe information
 * based on selected diet type and meal type. It exposes recipe data as [StateFlow]s
 * for the UI layer to observe.
 *
 * @property recipeRemoteDataSource Data source for fetching recipe information
 */
@ExperimentalCoroutinesApi
class RecipeViewModel @Inject constructor(
    private val recipeRemoteDataSource: RecipeRemoteDataSource
) : ViewModel() {



    /**
     * StateFlow that tracks the currently selected recipe diet type.
     */
    private val _menuItemSelection: StateFlow<String> =
        recipeRemoteDataSource.getRecipeDietType()
            .catch { emit(recipeTypeMap[RecipeDietType.Vegetarian]!!) }
            .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = recipeTypeMap[RecipeDietType.Vegetarian]!!
        )

    // Add loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Add error state for quota exceeded
    private val _isQuotaExceeded = MutableStateFlow(false)
    val isQuotaExceeded: StateFlow<Boolean> = _isQuotaExceeded

    /**
     * Publicly exposed StateFlow for current diet type
     */
    val currentDietType: StateFlow<String> = _menuItemSelection


    /**
     * Keeps track of the currently selected recipe ID for detailed view.
     */
    private val _currentRecipeId = MutableStateFlow(0)

    /**
     * StateFlow exposing the URL for the currently selected recipe.
     * Updates whenever the selected recipe ID changes.
     */
    val recipeUrl: StateFlow<String?> = _currentRecipeId.flatMapLatest { currentRecipeID ->
        recipeRemoteDataSource.getRecipeUrl(currentRecipeID)
            .catch { emit(null) } // Add error handling
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Flow of breakfast recipes based on the selected diet type.
     */
    private val breakfastRecipes: Flow<RecipeTrackUiState> =
        _menuItemSelection.flatMapLatest { dietType ->
            recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.Breakfast]!!)
                .catch { exception ->
                    handleRecipeError(exception)
                    emit(RecipeTrackUiState(
                        recipeType = mealTypeMap[MealType.Breakfast]!!,
                        recipes = emptyList()))
                }
        }

    /**
     * Flow of main course recipes based on the selected diet type.
     */
    private val mainCourseRecipes: Flow<RecipeTrackUiState> =
        _menuItemSelection.flatMapLatest { dietType ->
            recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.MainCourse]!!)
                .catch { exception ->
                    handleRecipeError(exception)
                    emit(RecipeTrackUiState(
                        recipeType = mealTypeMap[MealType.MainCourse]!!,
                        recipes = emptyList()))
                }
        }

    /**
     * Flow of snack recipes based on the selected diet type.
     */
    private val snackRecipes: Flow<RecipeTrackUiState> =
        _menuItemSelection.flatMapLatest { dietType ->
            recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.Snack]!!)
                .catch { exception ->
                    handleRecipeError(exception)
                    emit(RecipeTrackUiState(
                        recipeType = mealTypeMap[MealType.Snack]!!,
                        recipes = emptyList()))
                }
        }

    /**
     * Flow of salad recipes based on the selected diet type.
     */
    private val saladRecipes: Flow<RecipeTrackUiState> =
        _menuItemSelection.flatMapLatest { dietType ->
            recipeRemoteDataSource.getRecipes(dietType, mealTypeMap[MealType.Salad]!!)
                .catch { exception ->
                    handleRecipeError(exception)
                    emit(RecipeTrackUiState(
                        recipeType = mealTypeMap[MealType.Salad]!!,
                        recipes = emptyList()))
                }
        }

    /**
     * Combined StateFlow of all recipe types organized by meal category.
     * UI layer observes this to display different recipe sections.
     */
    val recipeTrackList: StateFlow<List<RecipeTrackUiState>> = combine(
        breakfastRecipes,
        mainCourseRecipes,
        snackRecipes,
        saladRecipes
    ) { bR: RecipeTrackUiState, mR: RecipeTrackUiState, sR: RecipeTrackUiState, nR: RecipeTrackUiState ->
        // Set loading to false when data is available
        _isLoading.value = false
        listOf(bR, mR, sR, nR)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Updates the selected diet type to filter recipes.
     *
     * @param diet The new diet type to filter by
     */
    fun updateMenuDietType(diet: RecipeDietType) {
        viewModelScope.launch {
            _isLoading.value = true
            recipeRemoteDataSource.updateRecipeDietType(recipeTypeMap[diet]!!)
        }
    }

    /**
     * Updates the currently selected recipe ID, typically called when
     * a user selects a specific recipe to view details.
     *
     * @param recipeId The ID of the selected recipe
     */
    fun updateCurrentRecipeId(recipeId: Int) {
        _currentRecipeId.update { recipeId }
    }

    /**
     * Resets the current recipe ID after navigation to recipe details is complete.
     * This prevents renavigation when returning to the recipes list.
     */
    fun navigateToRecipeUrlCompleted() {
        _currentRecipeId.update { 0 }
    }

    /**
     * Handles recipe API errors by setting quota exceeded state.
     */
    private fun handleRecipeError(exception: Throwable) {
        _isQuotaExceeded.value = true
    }
}