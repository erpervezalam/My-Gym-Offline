package com.example.mygymoffline.util

import android.content.Context
import android.os.Environment
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

object AppLogger {
    private const val LOG_DIR_NAME = "MyGymOfflineLogs"
    private const val LOG_FILE_PREFIX = "app_log_"
    private const val CRASH_LOG_FILE = "crash_log.txt"
    private const val MAX_LOG_FILES = 7
    private val executor = Executors.newSingleThreadExecutor()
    private var logFile: File? = null
    private var crashLogFile: File? = null

    fun init(context: Context) {
        Timber.plant(Timber.DebugTree())
        setupLogFiles(context)
        Timber.plant(FileLoggingTree())
    }

    private fun setupLogFiles(context: Context) {
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        val logDir = File(externalDir, LOG_DIR_NAME)
        if (!logDir.exists()) logDir.mkdirs()

        val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        logFile = File(logDir, "$LOG_FILE_PREFIX$date.txt")
        crashLogFile = File(logDir, CRASH_LOG_FILE)

        cleanupOldLogs(logDir)
    }

    private fun cleanupOldLogs(logDir: File) {
        val files = logDir.listFiles { _, name -> name.startsWith(LOG_FILE_PREFIX) && name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() } ?: return
        if (files.size > MAX_LOG_FILES) {
            files.drop(MAX_LOG_FILES).forEach { it.delete() }
        }
    }

    fun d(tag: String, msg: String, vararg args: Any) {
        Timber.tag(tag).d(msg, *args)
    }

    fun i(tag: String, msg: String, vararg args: Any) {
        Timber.tag(tag).i(msg, *args)
    }

    fun w(tag: String, msg: String, vararg args: Any) {
        Timber.tag(tag).w(msg, *args)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, msg)
        } else {
            Timber.tag(tag).e(msg)
        }
    }

    fun crash(tag: String, msg: String, throwable: Throwable) {
        val crashMsg = buildCrashMessage(tag, msg, throwable)
        writeToFile(crashLogFile, crashMsg)
        Timber.tag(tag).e(throwable, msg)
    }

    private fun buildCrashMessage(tag: String, msg: String, throwable: Throwable): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        sb.append("[CRASH] ").append(dateFormat.format(Date())).append("\n")
        sb.append("Tag: ").append(tag).append("\n")
        sb.append("Message: ").append(msg).append("\n")
        sb.append("Exception: ").append(throwable.toString()).append("\n")
        throwable.stackTrace.forEach { sb.append("    at ").append(it.toString()).append("\n") }
        sb.append("\n---\n")
        return sb.toString()
    }

    fun getLogFile(): File? = logFile

    fun getCrashLogFile(): File? = crashLogFile

    fun getAllLogFiles(): List<File> {
        val externalDir = getLogDir()
        return externalDir.listFiles { _, name -> name.endsWith(".txt") }?.toList() ?: emptyList()
    }

    private fun getLogDir(): File {
        return logFile?.parentFile ?: File(Environment.getExternalStorageDirectory(), LOG_DIR_NAME)
    }

    private fun writeToFile(file: File?, content: String) {
        file?.let { f ->
            executor.execute {
                FileWriter(f, true).use { it.write(content) }
            }
        }
    }

    private class FileLoggingTree : Timber.Tree() {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

        override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
            if (priority < Timber.INFO) return
            val logMessage = buildMessage(priority, tag, message, throwable)
            writeToFile(logFile, logMessage)
        }

        private fun buildMessage(priority: Int, tag: String?, message: String, throwable: Throwable?): String {
            val sb = StringBuilder()
            sb.append("[").append(priorityToString(priority)).append("] ")
            sb.append(dateFormat.format(Date())).append(" ")
            sb.append(tag ?: "").append(": ")
            sb.append(message).append("\n")
            throwable?.let {
                sb.append("  ").append(it.stackTraceToString()).append("\n")
            }
            return sb.toString()
        }

        private fun priorityToString(priority: Int): String {
            return when (priority) {
                Timber.VERBOSE -> "VERBOSE"
                Timber.DEBUG -> "DEBUG"
                Timber.INFO -> "INFO"
                Timber.WARN -> "WARN"
                Timber.ERROR -> "ERROR"
                else -> "UNKNOWN"
            }
        }
    }
}