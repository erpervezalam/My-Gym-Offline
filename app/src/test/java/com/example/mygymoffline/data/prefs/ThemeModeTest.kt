package com.example.mygymoffline.data.prefs

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {
    @Test
    fun fromPreference_acceptsOnlyKnownThemeModes() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromPreference("system"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromPreference("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromPreference("dark"))
    }

    @Test
    fun fromPreference_defaultsSafelyForUnknownValues() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromPreference(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromPreference("unsupported"))
    }
}
