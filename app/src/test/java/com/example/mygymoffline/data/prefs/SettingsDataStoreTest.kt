package com.example.mygymoffline.data.prefs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsDataStoreTest {

    @Test
    fun defaultEquipmentSet_containsTheCoreEquipmentTypes() {
        val equipment = SettingsDataStore.getDefaultEquipmentSet()

        assertEquals(12, equipment.size)
        assertTrue("body weight" in equipment)
        assertTrue("dumbbell" in equipment)
        assertTrue("barbell" in equipment)
    }
}
