package com.example.mygymoffline.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mygymoffline.R
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.navigation.NavController
import com.example.mygymoffline.navigation.Destination
import com.example.mygymoffline.ui.components.AppTopAppBar
import com.example.mygymoffline.ui.components.BodyPartCard
import com.example.mygymoffline.ui.components.EmptyState
import com.example.mygymoffline.util.Telemetry

@Composable
fun MainScreen(
    navController: NavController,
    repository: ExerciseRepository,
    onSettingsClick: () -> Unit
) {
    var isGridMode by remember { mutableStateOf(true) }
    var showFavoritesOnly by remember { mutableStateOf(false) }

    val initializationState by repository.initializationState.collectAsStateWithLifecycle()
    val categories by repository.getAllCategories().collectAsStateWithLifecycle(initialValue = emptyList())
    val categoryCounts by repository.getCategoryCounts().collectAsStateWithLifecycle(initialValue = emptyList())
    val countsByCategory = categoryCounts.associate { it.category to it.count }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            AppTopAppBar(
                title = stringResource(R.string.app_name),
                onSettingsClick = onSettingsClick,
                onGridListToggleClick = { isGridMode = !isGridMode },
                isGridMode = isGridMode,
                onFavoritesToggleClick = { showFavoritesOnly = !showFavoritesOnly },
                showFavoritesOnly = showFavoritesOnly
            )

            if (initializationState == ExerciseRepository.InitializationState.Loading) {
                EmptyState(message = "Loading exercise library...")
            } else if (initializationState == ExerciseRepository.InitializationState.Failed) {
                EmptyState(message = "Unable to load the exercise library")
            } else if (categories.isEmpty()) {
                EmptyState(message = "No categories found")
            } else if (isGridMode) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                ) {
                    items(categories) { category ->
                        BodyPartCard(
                            name = category.capitalize(),
                            exerciseCount = countsByCategory[category] ?: 0,
                            thumbnailPath = null,
                            onClick = {
                                Telemetry.trackScreenView("ExerciseList:$category")
                                navController.navigate(Destination.ExerciseList(category))
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                ) {
                    items(categories) { category ->
                        BodyPartCard(
                            name = category.capitalize(),
                            exerciseCount = countsByCategory[category] ?: 0,
                            thumbnailPath = null,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                Telemetry.trackScreenView("ExerciseList:$category")
                                navController.navigate(Destination.ExerciseList(category))
                            }
                        )
                    }
                }
            }
        }
    }
}
