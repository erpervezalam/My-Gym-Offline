package com.example.mygymoffline.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavControllerTest {

    @Test
    fun popBackStack_returnsToThePreviousDestination() {
        val controller = NavController()
        val exerciseList = Destination.ExerciseList("cardio")
        val exerciseDetail = Destination.ExerciseDetail("0001")

        controller.navigate(exerciseList)
        controller.navigate(exerciseDetail)

        assertTrue(controller.popBackStack())
        assertEquals(exerciseList, controller.currentDestination)
        assertTrue(controller.popBackStack())
        assertEquals(Destination.Main, controller.currentDestination)
        assertFalse(controller.popBackStack())
    }
}
