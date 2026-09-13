package com.example.mygymoffline.ui.media

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Resolves a GIF to a user-selected override when present, otherwise to its bundled asset. */
class GifSourceResolver(private val overrides: Map<String, Uri>) {

    fun modelFor(gifPath: String): Uri {
        return overrides[assetFileName(gifPath)] ?: bundledAssetUri(gifPath)
    }

    companion object {
        /** Lists the selected folder once off the UI thread and indexes valid GIF overrides by filename. */
        suspend fun loadOverrides(context: Context, customDirectoryUri: String?): Map<String, Uri> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val directory = customDirectoryUri
                        ?.let(Uri::parse)
                        ?.let { uri -> DocumentFile.fromTreeUri(context.applicationContext, uri) }
                        ?: return@runCatching emptyMap()

                    directory.listFiles()
                        .asSequence()
                        .filter { it.isFile && it.name?.endsWith(".gif", ignoreCase = true) == true }
                        .mapNotNull { file -> file.name?.let { name -> name to file.uri } }
                        .toMap()
                }.getOrDefault(emptyMap())
            }

        fun assetFileName(gifPath: String): String = gifPath.substringAfterLast('/')

        fun bundledAssetUri(gifPath: String): Uri =
            Uri.parse("file:///android_asset/$gifPath")
    }
}
