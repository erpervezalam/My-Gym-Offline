package com.example.mygymoffline.data.prefs

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsDataStoreInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @After
    fun resetDisplayPreferences() = runBlocking {
        SettingsDataStore.create(context).setGridMode(SettingsDataStore.DEFAULT_GRID_MODE)
        SettingsDataStore.create(context).setAutoPlayGif(SettingsDataStore.DEFAULT_AUTO_PLAY_GIF)
        SettingsDataStore.create(context).setDarkMode(false)
    }

    @Test
    fun defaultEquipmentSet_isAvailableOnDevice() {
        val equipment = SettingsDataStore.getDefaultEquipmentSet()

        assertTrue("cable" in equipment)
        assertTrue("kettlebell" in equipment)
    }

    @Test
    fun displayPreferences_persistAcrossStoreInstances() = runBlocking {
        val settings = SettingsDataStore.create(context)

        settings.setGridMode(false)
        settings.setAutoPlayGif(false)
        settings.setDarkMode(true)

        val reloadedSettings = SettingsDataStore.create(context)
        assertFalse(reloadedSettings.gridModeFlow.first())
        assertFalse(reloadedSettings.autoPlayGifFlow.first())
        assertTrue(reloadedSettings.darkModeFlow.first())
    }
}
