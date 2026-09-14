package com.example.mygymoffline.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

enum class ThemeMode(val preferenceValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromPreference(value: String?): ThemeMode =
            entries.firstOrNull { it.preferenceValue == value } ?: SYSTEM
    }
}

class SettingsDataStore(private val prefs: SharedPreferences) {

    private val gson = Gson()

    private val _language = MutableStateFlow(prefs.getString("language", "en") ?: "en")
    val languageFlow: Flow<String> = _language

    private val _enabledEquipment = MutableStateFlow(
        try {
            val json = prefs.getString("enabled_equipment", "") ?: ""
            if (json.isEmpty()) {
                getDefaultEquipmentSet()
            } else {
                val type = object : TypeToken<Set<String>>() {}.type
                gson.fromJson<Set<String>>(json, type)
            }
        } catch (e: Exception) {
            getDefaultEquipmentSet()
        }
    )
    val enabledEquipmentFlow: Flow<Set<String>> = _enabledEquipment

    private val _gridMode = MutableStateFlow(prefs.getBoolean("grid_mode", true))
    val gridModeFlow: Flow<Boolean> = _gridMode

    private val _autoPlayGif = MutableStateFlow(prefs.getBoolean("auto_play_gif", true))
    val autoPlayGifFlow: Flow<Boolean> = _autoPlayGif

    private val _themeMode = MutableStateFlow(
        when {
            prefs.contains(THEME_MODE_KEY) -> ThemeMode.fromPreference(
                prefs.getString(THEME_MODE_KEY, null)
            )
            // Keep an existing installation's explicit light/dark choice during migration.
            prefs.contains(LEGACY_DARK_MODE_KEY) -> if (
                prefs.getBoolean(LEGACY_DARK_MODE_KEY, false)
            ) ThemeMode.DARK else ThemeMode.LIGHT
            else -> ThemeMode.SYSTEM
        }
    )
    val themeModeFlow: Flow<ThemeMode> = _themeMode

    private val _favoritesOnly = MutableStateFlow(prefs.getBoolean("favorites_only", false))
    val favoritesOnlyFlow: Flow<Boolean> = _favoritesOnly

    private val _customGifDirectoryUri = MutableStateFlow(prefs.getString("custom_gif_directory_uri", null))
    val customGifDirectoryUriFlow: Flow<String?> = _customGifDirectoryUri

    companion object {
        const val DEFAULT_LANGUAGE = "en"
        const val DEFAULT_GRID_MODE = true
        const val DEFAULT_AUTO_PLAY_GIF = true
        const val DEFAULT_FAVORITES_ONLY = false
        const val DEFAULT_THEME_MODE = "system"

        private const val THEME_MODE_KEY = "theme_mode"
        private const val LEGACY_DARK_MODE_KEY = "dark_mode"

        fun create(context: Context): SettingsDataStore {
            val prefs = context.getSharedPreferences("my_gym_offline_settings", Context.MODE_PRIVATE)
            return SettingsDataStore(prefs)
        }

        fun getDefaultEquipmentSet(): Set<String> {
            return setOf(
                "body weight", "dumbbell", "cable", "barbell", "leverage machine",
                "band", "smith machine", "kettlebell", "weighted", "stability ball",
                "ez barbell", "other"
            )
        }
    }

    suspend fun setLanguage(language: String) {
        prefs.edit().putString("language", language).apply()
        _language.value = language
    }

    suspend fun setEnabledEquipment(equipment: Set<String>) {
        val jsonString = gson.toJson(equipment)
        prefs.edit().putString("enabled_equipment", jsonString).apply()
        _enabledEquipment.value = equipment
    }

    suspend fun setGridMode(isGrid: Boolean) {
        prefs.edit().putBoolean("grid_mode", isGrid).apply()
        _gridMode.value = isGrid
    }

    suspend fun setAutoPlayGif(enabled: Boolean) {
        prefs.edit().putBoolean("auto_play_gif", enabled).apply()
        _autoPlayGif.value = enabled
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        prefs.edit()
            .putString(THEME_MODE_KEY, mode.preferenceValue)
            // Retain this value for versions that only understand the legacy preference.
            .putBoolean(LEGACY_DARK_MODE_KEY, mode == ThemeMode.DARK)
            .apply()
        _themeMode.value = mode
    }

    suspend fun setFavoritesOnly(enabled: Boolean) {
        prefs.edit().putBoolean("favorites_only", enabled).apply()
        _favoritesOnly.value = enabled
    }

    suspend fun setCustomGifDirectoryUri(uri: String?) {
        prefs.edit().putString("custom_gif_directory_uri", uri).apply()
        _customGifDirectoryUri.value = uri
    }
}
