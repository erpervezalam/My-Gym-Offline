package com.example.mygymoffline.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.data.prefs.SettingsDataStore
import com.example.mygymoffline.ui.detail.ExerciseDetailBottomSheet
import com.example.mygymoffline.ui.detail.FullscreenGifScreen
import com.example.mygymoffline.ui.exercise.ExerciseListScreen
import com.example.mygymoffline.ui.main.MainScreen
import com.example.mygymoffline.ui.settings.SettingsScreen

sealed interface Destination {
    data object Main : Destination
    data class ExerciseList(val category: String) : Destination
    data class ExerciseDetail(val exerciseId: String) : Destination
    data class FullscreenGif(val exerciseId: String) : Destination
    data object Settings : Destination
}

class NavController {
    private val _currentDestination = mutableStateOf<Destination>(Destination.Main)
    val currentDestination: Destination
        @Composable get() = _currentDestination.value

    fun navigate(destination: Destination) {
        _currentDestination.value = destination
    }

    fun popBackStack() {
        _currentDestination.value = Destination.Main
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

    when (destination) {
        is Destination.Main -> {
            MainScreen(
                navController = navController,
                repository = repository,
                onSettingsClick = { navController.navigate(Destination.Settings) }
            )
        }
        is Destination.ExerciseList -> {
            ExerciseListScreen(
                navController = navController,
                repository = repository,
                category = destination.category,
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