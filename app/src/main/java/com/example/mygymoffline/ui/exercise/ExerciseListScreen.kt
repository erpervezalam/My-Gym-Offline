package com.example.mygymoffline.ui.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.mygymoffline.R
import com.example.mygymoffline.data.db.Exercise
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.navigation.NavController
import com.example.mygymoffline.navigation.Destination
import com.example.mygymoffline.ui.components.EmptyState
import com.example.mygymoffline.util.Telemetry
import kotlinx.coroutines.flow.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle

@Composable
fun ExerciseListScreen(
    navController: NavController,
    repository: ExerciseRepository,
    category: String,
    onBackClick: () -> Unit
) {
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                ),
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    androidx.compose.material3.IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (showFavoritesOnly) "Show all exercises" else "Show favorites only",
                            tint = if (showFavoritesOnly) androidx.compose.material3.MaterialTheme.colorScheme.error else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            // Search bar
            androidx.compose.material3.SearchBar(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                text = searchQuery,
                onTextChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                placeholder = { Text("Search exercises...") },
                colors = androidx.compose.material3.SearchBarDefaults.searchBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
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
                            onClick = {
                                Telemetry.trackEvent("exercise_click", "ExerciseList:$category", mapOf("exercise_id" to exercise.id))
                                navController.navigate(Destination.ExerciseDetail(exercise.id))
                            },
                            onFavoriteClick = {
                                repository.toggleFavorite(exercise.id)
                                Telemetry.trackEvent("favorite_toggle", "ExerciseList:$category", mapOf("exercise_id" to exercise.id, "is_favorite" to !exercise.isFavorite))
                            },
                            onDislikeClick = {
                                repository.toggleDislike(exercise.id)
                                Telemetry.trackEvent("dislike_toggle", "ExerciseList:$category", mapOf("exercise_id" to exercise.id, "is_disliked" to !exercise.isDisliked))
                            },
                            onGifClick = {
                                Telemetry.trackEvent("gif_click", "ExerciseList:$category", mapOf("exercise_id" to exercise.id))
                                navController.navigate(Destination.FullscreenGif(exercise.id))
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
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDislikeClick: () -> Unit,
    onGifClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // GIF as background
            AsyncImage(
                model = exercise.gifPath,
                contentDescription = "Exercise GIF",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay with info
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = exercise.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = exercise.equipment,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "• ${exercise.target}",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Favorite and Dislike buttons on top right
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (exercise.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (exercise.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (exercise.isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onDislikeClick) {
                        Icon(
                            imageVector = if (exercise.isDisliked) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                            contentDescription = if (exercise.isDisliked) "Remove dislike" else "Dislike exercise",
                            tint = if (exercise.isDisliked) Color.White.copy(alpha = 0.7f) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Invisible clickable area for GIF click (whole card)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onGifClick() },
                contentAlignment = Alignment.Center
            )
        }
    }
}