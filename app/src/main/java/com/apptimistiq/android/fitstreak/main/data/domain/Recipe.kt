package com.apptimistiq.android.fitstreak.main.data.domain

/**
 * Domain model representing a recipe in the FitStreak application.
 *
 * This class contains all essential information about a recipe that can be
 * displayed to the user, including its unique identifier, title, image URL,
 * and caloric content.
 *
 * @property id Unique identifier for the recipe
 * @property title The name/title of the recipe
 * @property imgUrl URL pointing to an image representation of the recipe
 * @property calories The caloric content of the recipe in kcal
 */
data class Recipe(
    val id: Int,
    val title: String,
    val imgUrl: String,
    val calories: Double
)
