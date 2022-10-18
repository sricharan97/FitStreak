package com.apptimistiq.android.fitstreak.utils

import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
import com.apptimistiq.android.fitstreak.main.data.domain.RecipeTrackUiState
import com.apptimistiq.android.fitstreak.main.progressTrack.ActivityListAdapter


@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<ActivityItemUiState>?) {

    val adapter = recyclerView.adapter as ActivityListAdapter
    adapter.submitList(data)

}

@BindingAdapter("activityIcon")
fun bindActivityIcon(imageView: ImageView, activityType: ActivityType) {

    val imageResource = when (activityType) {
        ActivityType.STEP -> R.drawable.ic_step_goal
        ActivityType.WATER -> R.drawable.ic_water_glass
        ActivityType.EXERCISE -> R.drawable.ic_exercise_goal
        ActivityType.SLEEP -> R.drawable.ic_sleep_goal
    }

    imageView.setImageResource(imageResource)

}

@BindingAdapter("goalProgress")

fun bindGoalProgress(progressBar: ProgressBar, activityItemUiState: ActivityItemUiState) {

    val progressNum =
        activityItemUiState.currentReading.toDouble().div(activityItemUiState.goalReading)
    progressBar.progress = (progressNum * 100).toInt()

}

@BindingAdapter("recipeInfo")
fun bindRecipeInfo(textView: TextView, recipeTrackState: RecipeTrackUiState) {

    val recipeList = recipeTrackState.recipes

    val recipesNumber = recipeList.size

    Log.d(
        "BindingAdapters", "setting on ${recipeTrackState.recipeType} with list size being" +
                " ${recipeList.size}"
    )

    if (recipeList.isNotEmpty()) {
        textView.text =
            "${recipeTrackState.recipeType} has fetched $recipesNumber recipes with first one" +
                    "being ${recipeList[0].title} with ${recipeList[0].calories} calories "
    }


}