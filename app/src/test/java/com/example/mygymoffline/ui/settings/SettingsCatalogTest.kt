package com.example.mygymoffline.ui.settings

import com.example.mygymoffline.data.prefs.SettingsDataStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsCatalogTest {
    @Test
    fun languageDropdown_exposesEverySupportedLanguageOnce() {
        assertEquals(10, SettingsCatalog.languages.size)
        assertEquals(10, SettingsCatalog.languages.map { it.first }.toSet().size)
        assertTrue(SettingsCatalog.languages.any { it == "en" to "English" })
    }

    @Test
    fun equipmentFilters_exposeEverySupportedEquipmentType() {
        val equipment = SettingsDataStore.getDefaultEquipmentSet()
        assertEquals(12, equipment.size)
        assertTrue("body weight" in equipment)
        assertTrue("stability ball" in equipment)
    }
}
