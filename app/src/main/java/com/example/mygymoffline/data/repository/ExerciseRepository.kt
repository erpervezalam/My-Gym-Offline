package com.example.mygymoffline.data.repository

import android.content.Context
import com.example.mygymoffline.data.db.Exercise
import com.example.mygymoffline.data.db.ExerciseDao
import com.example.mygymoffline.data.db.ExerciseDatabase
import com.example.mygymoffline.data.loader.ExerciseJsonLoader
import com.example.mygymoffline.data.prefs.SettingsDataStore
import com.example.mygymoffline.util.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ExerciseRepository(
    private val context: Context,
    private val dao: ExerciseDao,
    private val settings: SettingsDataStore,
    private val jsonLoader: ExerciseJsonLoader,
    private val scope: CoroutineScope
) {

    companion object {
        private const val PREFS_INITIALIZED = "exercises_pre_populated"
    }

    suspend fun initialize() {
        AppLogger.i("ExerciseRepository", "Initializing repository")
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val initialized = prefs.getBoolean(PREFS_INITIALIZED, false)

        if (!initialized) {
            AppLogger.i("ExerciseRepository", "First launch - pre-populating database from JSON")
            val result = jsonLoader.loadExercises()
            result.onSuccess { exercises ->
                dao.insertAll(exercises)
                AppLogger.i("ExerciseRepository", "Inserted ${exercises.size} exercises into database")
                prefs.edit().putBoolean(PREFS_INITIALIZED, true).apply()
            }.onFailure { e ->
                AppLogger.e("ExerciseRepository", "Failed to pre-populate database", e)
            }
        } else {
            AppLogger.i("ExerciseRepository", "Database already initialized")
        }
    }

    // Combined flow: category + equipment filter + favorites filter
    fun getExercisesForCategory(category: String): Flow<List<Exercise>> {
        return combine(
            settings.enabledEquipmentFlow,
            settings.favoritesOnlyFlow
        ) { enabledEquipment, favoritesOnly ->
            Pair(enabledEquipment, favoritesOnly)
        }.flatMapLatest { (enabledEquipment, favoritesOnly) ->
            if (favoritesOnly) {
                dao.getFavoritesByCategory(category)
            } else if (enabledEquipment.isNotEmpty()) {
                dao.getByCategoryAndEquipment(category, enabledEquipment.toList())
            } else {
                dao.getByCategory(category)
            }
        }.distinctUntilChanged()
    }

    fun getAllCategories(): Flow<List<String>> = dao.getAllCategories()

    fun getAllEquipment(): Flow<List<String>> = dao.getAllEquipment()

    fun getFavorites(): Flow<List<Exercise>> = dao.getFavorites()

    fun searchExercises(query: String): Flow<List<Exercise>> {
        return combine(
            settings.enabledEquipmentFlow,
            settings.favoritesOnlyFlow
        ) { enabledEquipment, favoritesOnly ->
            Pair(enabledEquipment, favoritesOnly)
        }.flatMapLatest { (enabledEquipment, favoritesOnly) ->
            if (favoritesOnly) {
                dao.getFavorites()
            } else if (enabledEquipment.isNotEmpty()) {
                dao.searchByNameAndEquipment(query, enabledEquipment.toList())
            } else {
                dao.searchByName(query)
            }
        }.distinctUntilChanged()
    }

    suspend fun toggleFavorite(exerciseId: String) {
        val exercise = dao.getById(exerciseId)
        if (exercise != null) {
            val updated = exercise.copy(isFavorite = !exercise.isFavorite)
            dao.update(updated)
            AppLogger.i("ExerciseRepository", "Toggled favorite for $exerciseId: ${updated.isFavorite}")
        }
    }

    suspend fun toggleDislike(exerciseId: String) {
        val exercise = dao.getById(exerciseId)
        if (exercise != null) {
            val updated = exercise.copy(isDisliked = !exercise.isDisliked)
            // If marking as disliked, remove from favorites
            val finalUpdated = if (updated.isDisliked) updated.copy(isFavorite = false) else updated
            dao.update(finalUpdated)
            AppLogger.i("ExerciseRepository", "Toggled dislike for $exerciseId: ${finalUpdated.isDisliked}")
        }
    }

    suspend fun getExerciseById(id: String): Exercise? = dao.getById(id)

    suspend fun getTotalCount(): Int = dao.getTotalCount()

    suspend fun getCountByCategory(category: String): Int = dao.getCountByCategory(category)
}