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


@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<ActivityItemUiState>?) {

    val adapter = recyclerView.adapter as ActivityListAdapter
    adapter.submitList(data)

}

@BindingAdapter("listData")
fun bindRecipeParentRecyclerView(recyclerView: RecyclerView, data: List<RecipeTrackUiState>?) {

    val adapter = recyclerView.adapter as RecipeTypeListAdapter
    adapter.submitList(data)

}


@BindingAdapter("listData")
fun bindRecipeChildRecyclerView(recyclerView: RecyclerView, data: List<Recipe>?) {

    val adapter = recyclerView.adapter as RecipeListAdapter
    adapter.submitList(data)

}

@BindingAdapter("activityIcon")
fun bindActivityIcon(imageView: ImageView, activityType: ActivityType) {

    val imageResource = when (activityType) {
        ActivityType.STEP -> R.drawable.ic_step_goal
        ActivityType.WATER -> R.drawable.ic_water_glass
        ActivityType.EXERCISE -> R.drawable.ic_exercise_goal
        ActivityType.SLEEP -> R.drawable.ic_sleep_goal
        else -> {
            R.drawable.ic_glide_recipe_error
        }
    }

    imageView.setImageResource(imageResource)

}

@BindingAdapter("goalProgress")

fun bindGoalProgress(progressBar: ProgressBar, activityItemUiState: ActivityItemUiState) {

    val progressNum =
        activityItemUiState.currentReading.toDouble().div(activityItemUiState.goalReading)
    progressBar.progress = (progressNum * 100).toInt()

}

@BindingAdapter("imgUrl")
fun bindRecipeImage(imageView: ImageView, imgUrl: String) {

    Glide.with(imageView.context).load(imgUrl).placeholder(R.drawable.ic_glide_placeholder_recipe)
        .error(R.drawable.ic_glide_recipe_error)
        .centerInside()
        .into(imageView)

}

