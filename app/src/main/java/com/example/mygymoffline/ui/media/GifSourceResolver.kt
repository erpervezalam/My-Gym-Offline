package com.example.mygymoffline.ui.media

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class GifOverrideValidation(
    val overrides: Map<String, Uri>,
    val candidateGifCount: Int,
    val ignoredGifCount: Int,
    val directoryReadable: Boolean
) {
    val validOverrideCount: Int
        get() = overrides.size

    companion object {
        fun unavailable() = GifOverrideValidation(
            overrides = emptyMap(),
            candidateGifCount = 0,
            ignoredGifCount = 0,
            directoryReadable = false
        )
    }
}

/** Resolves a GIF to a user-selected override when present, otherwise to its bundled asset. */
class GifSourceResolver(private val overrides: Map<String, Uri>) {

    fun modelFor(gifPath: String): Uri {
        return overrides[assetFileName(gifPath)] ?: bundledModelFor(gifPath)
    }

    fun bundledModelFor(gifPath: String): Uri = bundledAssetUri(gifPath)

    companion object {
        private const val BUNDLED_VIDEO_DIRECTORY = "videos"

        /**
         * Lists a selected folder off the UI thread and accepts only GIFs whose names match bundled
         * exercise media. This bounds the override library to known application assets.
         */
        suspend fun validateOverrides(
            context: Context,
            customDirectoryUri: String?
        ): GifOverrideValidation =
            withContext(Dispatchers.IO) {
                runCatching {
                    val directory = customDirectoryUri
                        ?.let(Uri::parse)
                        ?.let { uri -> DocumentFile.fromTreeUri(context.applicationContext, uri) }
                        ?: return@runCatching GifOverrideValidation.unavailable()

                    val bundledNames = context.applicationContext.assets
                        .list(BUNDLED_VIDEO_DIRECTORY)
                        .orEmpty()
                        .filter { it.endsWith(".gif", ignoreCase = true) }
                        .toSet()
                    val candidateFiles = directory.listFiles()
                        .filter { it.isFile && it.name?.endsWith(".gif", ignoreCase = true) == true }
                    val candidateNames = candidateFiles.mapNotNull(DocumentFile::getName)
                    val validNames = validOverrideFileNames(candidateNames, bundledNames)
                    val canonicalNamesByNormalizedName = validNames.associateBy(::normalizedFileName)
                    val overrides = candidateFiles.mapNotNull { file ->
                        file.name?.let(::normalizedFileName)
                            ?.let(canonicalNamesByNormalizedName::get)
                            ?.let { bundledName -> bundledName to file.uri }
                    }.toMap()

                    GifOverrideValidation(
                        overrides = overrides,
                        candidateGifCount = candidateNames.size,
                        ignoredGifCount = candidateNames.size - overrides.size,
                        directoryReadable = true
                    )
                }.getOrElse { GifOverrideValidation.unavailable() }
            }

        /** Lists a selected folder once off the UI thread and indexes validated GIF overrides. */
        suspend fun loadOverrides(context: Context, customDirectoryUri: String?): Map<String, Uri> =
            validateOverrides(context, customDirectoryUri).overrides

        /**
         * Returns the canonical bundled names that a user-provided library is allowed to replace.
         * Paths, non-GIFs, and unknown names are deliberately rejected.
         */
        internal fun validOverrideFileNames(
            candidateNames: Collection<String>,
            bundledNames: Set<String>
        ): Set<String> {
            val bundledNamesByNormalizedName = bundledNames
                .filter(::isSimpleGifFileName)
                .associateBy(::normalizedFileName)
            return candidateNames.mapNotNull { candidate ->
                candidate.takeIf(::isSimpleGifFileName)
                    ?.let(::normalizedFileName)
                    ?.let(bundledNamesByNormalizedName::get)
            }.toSet()
        }

        fun assetFileName(gifPath: String): String = gifPath.substringAfterLast('/')

        fun bundledAssetUri(gifPath: String): Uri =
            Uri.parse("file:///android_asset/$gifPath")

        private fun isSimpleGifFileName(name: String): Boolean =
            name.isNotBlank() &&
                name.endsWith(".gif", ignoreCase = true) &&
                name == name.substringAfterLast('/') &&
                name == name.substringAfterLast('\\')

        private fun normalizedFileName(name: String): String = name.lowercase(Locale.ROOT)
    }
}
