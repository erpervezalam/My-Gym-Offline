package com.example.mygymoffline.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.mygymoffline.data.db.Exercise
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.ui.components.EmptyState
import com.example.mygymoffline.util.Telemetry

@Composable
fun FullscreenGifScreen(
    exercise: Exercise,
    repository: ExerciseRepository,
    onCloseClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(exercise.isFavorite) }
    var isDisliked by remember { mutableStateOf(exercise.isDisliked) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Fullscreen GIF
        AsyncImage(
            model = exercise.gifPath,
            contentDescription = exercise.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )

        // Top bar
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { Text(text = exercise.name, fontWeight = FontWeight.Bold, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            ),
            navigationIcon = {
                androidx.compose.material3.IconButton(onClick = onCloseClick) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            },
            actions = {
                androidx.compose.material3.IconButton(onClick = {
                    isFavorite = !isFavorite
                    repository.toggleFavorite(exercise.id)
                    Telemetry.trackEvent("favorite_toggle", "FullscreenGif", mapOf("exercise_id" to exercise.id, "is_favorite" to isFavorite))
                }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
                androidx.compose.material3.IconButton(onClick = {
                    isDisliked = !isDisliked
                    repository.toggleDislike(exercise.id)
                    Telemetry.trackEvent("dislike_toggle", "FullscreenGif", mapOf("exercise_id" to exercise.id, "is_disliked" to isDisliked))
                }) {
                    Icon(
                        imageVector = if (isDisliked) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                        contentDescription = if (isDisliked) "Remove dislike" else "Dislike exercise",
                        tint = if (isDisliked) Color.White.copy(alpha = 0.7f) else Color.White
                    )
                }
            }
        )
    }
}