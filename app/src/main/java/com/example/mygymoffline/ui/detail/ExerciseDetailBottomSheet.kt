package com.example.mygymoffline.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygymoffline.data.db.Exercise
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.util.Telemetry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

@Composable
fun ExerciseDetailBottomSheet(
    exercise: Exercise,
    repository: ExerciseRepository,
    selectedLanguage: String,
    onCloseClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(exercise.isFavorite) }
    var isDisliked by remember { mutableStateOf(exercise.isDisliked) }
    val coroutineScope = rememberCoroutineScope()

    val instructionsMap = remember(exercise) {
        Gson().fromJson<Map<String, String>>(
            exercise.instructionsJson,
            object : TypeToken<Map<String, String>>() {}.type
        ) ?: emptyMap()
    }
    val instructionStepsMap = remember(exercise) {
        Gson().fromJson<Map<String, List<String>>>(
            exercise.instructionStepsJson,
            object : TypeToken<Map<String, List<String>>>() {}.type
        ) ?: emptyMap()
    }
    val secondaryMuscles = remember(exercise) {
        Gson().fromJson<List<String>>(
            exercise.secondaryMusclesJson,
            object : TypeToken<List<String>>() {}.type
        ) ?: emptyList()
    }

    val currentInstructions = instructionsMap[selectedLanguage] ?: instructionsMap["en"] ?: ""
    val currentSteps = instructionStepsMap[selectedLanguage] ?: instructionStepsMap["en"] ?: emptyList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = {
                        isFavorite = !isFavorite
                        coroutineScope.launch { repository.toggleFavorite(exercise.id) }
                        Telemetry.trackEvent("favorite_toggle", "DetailBottomSheet", mapOf("exercise_id" to exercise.id, "is_favorite" to isFavorite))
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) androidx.compose.material3.MaterialTheme.colorScheme.error else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        isDisliked = !isDisliked
                        coroutineScope.launch { repository.toggleDislike(exercise.id) }
                        Telemetry.trackEvent("dislike_toggle", "DetailBottomSheet", mapOf("exercise_id" to exercise.id, "is_disliked" to isDisliked))
                    }) {
                        Icon(
                            imageVector = if (isDisliked) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                            contentDescription = if (isDisliked) "Remove dislike" else "Dislike exercise",
                            tint = if (isDisliked) androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onCloseClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            // Details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Equipment", exercise.equipment)
                DetailRow("Target Muscle", exercise.target)
                DetailRow("Muscle Group", exercise.muscleGroup)
                if (secondaryMuscles.isNotEmpty()) {
                    DetailRow("Secondary Muscles", secondaryMuscles.joinToString(", "))
                }

                Divider(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                )

                // Instructions
                Text("Instructions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (currentSteps.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        currentSteps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${index + 1}. ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = step, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    Text(currentInstructions, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
