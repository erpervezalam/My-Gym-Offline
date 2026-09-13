package com.example.mygymoffline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.example.mygymoffline.data.prefs.SettingsDataStore
import com.example.mygymoffline.data.prefs.ThemeMode
import com.example.mygymoffline.data.repository.ExerciseRepository
import com.example.mygymoffline.navigation.AppNavHost
import com.example.mygymoffline.navigation.rememberNavController
import com.example.mygymoffline.theme.MyGymOfflineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val app = application as MyGymApplication
        setContent {
            val themeMode by app.settingsDataStore.themeModeFlow.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            MyGymOfflineTheme(darkTheme = darkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        repository = app.exerciseRepository,
                        settings = app.settingsDataStore
                    )
                }
            }
        }
    }
}
