package com.example.mygymoffline.data.loader

import android.content.Context
import com.example.mygymoffline.data.db.Exercise
import com.example.mygymoffline.util.AppLogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

private val gson = Gson()

class ExerciseJsonLoader(private val context: Context) {

    @Suppress("UNUSED_PARAMETER")
    suspend fun loadExercises(): Result<List<Exercise>> = withContext(Dispatchers.IO) {
        try {
            AppLogger.i("ExerciseJsonLoader", "Loading exercises from assets/exercises.json")
            val inputStream = context.assets.open("exercises.json")
            val reader = InputStreamReader(inputStream, "UTF-8")

            val exerciseListType = object : TypeToken<List<JsonExercise>>() {}.type
            val jsonExercises = gson.fromJson<List<JsonExercise>>(reader, exerciseListType)
            reader.close()

            val exercises = jsonExercises.map { it.toExercise() }

            AppLogger.i("ExerciseJsonLoader", "Loaded ${exercises.size} exercises from JSON")
            Result.success(exercises)
        } catch (e: Exception) {
            AppLogger.e("ExerciseJsonLoader", "Failed to load exercises from JSON", e)
            Result.failure(e)
        }
    }

    private data class JsonExercise(
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
                secondaryMusclesJson = gson.toJson(secondary_muscles),
                instructionsJson = gson.toJson(instructions),
                instructionStepsJson = gson.toJson(instruction_steps),
                imagePath = image,
                gifPath = gif_url,
                mediaId = media_id,
                attribution = attribution,
                isFavorite = false,
                isDisliked = false,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}