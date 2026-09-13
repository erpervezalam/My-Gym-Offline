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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val exercises by if (searchQuery.isNotBlank()) {
        repository.searchExercises(searchQuery).collectAsStateWithLifecycle(initialValue = emptyList())
    } else {
        repository.getExercisesForCategory(category).collectAsStateWithLifecycle(initialValue = emptyList())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text(text = category.capitalize(), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

            // Search bar - using TextField instead of SearchBar
            androidx.compose.material3.TextField(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                placeholder = { Text("Search exercises...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )

            if (exercises.isEmpty()) {
                EmptyState(message = "No exercises found for this category")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                ) {
                    items(exercises) { exercise ->
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
            AsyncImage(
                model = gifSourceResolver.modelFor(exercise.gifPath),
                contentDescription = "Exercise GIF",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name.replaceFirstChar { it.uppercase() }, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${exercise.equipment.replaceFirstChar { it.uppercase() }} • ${exercise.target.replaceFirstChar { it.uppercase() }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (exercise.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (exercise.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (exercise.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onDislikeClick) {
                        Icon(
                            imageVector = if (exercise.isDisliked) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                            contentDescription = if (exercise.isDisliked) "Remove dislike" else "Dislike exercise",
                            tint = if (exercise.isDisliked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
