package com.example.mygymoffline.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygymoffline.R
import com.example.mygymoffline.data.prefs.SettingsDataStore
import com.example.mygymoffline.util.AppLogger
import com.example.mygymoffline.util.Telemetry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: SettingsDataStore,
    onBackClick: () -> Unit
) {
    val language by settings.languageFlow.collectAsStateWithLifecycle(initialValue = "en")
    val enabledEquipment by settings.enabledEquipmentFlow.collectAsStateWithLifecycle(initialValue = emptySet())
    val gridMode by settings.gridModeFlow.collectAsStateWithLifecycle(initialValue = true)
    val autoPlayGif by settings.autoPlayGifFlow.collectAsStateWithLifecycle(initialValue = true)
    val darkMode by settings.darkModeFlow.collectAsStateWithLifecycle(initialValue = false)
    val customGifDirectoryUri by settings.customGifDirectoryUriFlow.collectAsStateWithLifecycle(initialValue = null)

    var expandedLanguage by remember { mutableStateOf(false) }
    var expandedDebug by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val gifDirectoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                customGifDirectoryUri
                    ?.takeIf { it != uri.toString() }
                    ?.let { previousUri ->
                        runCatching {
                            context.contentResolver.releasePersistableUriPermission(
                                Uri.parse(previousUri),
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        }
                    }
                coroutineScope.launch { settings.setCustomGifDirectoryUri(uri.toString()) }
            } catch (error: SecurityException) {
                AppLogger.w("Settings", "Unable to retain GIF folder access")
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text(text = stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language", modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Box {
                        TextButton(onClick = { expandedLanguage = true }) {
                            Text(SettingsCatalog.languages.find { it.first == language }?.second ?: "English")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose language")
                        }
                        DropdownMenu(expanded = expandedLanguage, onDismissRequest = { expandedLanguage = false }) {
                            SettingsCatalog.languages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        expandedLanguage = false
                                        coroutineScope.launch { settings.setLanguage(code) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(title = "Equipment") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${enabledEquipment.size}/${SettingsDataStore.getDefaultEquipmentSet().size} selected",
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = {
                            coroutineScope.launch { settings.setEnabledEquipment(SettingsDataStore.getDefaultEquipmentSet()) }
                        }) { Text("All", fontSize = 14.sp) }
                        TextButton(onClick = {
                            coroutineScope.launch { settings.setEnabledEquipment(emptySet()) }
                        }) { Text("Clear", fontSize = 14.sp) }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SettingsDataStore.getDefaultEquipmentSet().sorted().forEach { equipment ->
                            FilterChip(
                                modifier = Modifier.height(32.dp),
                                selected = equipment in enabledEquipment,
                                onClick = {
                                    val newSet = enabledEquipment.toMutableSet()
                                    if (equipment in newSet) newSet.remove(equipment) else newSet.add(equipment)
                                    coroutineScope.launch { settings.setEnabledEquipment(newSet) }
                                },
                                label = {
                                    Text(
                                        text = equipment.replaceFirstChar { it.uppercase() },
                                        fontSize = 14.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(title = "Display") {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    SettingsToggle(
                        title = "Dark Mode",
                        checked = darkMode,
                        onCheckedChange = { coroutineScope.launch { settings.setDarkMode(it) } }
                    )
                    SettingsToggle(
                        title = "Grid Layout",
                        checked = gridMode,
                        modifier = Modifier.testTag("grid-mode-toggle"),
                        onCheckedChange = { coroutineScope.launch { settings.setGridMode(it) } }
                    )
                    SettingsToggle(
                        title = "Auto-play GIFs",
                        checked = autoPlayGif,
                        onCheckedChange = { coroutineScope.launch { settings.setAutoPlayGif(it) } }
                    )
                }
            }
        }

        item {
            SettingsSection(title = "GIF Library") {
                SettingsRow(
                    title = "GIF Folder",
                    subtitle = if (customGifDirectoryUri == null) {
                        "Bundled animations"
                    } else {
                        "Custom folder selected"
                    },
                    trailing = {
                        TextButton(onClick = { gifDirectoryPicker.launch(null) }) {
                            Text(if (customGifDirectoryUri == null) "Choose" else "Change")
                        }
                    }
                )
                if (customGifDirectoryUri != null) {
                    TextButton(onClick = {
                        runCatching {
                            context.contentResolver.releasePersistableUriPermission(
                                Uri.parse(customGifDirectoryUri),
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        }
                        coroutineScope.launch { settings.setCustomGifDirectoryUri(null) }
                    }) {
                        Text("Use Bundled GIFs Only")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDebug = !expandedDebug }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Debug", modifier = Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (expandedDebug) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expandedDebug) "Hide debug options" else "Show debug options"
                        )
                    }
                    if (expandedDebug) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                        SettingsRow(
                            title = "Share Logs",
                            subtitle = "Email app logs for debugging",
                            trailing = {
                                Button(onClick = { shareLogs() }) { Text("Share") }
                            }
                        )
                        SettingsRow(
                            title = "Reset Database",
                            subtitle = "Re-import all exercises from JSON",
                            trailing = {
                                Button(onClick = { resetDatabase() }) { Text("Reset") }
                            }
                        )
                        SettingsRow(
                            title = "Export Telemetry",
                            subtitle = "Share telemetry data",
                            trailing = {
                                Button(onClick = { exportTelemetry() }) { Text("Export") }
                            }
                        )
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(title = "About") {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    SettingsRow(title = "Version", trailing = { Text("1.0.0", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                    SettingsRow(title = "Exercise Data", trailing = { Text("Gym Visual", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                    SettingsRow(title = "License", trailing = { Text("MIT", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                }
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp)
            subtitle?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        androidx.compose.material3.Switch(
            modifier = modifier,
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

internal object SettingsCatalog {
    val languages = listOf(
        "en" to "English", "es" to "Español", "it" to "Italiano", "tr" to "Türkçe",
        "ru" to "Русский", "zh" to "中文", "hi" to "हिन्दी", "pl" to "Polski",
        "ko" to "한국어", "fr" to "Français"
    )
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
