package com.example.mygymoffline.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mygymoffline.R
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.data.prefs.SettingsDataStore
import com.example.mygymoffline.navigation.NavController
import com.example.mygymoffline.navigation.Destination
import com.example.mygymoffline.ui.components.AppTopAppBar
import com.example.mygymoffline.ui.components.BodyPartCard
import com.example.mygymoffline.ui.components.EmptyState
import com.example.mygymoffline.util.Telemetry
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun MainScreen(
    navController: NavController,
    repository: ExerciseRepository,
    settings: SettingsDataStore,
    onSettingsClick: () -> Unit
) {
    val initializationState by repository.initializationState.collectAsStateWithLifecycle()
    val categories by repository.getAllCategories().collectAsStateWithLifecycle(initialValue = emptyList())
    val categoryCounts by repository.getCategoryCounts().collectAsStateWithLifecycle(initialValue = emptyList())
    val categoryPreviews by repository.getCategoryPreviews().collectAsStateWithLifecycle(initialValue = emptyList())
    val isGridMode by settings.gridModeFlow.collectAsStateWithLifecycle(initialValue = SettingsDataStore.DEFAULT_GRID_MODE)
    val showFavoritesOnly by settings.favoritesOnlyFlow.collectAsStateWithLifecycle(
        initialValue = SettingsDataStore.DEFAULT_FAVORITES_ONLY
    )
    val countsByCategory = categoryCounts.associate { it.category to it.count }
    val previewsByCategory = categoryPreviews.associate { it.category to it.gifPath }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            AppTopAppBar(
                title = stringResource(R.string.app_name),
                onSettingsClick = onSettingsClick,
                onGridListToggleClick = {
                    coroutineScope.launch { settings.setGridMode(!isGridMode) }
                },
                isGridMode = isGridMode,
                onFavoritesToggleClick = {
                    coroutineScope.launch { settings.setFavoritesOnly(!showFavoritesOnly) }
                },
                showFavoritesOnly = showFavoritesOnly
            )
        }
    ) { contentPadding ->
        when {
            initializationState == ExerciseRepository.InitializationState.Loading -> {
                EmptyState(
                    message = "Loading exercise library...",
                    modifier = Modifier.fillMaxSize().padding(contentPadding)
                )
            }
            initializationState == ExerciseRepository.InitializationState.Failed -> {
                EmptyState(
                    message = "Unable to load the exercise library",
                    modifier = Modifier.fillMaxSize().padding(contentPadding)
                )
            }
            categories.isEmpty() -> {
                EmptyState(
                    message = "No categories found",
                    modifier = Modifier.fillMaxSize().padding(contentPadding)
                )
            }
            isGridMode -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .testTag("category-grid")
                ) {
                    items(categories) { category ->
                        BodyPartCard(
                            name = category.replaceFirstChar { it.uppercase() },
                            exerciseCount = countsByCategory[category] ?: 0,
                            thumbnailPath = previewsByCategory[category],
                            onClick = {
                                Telemetry.trackScreenView("ExerciseList:$category")
                                navController.navigate(Destination.ExerciseList(category))
                            }
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .testTag("category-list")
                ) {
                    items(categories) { category ->
                        BodyPartCard(
                            name = category.replaceFirstChar { it.uppercase() },
                            exerciseCount = countsByCategory[category] ?: 0,
                            thumbnailPath = previewsByCategory[category],
                            compactList = true,
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
