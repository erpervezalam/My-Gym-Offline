package com.example.mygymoffline.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mygymoffline.R
import com.example.mygymoffline.ui.media.GifSourceResolver

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    onSettingsClick: () -> Unit,
    onGridListToggleClick: () -> Unit,
    isGridMode: Boolean,
    onFavoritesToggleClick: () -> Unit,
    showFavoritesOnly: Boolean
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        navigationIcon = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onGridListToggleClick) {
                    Icon(
                        imageVector = if (isGridMode) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = if (isGridMode) "Switch to list view" else "Switch to grid view"
                    )
                }
                IconButton(onClick = onFavoritesToggleClick) {
                    Icon(
                        imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (showFavoritesOnly) "Show all exercises" else "Show favorites only",
                        tint = if (showFavoritesOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

@Composable
fun BodyPartCard(
    name: String,
    exerciseCount: Int,
    thumbnailPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (thumbnailPath == null) 96.dp else 220.dp)
            .padding(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Keep text outside the preview so it remains readable on every frame.
            thumbnailPath?.let { path ->
                AsyncImage(
                    model = GifSourceResolver.bundledAssetUri(path),
                    contentDescription = "$name exercise preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "$exerciseCount exercises", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: com.example.mygymoffline.data.db.Exercise,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDislikeClick: () -> Unit,
    onGifClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // GIF Thumbnail
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = GifSourceResolver.bundledAssetUri(exercise.gifPath),
                    contentDescription = "Exercise GIF",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth()
                        .clickable { onGifClick() }
                )
            }

            // Exercise Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = exercise.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Equipment badge using Text with background
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .padding(horizontal = 8.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = exercise.equipment,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = exercise.target,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Favorite and Dislike Buttons
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (exercise.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (exercise.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (exercise.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDislikeClick) {
                    Icon(
                        imageVector = if (exercise.isDisliked) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                        contentDescription = if (exercise.isDisliked) "Remove dislike" else "Dislike exercise",
                        tint = if (exercise.isDisliked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Search
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
            Text(
                text = message,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
