package com.example.mygymoffline.data.loader

import android.content.Context
import com.example.mygymoffline.data.db.Exercise
import com.example.mygymoffline.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.StringSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import java.io.InputStreamReader

class ExerciseJsonLoader(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    @Suppress("UNUSED_PARAMETER")
    suspend fun loadExercises(): Result<List<Exercise>> = withContext(Dispatchers.IO) {
        try {
            AppLogger.i("ExerciseJsonLoader", "Loading exercises from assets/exercises.json")
            val inputStream = context.assets.open("exercises.json")
            val reader = InputStreamReader(inputStream, "UTF-8")
            val jsonText = reader.readText()
            reader.close()

            val exercises = json.decodeFromString<List<JsonExercise>>(jsonText)
                .map { it.toExercise() }

            AppLogger.i("ExerciseJsonLoader", "Loaded ${exercises.size} exercises from JSON")
            Result.success(exercises)
        } catch (e: Exception) {
            AppLogger.e("ExerciseJsonLoader", "Failed to load exercises from JSON", e)
            Result.failure(e)
        }
    }

    @kotlinx.serialization.Serializable
    private data class JsonExercise(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String,
        @SerialName("category") val category: String,
        @SerialName("body_part") val bodyPart: String,
        @SerialName("equipment") val equipment: String,
        @SerialName("instructions") val instructions: Map<String, String>,
        @SerialName("instruction_steps") val instructionSteps: Map<String, List<String>>,
        @SerialName("muscle_group") val muscleGroup: String,
        @SerialName("secondary_muscles") val secondaryMuscles: List<String>,
        @SerialName("target") val target: String,
        @SerialName("media_id") val mediaId: String,
        @SerialName("image") val image: String,
        @SerialName("gif_url") val gifUrl: String,
        @SerialName("attribution") val attribution: String,
        @SerialName("created_at") val createdAt: String
    ) {
        fun toExercise(): Exercise {
            val localJson = Json { ignoreUnknownKeys = true }
            return Exercise(
                id = id,
                name = name,
                category = category,
                equipment = equipment,
                target = target,
                muscleGroup = muscleGroup,
                secondaryMusclesJson = localJson.encodeToString(ListSerializer(StringSerializer), secondaryMuscles),
                instructionsJson = localJson.encodeToString(MapSerializer(StringSerializer, StringSerializer), instructions),
                instructionStepsJson = localJson.encodeToString(MapSerializer(StringSerializer, ListSerializer(StringSerializer)), instructionSteps),
                imagePath = image,
                gifPath = gifUrl,
                mediaId = mediaId,
                attribution = attribution,
                isFavorite = false,
                isDisliked = false,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}