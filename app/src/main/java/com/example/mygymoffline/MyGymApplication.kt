package com.example.mygymoffline

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.mygymoffline.data.db.ExerciseDatabase
import com.example.mygymoffline.data.loader.ExerciseJsonLoader
import com.example.mygymoffline.data.prefs.SettingsDataStore
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.util.AppLogger
import com.example.mygymoffline.util.Telemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MyGymApplication : Application() {
    private val supervisorJob = SupervisorJob()
    val coroutineScope = CoroutineScope(Dispatchers.Default + supervisorJob)

    lateinit var exerciseDatabase: ExerciseDatabase
    lateinit var exerciseRepository: ExerciseRepository
    lateinit var settingsDataStore: SettingsDataStore
    lateinit var exerciseJsonLoader: ExerciseJsonLoader

    override fun onCreate() {
        super.onCreate()

        // Initialize logging first
        AppLogger.init(this)

        // Initialize Telemetry
        Telemetry.init(this, coroutineScope)

        // Initialize database
        exerciseDatabase = ExerciseDatabase.getInstance(this)
        exerciseJsonLoader = ExerciseJsonLoader(this)
        settingsDataStore = SettingsDataStore.create(this)

        // Initialize repository
        exerciseRepository = ExerciseRepository(
            context = this,
            dao = exerciseDatabase.exerciseDao(),
            settings = settingsDataStore,
            jsonLoader = exerciseJsonLoader,
            scope = coroutineScope
        )

        // Initialize repository asynchronously
        coroutineScope.launch {
            exerciseRepository.initialize()
        }

        AppLogger.i("MyGymApplication", "Application initialized")
    }

    override fun onTerminate() {
        super.onTerminate()
        supervisorJob.cancel()
        Telemetry.shutdown()
        AppLogger.i("MyGymApplication", "Application terminated")
    }
}