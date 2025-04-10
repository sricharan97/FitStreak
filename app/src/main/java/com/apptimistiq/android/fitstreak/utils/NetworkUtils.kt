package com.apptimistiq.android.fitstreak.utils

import org.json.JSONObject

/**
 * Key constant for extracting recipe URL from the JSON response.
 */
private const val RECIPE_URL = "spoonacularSourceUrl"

/**
 * Parses and extracts the recipe URL from a JSON object containing recipe information.
 *
 * This function looks for the "spoonacularSourceUrl" field in the provided JSON object
 * and returns its value.
 *
 * @param recipeInfoObject The JSON object containing recipe information
 * @return The recipe URL as a string, or null if the URL cannot be found or extracted
 * @throws org.json.JSONException if the key is not found in the JSON object
 */
fun parseRecipeUrl(recipeInfoObject: JSONObject): String? {
    return recipeInfoObject.getString(RECIPE_URL)
}
