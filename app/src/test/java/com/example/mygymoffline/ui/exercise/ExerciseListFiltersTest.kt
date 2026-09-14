package com.example.mygymoffline.ui.exercise

import com.example.mygymoffline.data.db.Exercise
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseListFiltersTest {
    @Test
    fun favoritesFilter_onlyShowsFavoriteExercisesWhenEnabled() {
        val favorite = exercise("1", isFavorite = true)
        val ordinary = exercise("2")

        assertEquals(listOf(favorite), listOf(favorite, ordinary).filterFavorites(true))
        assertEquals(listOf(favorite, ordinary), listOf(favorite, ordinary).filterFavorites(false))
    }

    private fun exercise(id: String, isFavorite: Boolean = false) = Exercise(
        id = id, name = "Exercise", category = "back", equipment = "barbell", target = "back",
        muscleGroup = "back", secondaryMusclesJson = "[]", instructionsJson = "{}", instructionStepsJson = "{}",
        imagePath = "", gifPath = "", mediaId = "", attribution = "", isFavorite = isFavorite
    )
}
