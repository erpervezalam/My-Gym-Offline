package com.example.mygymoffline.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mygymoffline.R
import com.example.mygymoffline.data.db.Exercise
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.navigation.NavController
import com.example.mygymoffline.navigation.Destination
import com.example.mygymoffline.ui.components.EmptyState
import com.example.mygymoffline.ui.media.GifSourceResolver
import com.example.mygymoffline.util.Telemetry
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    navController: NavController,
    repository: ExerciseRepository,
    category: String,
    gifSourceResolver: GifSourceResolver,
    onBackClick: () -> Unit
) {
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val exercises by if (searchQuery.isNotBlank()) {
        repository.searchExercises(searchQuery).collectAsStateWithLifecycle(initialValue = emptyList())
    } else {
        repository.getExercisesForCategory(category).collectAsStateWithLifecycle(initialValue = emptyList())
    }
    val visibleExercises = exercises.filterFavorites(showFavoritesOnly)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text(text = category.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (showFavoritesOnly) "Show all exercises" else "Show favorites only",
                            tint = if (showFavoritesOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!showSearch) {
                FloatingActionButton(onClick = { showSearch = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search exercises")
                }
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {

            if (showSearch) {
                androidx.compose.material3.TextField(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        IconButton(onClick = {
                            searchQuery = ""
                            showSearch = false
                        }) { Icon(Icons.Default.Close, contentDescription = "Close search") }
                    },
                    placeholder = { Text("Search exercises...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            }

            if (visibleExercises.isEmpty()) {
                EmptyState(
                    message = if (showFavoritesOnly) {
                        "No favorite exercises in this category"
                    } else {
                        "No exercises found for this category"
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(visibleExercises, key = { it.id }) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            gifSourceResolver = gifSourceResolver,
                            onClick = {
                                Telemetry.trackEvent("exercise_click", "ExerciseList:$category", mapOf("exercise_id" to exercise.id))
                                navController.navigate(Destination.ExerciseDetail(exercise.id))
                            },
                            onFavoriteClick = {
                                coroutineScope.launch { repository.toggleFavorite(exercise.id) }
                                Telemetry.trackEvent("favorite_toggle", "ExerciseList:$category", mapOf("exercise_id" to exercise.id, "is_favorite" to !exercise.isFavorite))
                            },
                            onDislikeClick = {
                                coroutineScope.launch { repository.toggleDislike(exercise.id) }
                                Telemetry.trackEvent("dislike_toggle", "ExerciseList:$category", mapOf("exercise_id" to exercise.id, "is_disliked" to !exercise.isDisliked))
                            }
                        )
                    }
                }
            }
        }
    }
}

internal fun List<Exercise>.filterFavorites(showFavoritesOnly: Boolean): List<Exercise> =
    if (showFavoritesOnly) filter { it.isFavorite } else this

@Composable
fun ExerciseCard(
    exercise: Exercise,
    gifSourceResolver: GifSourceResolver,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDislikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(118.dp)) {
                AsyncImage(
                    model = gifSourceResolver.modelFor(exercise.gifPath),
                    contentDescription = "Exercise GIF",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                        .background(Color.DarkGray.copy(alpha = 0.68f), RoundedCornerShape(9.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onFavoriteClick, modifier = Modifier.size(20.dp)) {
                        Icon(
                            imageVector = if (exercise.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (exercise.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (exercise.isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                    IconButton(onClick = onDislikeClick, modifier = Modifier.size(20.dp)) {
                        Icon(
                            imageVector = if (exercise.isDisliked) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                            contentDescription = if (exercise.isDisliked) "Remove dislike" else "Dislike exercise",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(exercise.name.replaceFirstChar { it.uppercase() }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${exercise.equipment.replaceFirstChar { it.uppercase() }} • ${exercise.target.replaceFirstChar { it.uppercase() }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
        }
    }
}
