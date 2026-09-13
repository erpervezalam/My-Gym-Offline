package com.example.mygymoffline.data.prefs

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsDataStoreInstrumentedTest {

    @Test
    fun defaultEquipmentSet_isAvailableOnDevice() {
        val equipment = SettingsDataStore.getDefaultEquipmentSet()

        assertTrue("cable" in equipment)
        assertTrue("kettlebell" in equipment)
    }
}
