package com.example.mygymoffline.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygymoffline.data.db.Exercise
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.ui.media.GifSourceResolver
import com.example.mygymoffline.util.Telemetry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import com.example.mygymoffline.ui.media.ExerciseGif

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExerciseDetailBottomSheet(
    exercise: Exercise,
    repository: ExerciseRepository,
    selectedLanguage: String,
    gifSourceResolver: GifSourceResolver,
    autoPlayGif: Boolean,
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercise.name.toTitleCase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isFavorite = !isFavorite
                        coroutineScope.launch { repository.toggleFavorite(exercise.id) }
                        Telemetry.trackEvent("favorite_toggle", "DetailBottomSheet", mapOf("exercise_id" to exercise.id, "is_favorite" to isFavorite))
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
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
                            tint = if (isDisliked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .align(Alignment.CenterHorizontally)
                    .aspectRatio(1f)
            ) {
                ExerciseGif(
                    model = gifSourceResolver.modelFor(exercise.gifPath),
                    contentDescription = "${exercise.name} exercise GIF",
                    autoPlay = autoPlayGif,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DetailRow("Equipment", exercise.equipment.toDisplayText())
                DetailRow("Target Muscle", exercise.target.toDisplayText())
                DetailRow("Muscle Group", exercise.muscleGroup.toDisplayText())
                if (secondaryMuscles.isNotEmpty()) {
                    DetailRow("Secondary Muscles", secondaryMuscles.joinToString(", ") { it.toDisplayText() })
                }
            }

            HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = "Instructions",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .align(Alignment.CenterHorizontally)
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentSteps.isNotEmpty()) {
                    currentSteps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}.",
                                modifier = Modifier.padding(end = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = step, fontSize = 14.sp, modifier = Modifier.weight(1f))
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
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            fontSize = 13.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.58f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun String.toDisplayText(): String =
    replaceFirstChar { it.uppercase() }

private fun String.toTitleCase(): String =
    trim().split(Regex("\\s+")).joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.titlecase() }
    }
