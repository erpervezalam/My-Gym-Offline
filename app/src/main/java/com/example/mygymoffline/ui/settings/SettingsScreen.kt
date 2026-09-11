package com.example.mygymoffline.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygymoffline.R
import com.example.mygymoffline.data.prefs.SettingsDataStore
import com.example.mygymoffline.util.AppLogger
import com.example.mygymoffline.util.Telemetry
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settings: SettingsDataStore,
    onBackClick: () -> Unit
) {
    val language by settings.languageFlow.collectAsStateWithLifecycle(initialValue = "en")
    val enabledEquipment by settings.enabledEquipmentFlow.collectAsStateWithLifecycle()
    val gridMode by settings.gridModeFlow.collectAsStateWithLifecycle(initialValue = true)
    val autoPlayGif by settings.autoPlayGifFlow.collectAsStateWithLifecycle(initialValue = true)
    val gifDownloadComplete by settings.gifDownloadCompleteFlow.collectAsStateWithLifecycle(initialValue = false)
    val gifCacheSize by settings.gifCacheSizeFlow.collectAsStateWithLifecycle(initialValue = 0L)

    var expandedEquipment by remember { mutableStateOf(false) }
    var expandedDebug by remember { mutableStateOf(false) }

    val languages = listOf(
        "en" to "English",
        "es" to "Español",
        "it" to "Italiano",
        "tr" to "Türkçe",
        "ru" to "Русский",
        "zh" to "中文",
        "hi" to "हिन्दी",
        "pl" to "Polski",
        "ko" to "한국어",
        "fr" to "Français"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text(text = stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }

        // Language Section
        item {
            SettingsSection(title = "Language") {
                ExposedDropdownMenuBox(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = false, // Would need state for full dropdown
                    onExpandedChange = {}
                ) {
                    androidx.compose.material3.TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = languages.find { it.first == language }?.second ?: "English",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Language") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand") },
                        singleLine = true,
                        colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )
                }
            }
        }

        // Language options
        item {
            SettingsSection(title = "Available Languages") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    languages.forEach { (code, name) ->
                        ListItem(
                            modifier = Modifier.fillMaxWidth(),
                            headlineContent = { Text(name) },
                            leadingContent = {
                                androidx.compose.material3.Checkbox(
                                    checked = code == language,
                                    onCheckedChange = { if (it) settings.setLanguage(code) }
                                )
                            }
                        )
                    }
                }
            }
        }

        // Equipment Filter Section
        item {
            SettingsSection(title = "Equipment Filter", subtitle = "Select equipment types to show") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("Show ${enabledEquipment.size} of ${getDefaultEquipmentSet().size} equipment types")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Select All", fontSize = 14.sp)
                                .let { androidx.compose.material3.TextButton(onClick = { settings.setEnabledEquipment(getDefaultEquipmentSet()) }) { it } }
                            Text("Clear All", fontSize = 14.sp)
                                .let { androidx.compose.material3.TextButton(onClick = { settings.setEnabledEquipment(emptySet()) }) { it } }
                        }
                    }
                    if (expandedEquipment) {
                        getDefaultEquipmentSet().sorted().forEach { equipment ->
                            ListItem(
                                modifier = Modifier.fillMaxWidth(),
                                headlineContent = { Text(equipment.capitalize()) },
                                leadingContent = {
                                    Checkbox(
                                        checked = enabledEquipment.contains(equipment),
                                        onCheckedChange = { checked ->
                                            val newSet = enabledEquipment.toMutableSet()
                                            if (checked) newSet.add(equipment) else newSet.remove(equipment)
                                            settings.setEnabledEquipment(newSet)
                                        }
                                    )
                                }
                            )
                        }
                    }
                    androidx.compose.material3.TextButton(onClick = { expandedEquipment = !expandedEquipment }) {
                        Text(if (expandedEquipment) "Show Less" else "Show All Equipment")
                    }
                }
            }
        }

        // Display Section
        item {
            SettingsSection(title = "Display") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsToggle(
                        title = "Grid View",
                        subtitle = "Show body parts as grid (off = list)",
                        checked = gridMode,
                        onCheckedChange = { settings.setGridMode(it) }
                    )
                    SettingsToggle(
                        title = "Auto-play GIFs",
                        subtitle = "Automatically play animated GIFs in lists",
                        checked = autoPlayGif,
                        onCheckedChange = { settings.setAutoPlayGif(it) }
                    )
                }
            }
        }

        // GIF Management Section
        item {
            SettingsSection(title = "GIF Management") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsRow(
                        title = "GIF Cache Status",
                        subtitle = if (gifDownloadComplete) "All GIFs downloaded" else "GIFs load on demand",
                        trailing = {
                            if (!gifDownloadComplete) {
                                androidx.compose.material3.Button(
                                    onClick = {
                                        // Trigger GIF download
                                        Telemetry.trackEvent("gif_download_start", "Settings", emptyMap())
                                    }
                                ) {
                                    Text("Download All GIFs")
                                }
                            } else {
                                Text("Complete", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    SettingsRow(
                        title = "Cache Size",
                        subtitle = formatFileSize(gifCacheSize),
                        trailing = {
                            androidx.compose.material3.Button(onClick = {
                                settings.setGifCacheSize(0)
                                Telemetry.trackEvent("gif_cache_clear", "Settings", emptyMap())
                            }) {
                                Text("Clear Cache")
                            }
                        }
                    )
                }
            }
        }

        // Debug Section
        item {
            SettingsSection(title = "Debug", subtitle = "Developer options") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (expandedDebug) {
                        SettingsRow(
                            title = "Share Logs",
                            subtitle = "Email app logs for debugging",
                            trailing = {
                                androidx.compose.material3.Button(onClick = {
                                    shareLogs()
                                }) {
                                    Text("Share")
                                }
                            }
                        )
                        SettingsRow(
                            title = "Reset Database",
                            subtitle = "Re-import all exercises from JSON",
                            trailing = {
                                androidx.compose.material3.Button(onClick = {
                                    resetDatabase()
                                }) {
                                    Text("Reset")
                                }
                            }
                        )
                        SettingsRow(
                            title = "Export Telemetry",
                            subtitle = "Share telemetry data",
                            trailing = {
                                androidx.compose.material3.Button(onClick = {
                                    exportTelemetry()
                                }) {
                                    Text("Export")
                                }
                            }
                        )
                    }
                    androidx.compose.material3.TextButton(onClick = { expandedDebug = !expandedDebug }) {
                        Text(if (expandedDebug) "Hide Debug Options" else "Show Debug Options")
                    }
                }
            }
        }

        // About Section
        item {
            SettingsSection(title = "About") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsRow(title = "Version", subtitle = "1.0.0")
                    SettingsRow(title = "Dataset", subtitle = "1,324 exercises from Gym Visual")
                    SettingsRow(title = "License", subtitle = "MIT + Gym Visual media terms")
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            subtitle?.let { Text(text = it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 16.sp)
            subtitle?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        trailing()
    }
}

@Composable
fun SettingsToggle(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 16.sp)
            subtitle?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun getDefaultEquipmentSet(): Set<String> {
    return setOf(
        "body weight", "dumbbell", "cable", "barbell", "leverage machine",
        "band", "smith machine", "kettlebell", "weighted", "stability ball",
        "ez barbell", "other"
    )
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
    }
}

private fun shareLogs() {
    // Implementation would use Intent.ACTION_SEND with log files
    Telemetry.trackEvent("share_logs", "Settings", emptyMap())
    AppLogger.i("Settings", "Share logs requested")
}

private fun resetDatabase() {
    // Implementation would clear Room database and re-trigger initialization
    Telemetry.trackEvent("reset_database", "Settings", emptyMap())
    AppLogger.i("Settings", "Database reset requested")
}

private fun exportTelemetry() {
    // Implementation would share telemetry JSON files
    Telemetry.trackEvent("export_telemetry", "Settings", emptyMap())
    AppLogger.i("Settings", "Telemetry export requested")
}