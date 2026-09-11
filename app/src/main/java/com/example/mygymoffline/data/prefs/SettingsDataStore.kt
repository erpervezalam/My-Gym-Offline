package com.example.mygymoffline.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.PreferencesDataStore
import androidx.datastore.preferences.PreferencesDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.json.Json

class SettingsDataStore(private val dataStore: PreferencesDataStore) {
    companion object {
        private const val DATA_STORE_NAME = "settings"

        // Keys
        val KEY_LANGUAGE = preferencesKey<String>("language")
        val KEY_ENABLED_EQUIPMENT = preferencesKey<String>("enabled_equipment") // JSON string of Set<String>
        val KEY_GRID_MODE = preferencesKey<Boolean>("grid_mode")
        val KEY_AUTO_PLAY_GIF = preferencesKey<Boolean>("auto_play_gif")
        val KEY_FAVORITES_ONLY = preferencesKey<Boolean>("favorites_only")
        val KEY_GIF_DOWNLOAD_COMPLETE = preferencesKey<Boolean>("gif_download_complete")
        val KEY_GIF_CACHE_SIZE = preferencesKey<Long>("gif_cache_size")

        // Defaults
        const val DEFAULT_LANGUAGE = "en"
        const val DEFAULT_GRID_MODE = true
        const val DEFAULT_AUTO_PLAY_GIF = true
        const val DEFAULT_FAVORITES_ONLY = false
        const val DEFAULT_GIF_DOWNLOAD_COMPLETE = false
        const val DEFAULT_GIF_CACHE_SIZE = 0L

        fun create(context: Context): SettingsDataStore {
            val dataStore = PreferencesDataStoreFactory.create(
                context,
                DATA_STORE_NAME,
                produceMigrations = { listOf() }
            )
            return SettingsDataStore(dataStore)
        }
    }

    // Language
    val languageFlow: Flow<String> = dataStore.data
        .map { it[KEY_LANGUAGE] ?: DEFAULT_LANGUAGE }
        .distinctUntilChanged()

    suspend fun setLanguage(language: String) {
        dataStore.edit { it[KEY_LANGUAGE] = language }
    }

    // Enabled Equipment (stored as JSON string)
    val enabledEquipmentFlow: Flow<Set<String>> = dataStore.data
        .map { it[KEY_ENABLED_EQUIPMENT]?.let { parseEquipmentJson(it) } ?: getDefaultEquipmentSet() }
        .distinctUntilChanged()

    suspend fun setEnabledEquipment(equipment: Set<String>) {
        dataStore.edit { it[KEY_ENABLED_EQUIPMENT] = equipmentToJson(equipment) }
    }

    // Grid Mode
    val gridModeFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_GRID_MODE] ?: DEFAULT_GRID_MODE }
        .distinctUntilChanged()

    suspend fun setGridMode(isGrid: Boolean) {
        dataStore.edit { it[KEY_GRID_MODE] = isGrid }
    }

    // Auto-play GIF
    val autoPlayGifFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_AUTO_PLAY_GIF] ?: DEFAULT_AUTO_PLAY_GIF }
        .distinctUntilChanged()

    suspend fun setAutoPlayGif(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_PLAY_GIF] = enabled }
    }

    // Favorites Only
    val favoritesOnlyFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_FAVORITES_ONLY] ?: DEFAULT_FAVORITES_ONLY }
        .distinctUntilChanged()

    suspend fun setFavoritesOnly(enabled: Boolean) {
        dataStore.edit { it[KEY_FAVORITES_ONLY] = enabled }
    }

    // GIF Download Complete
    val gifDownloadCompleteFlow: Flow<Boolean> = dataStore.data
        .map { it[KEY_GIF_DOWNLOAD_COMPLETE] ?: DEFAULT_GIF_DOWNLOAD_COMPLETE }
        .distinctUntilChanged()

    suspend fun setGifDownloadComplete(complete: Boolean) {
        dataStore.edit { it[KEY_GIF_DOWNLOAD_COMPLETE] = complete }
    }

    // GIF Cache Size
    val gifCacheSizeFlow: Flow<Long> = dataStore.data
        .map { it[KEY_GIF_CACHE_SIZE] ?: DEFAULT_GIF_CACHE_SIZE }
        .distinctUntilChanged()

    suspend fun setGifCacheSize(size: Long) {
        dataStore.edit { it[KEY_GIF_CACHE_SIZE] = size }
    }

    // Helpers
    private fun parseEquipmentJson(json: String): Set<String> {
        return try {
            Json { ignoreUnknownKeys = true }.decodeFromString(json)
        } catch (e: Exception) {
            getDefaultEquipmentSet()
        }
    }

    private fun equipmentToJson(equipment: Set<String>): String {
        return Json.encodeToString(equipment)
    }

    private fun getDefaultEquipmentSet(): Set<String> {
        return setOf(
            "body weight", "dumbbell", "cable", "barbell", "leverage machine",
            "band", "smith machine", "kettlebell", "weighted", "stability ball",
            "ez barbell", "other"
        )
    }
}