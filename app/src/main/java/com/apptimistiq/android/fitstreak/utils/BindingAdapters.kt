package com.apptimistiq.android.fitstreak.utils

import android.widget.ImageView
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
import com.apptimistiq.android.fitstreak.main.data.domain.Recipe
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import com.apptimistiq.android.fitstreak.main.progressTrack.ActivityListAdapter
import com.apptimistiq.android.fitstreak.main.recipe.RecipeListAdapter
import com.apptimistiq.android.fitstreak.main.recipe.RecipeTypeListAdapter
import com.bumptech.glide.Glide

/**
 * Collection of binding adapters used throughout the application.
 * These binding adapters connect XML layout attributes to code functionality,
 * primarily handling data binding for RecyclerViews, progress indicators, and image loading.
 */

/**
 * RecyclerView Binding Adapters
 */

/**
 * Binds activity data to a RecyclerView displaying activity items.
 *
 * @param recyclerView The RecyclerView to bind data to
 * @param data List of activity items to display
 */
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<ActivityItemUiState>?) {
    val adapter = recyclerView.adapter as ActivityListAdapter
    adapter.submitList(data)
}

/**
 * Binds recipe category data to a parent RecyclerView displaying recipe types.
 *
 * @param recyclerView The RecyclerView to bind data to
 * @param data List of recipe categories to display
 */
@BindingAdapter("listData")
fun bindRecipeParentRecyclerView(recyclerView: RecyclerView, data: List<RecipeTrackUiState>?) {
    val adapter = recyclerView.adapter as RecipeTypeListAdapter
    adapter.submitList(data)
}

/**
 * Binds recipe data to a child RecyclerView displaying individual recipes.
 *
 * @param recyclerView The RecyclerView to bind data to
 * @param data List of recipes to display
 */
@BindingAdapter("listData")
fun bindRecipeChildRecyclerView(recyclerView: RecyclerView, data: List<Recipe>?) {
    val adapter = recyclerView.adapter as RecipeListAdapter
    adapter.submitList(data)
}

/**
 * Image Binding Adapters
 */

/**
 * Binds the appropriate icon resource to an ImageView based on activity type.
 *
 * @param imageView The ImageView to set the icon on
 * @param activityType The type of activity determining which icon to display
 */
@BindingAdapter("activityIcon")
fun bindActivityIcon(imageView: ImageView, activityType: ActivityType) {
    val imageResource = when (activityType) {
        ActivityType.STEP -> R.drawable.ic_step_goal
        ActivityType.WATER -> R.drawable.ic_water_glass
        ActivityType.EXERCISE -> R.drawable.ic_exercise_goal
        ActivityType.SLEEP -> R.drawable.ic_sleep_goal
        else -> R.drawable.ic_glide_recipe_error
    }
    imageView.setImageResource(imageResource)
}

/**
 * Loads a recipe image from a URL into an ImageView using Glide.
 *
 * @param imageView The ImageView to load the image into
 * @param imgUrl The URL of the image to load
 */
@BindingAdapter("imgUrl")
fun bindRecipeImage(imageView: ImageView, imgUrl: String) {
    Glide.with(imageView.context)
        .load(imgUrl)
        .placeholder(R.drawable.ic_glide_placeholder_recipe)
        .error(R.drawable.ic_glide_recipe_error)
        .centerInside()
        .into(imageView)
}

/**
 * Progress Indicator Binding Adapters
 */

/**
 * Updates a ProgressBar to reflect the current progress towards an activity goal.
 *
 * @param progressBar The ProgressBar to update
 * @param activityItemUiState The UI state containing current and goal values
 */
@BindingAdapter("goalProgress")
fun bindGoalProgress(progressBar: ProgressBar, activityItemUiState: ActivityItemUiState) {
    val progressRatio = activityItemUiState.currentReading.toDouble() / activityItemUiState.goalReading
    val progressPercent = (progressRatio * 100).toInt()
    progressBar.progress = progressPercent
}
