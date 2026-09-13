package com.example.mygymoffline.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.data.prefs.SettingsDataStore
import com.example.mygymoffline.ui.detail.ExerciseDetailBottomSheet
import com.example.mygymoffline.ui.detail.FullscreenGifScreen
import com.example.mygymoffline.ui.exercise.ExerciseListScreen
import com.example.mygymoffline.ui.main.MainScreen
import com.example.mygymoffline.ui.media.GifSourceResolver
import com.example.mygymoffline.ui.settings.SettingsScreen

sealed interface Destination {
    data object Main : Destination
    data class ExerciseList(val category: String) : Destination
    data class ExerciseDetail(val exerciseId: String) : Destination
    data class FullscreenGif(val exerciseId: String) : Destination
    data object Settings : Destination
}

class NavController {
    private val backStack = mutableStateListOf<Destination>(Destination.Main)

    val currentDestination: Destination
        get() = backStack.last()

    fun navigate(destination: Destination) {
        if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
    }

    fun popBackStack(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}

@Composable
fun rememberNavController(): NavController = remember { NavController() }

@Composable
fun AppNavHost(
    navController: NavController,
    repository: ExerciseRepository,
    settings: SettingsDataStore
) {
    val destination = navController.currentDestination
    val customGifDirectoryUri by settings.customGifDirectoryUriFlow
        .collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current
    val gifOverrides by produceState<Map<String, android.net.Uri>>(
        initialValue = emptyMap(),
        key1 = customGifDirectoryUri
    ) {
        value = GifSourceResolver.loadOverrides(context, customGifDirectoryUri)
    }
    val gifSourceResolver = remember(gifOverrides) {
        GifSourceResolver(gifOverrides)
    }

    BackHandler(enabled = destination != Destination.Main) {
        navController.popBackStack()
    }

    when (destination) {
        is Destination.Main -> {
                MainScreen(
                    navController = navController,
                    repository = repository,
                    settings = settings,
                    onSettingsClick = { navController.navigate(Destination.Settings) }
            )
        }
        is Destination.ExerciseList -> {
            ExerciseListScreen(
                navController = navController,
                repository = repository,
                category = destination.category,
                gifSourceResolver = gifSourceResolver,
                onBackClick = { navController.popBackStack() }
            )
        }
        is Destination.ExerciseDetail -> {
            var exercise by remember { mutableStateOf<com.example.mygymoffline.data.db.Exercise?>(null) }
            LaunchedEffect(destination.exerciseId) {
                exercise = repository.getExerciseById(destination.exerciseId)
            }
            exercise?.let { ex ->
                ExerciseDetailBottomSheet(
                    exercise = ex,
                    repository = repository,
                    selectedLanguage = "en",
                    gifSourceResolver = gifSourceResolver,
                    onGifClick = { navController.navigate(Destination.FullscreenGif(ex.id)) },
                    onCloseClick = { navController.popBackStack() }
                )
            }
        }
        is Destination.FullscreenGif -> {
            var exercise by remember { mutableStateOf<com.example.mygymoffline.data.db.Exercise?>(null) }
            LaunchedEffect(destination.exerciseId) {
                exercise = repository.getExerciseById(destination.exerciseId)
            }
            exercise?.let { ex ->
                FullscreenGifScreen(
                exercise = ex,
                repository = repository,
                gifSourceResolver = gifSourceResolver,
                onCloseClick = { navController.popBackStack() }
                )
            }
        }
        is Destination.Settings -> {
            SettingsScreen(
                settings = settings,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
