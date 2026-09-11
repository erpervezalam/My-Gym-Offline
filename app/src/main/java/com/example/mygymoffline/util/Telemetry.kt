package com.example.mygymoffline.util

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object Telemetry {
    private const val TELEMETRY_DIR_NAME = "MyGymOfflineTelemetry"
    private const val TELEMETRY_FILE_PREFIX = "telemetry_"
    private val executor = Executors.newSingleThreadExecutor()
    private val eventBuffer = ConcurrentHashMap<String, MutableList<TelemetryEvent>>()
    private var telemetryDir: File? = null
    private var currentSessionId: String = ""
    private var flushJob: kotlinx.coroutines.Job? = null

    data class TelemetryEvent(
        val timestamp: String,
        val eventType: String,
        val screenName: String?,
        val properties: Map<String, Any>
    )

    @JvmStatic
    fun init(context: Context, scope: CoroutineScope) {
        setupTelemetryDir(context)
        currentSessionId = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        startPeriodicFlush(scope)
        trackEvent("app_start", null, mapOf("session_id" to currentSessionId))
    }

    private fun setupTelemetryDir(context: Context) {
        val externalDir = context.getExternalFilesDir(null) ?: context.filesDir
        telemetryDir = File(externalDir, TELEMETRY_DIR_NAME)
        if (!telemetryDir!!.exists()) telemetryDir!!.mkdirs()
    }

    private fun startPeriodicFlush(scope: CoroutineScope) {
        flushJob = scope.launch(Dispatchers.IO) {
            while (true) {
                kotlinx.coroutines.delay(30000) // Flush every 30 seconds
                flush()
            }
        }
    }

    @JvmStatic
    fun trackEvent(eventName: String, screenName: String?, properties: Map<String, Any>) {
        val event = TelemetryEvent(
            timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()),
            eventType = eventName,
            screenName = screenName,
            properties = properties + ("session_id" to currentSessionId)
        )
        eventBuffer.computeIfAbsent(eventName) { mutableListOf() }.add(event)
    }

    @JvmStatic
    fun trackScreenView(screenName: String) {
        trackEvent("screen_view", screenName, mapOf("screen" to screenName))
    }

    @JvmStatic
    fun trackError(errorType: String, message: String, stackTrace: String?) {
        trackEvent("error", null, mapOf(
            "error_type" to errorType,
            "message" to message,
            "stack_trace" to stackTrace ?: ""
        ))
    }

    @JvmStatic
    fun trackCrash(message: String, stackTrace: String) {
        trackEvent("crash", null, mapOf(
            "message" to message,
            "stack_trace" to stackTrace
        ))
        flush() // Immediate flush on crash
    }

    @JvmStatic
    fun flush() {
        executor.execute {
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val file = File(telemetryDir!!, "$TELEMETRY_FILE_PREFIX$date.json")
            val allEvents = eventBuffer.values.flatten().toList()
            if (allEvents.isNotEmpty()) {
                val json = Json { prettyPrint = true }.encodeToString(allEvents)
                FileWriter(file, true).use { it.write(json + "\n") }
                eventBuffer.clear()
            }
        }
    }

    @JvmStatic
    fun getTelemetryFiles(): List<File> {
        return telemetryDir?.listFiles { _, name -> name.startsWith(TELEMETRY_FILE_PREFIX) && name.endsWith(".json") }
            ?.sortedByDescending { it.lastModified() }
            ?.toList() ?: emptyList()
    }

    @JvmStatic
    fun getAllTelemetryEvents(): List<TelemetryEvent> {
        return eventBuffer.values.flatten().toList()
    }

    @JvmStatic
    fun shutdown() {
        flushJob?.cancel()
        flush()
        executor.shutdown()
    }
}