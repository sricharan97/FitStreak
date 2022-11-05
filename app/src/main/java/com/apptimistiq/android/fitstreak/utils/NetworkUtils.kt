package com.apptimistiq.android.fitstreak.utils

import org.json.JSONObject

private const val RECIPE_URL = "spoonacularSourceUrl"

fun parseRecipeUrl(recipeInfoObject: JSONObject): String? {

    return recipeInfoObject.getString(RECIPE_URL)

}