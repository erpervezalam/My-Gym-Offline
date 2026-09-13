package com.example.mygymoffline.data.repository

import com.example.mygymoffline.data.db.Exercise
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseReactionsTest {
    @Test
    fun favoriteReaction_marksFavoriteAndClearsDislike() {
        val updated = exercise(isDisliked = true).toggleFavoriteReaction()

        assertTrue(updated.isFavorite)
        assertFalse(updated.isDisliked)
    }

    @Test
    fun favoriteReaction_togglesAnExistingFavoriteOff() {
        val updated = exercise(isFavorite = true).toggleFavoriteReaction()

        assertFalse(updated.isFavorite)
        assertFalse(updated.isDisliked)
    }

    @Test
    fun dislikeReaction_marksDislikeAndClearsFavorite() {
        val updated = exercise(isFavorite = true).toggleDislikeReaction()

        assertFalse(updated.isFavorite)
        assertTrue(updated.isDisliked)
    }

    @Test
    fun dislikeReaction_togglesAnExistingDislikeOff() {
        val updated = exercise(isDisliked = true).toggleDislikeReaction()

        assertFalse(updated.isFavorite)
        assertFalse(updated.isDisliked)
    }

    private fun exercise(
        isFavorite: Boolean = false,
        isDisliked: Boolean = false
    ) = Exercise(
        id = "test",
        name = "Test exercise",
        category = "back",
        equipment = "barbell",
        target = "back",
        muscleGroup = "back",
        secondaryMusclesJson = "[]",
        instructionsJson = "{}",
        instructionStepsJson = "{}",
        imagePath = "",
        gifPath = "",
        mediaId = "",
        attribution = "",
        isFavorite = isFavorite,
        isDisliked = isDisliked
    )
}
