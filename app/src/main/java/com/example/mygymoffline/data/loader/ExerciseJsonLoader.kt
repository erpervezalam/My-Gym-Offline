package com.example.mygymoffline.data.loader

import android.content.Context
import com.example.mygymoffline.data.db.Exercise
import com.example.mygymoffline.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

class ExerciseJsonLoader(private val context: Context) {

    @Suppress("UNUSED_PARAMETER")
    suspend fun loadExercises(): Result<List<Exercise>> = withContext(Dispatchers.IO) {
        try {
            AppLogger.i("ExerciseJsonLoader", "Loading exercises from assets/exercises.json")
            val inputStream = context.assets.open("exercises.json")
            val reader = InputStreamReader(inputStream, "UTF-8")
            val json = reader.readText()
            reader.close()

            val exercises = Json { ignoreUnknownKeys = true }.decodeFromString<List<JsonExercise>>(json)
                .map { it.toExercise() }

            AppLogger.i("ExerciseJsonLoader", "Loaded ${exercises.size} exercises from JSON")
            Result.success(exercises)
        } catch (e: Exception) {
            AppLogger.e("ExerciseJsonLoader", "Failed to load exercises from JSON", e)
            Result.failure(e)
        }
    }

    private inline class JsonExercise(
        val id: String,
        val name: String,
        val category: String,
        val body_part: String,
        val equipment: String,
        val instructions: Map<String, String>,
        val instruction_steps: Map<String, List<String>>,
        val muscle_group: String,
        val secondary_muscles: List<String>,
        val target: String,
        val media_id: String,
        val image: String,
        val gif_url: String,
        val attribution: String,
        val created_at: String
    ) {
        fun toExercise(): Exercise {
            return Exercise(
                id = id,
                name = name,
                category = category,
                equipment = equipment,
                target = target,
                muscleGroup = muscle_group,
                secondaryMusclesJson = Json.encodeToString(secondary_muscles),
                instructionsJson = Json.encodeToString(instructions),
                instructionStepsJson = Json.encodeToString(instruction_steps),
                imagePath = image,
                gifPath = gif_url,
                mediaId = media_id,
                attribution = attribution,
                isFavorite = false,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}