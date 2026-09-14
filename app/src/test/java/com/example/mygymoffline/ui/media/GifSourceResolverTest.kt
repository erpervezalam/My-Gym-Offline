package com.example.mygymoffline.ui.media

import org.junit.Assert.assertEquals
import org.junit.Test

class GifSourceResolverTest {

    @Test
    fun assetFileName_usesTheLastPathSegment() {
        assertEquals(
            "0001-2gPfomN.gif",
            GifSourceResolver.assetFileName("videos/0001-2gPfomN.gif")
        )
    }

    @Test
    fun assetFileName_doesNotDependOnTheFolderName() {
        assertEquals(
            "replacement.gif",
            GifSourceResolver.assetFileName("high-resolution/replacement.gif")
        )
    }

    @Test
    fun validOverrideFileNames_acceptsOnlyMatchingBundledGifNames() {
        val valid = GifSourceResolver.validOverrideFileNames(
            candidateNames = listOf(
                "0001-2gPfomN.gif",
                "0002-Hy9D21L.GIF",
                "not-in-library.gif",
                "preview.jpg"
            ),
            bundledNames = setOf("0001-2gPfomN.gif", "0002-Hy9D21L.gif")
        )

        assertEquals(setOf("0001-2gPfomN.gif", "0002-Hy9D21L.gif"), valid)
    }

    @Test
    fun validOverrideFileNames_rejectsPathsAndUnsafeNames() {
        val valid = GifSourceResolver.validOverrideFileNames(
            candidateNames = listOf("../0001-2gPfomN.gif", "folder/0001-2gPfomN.gif", ""),
            bundledNames = setOf("0001-2gPfomN.gif")
        )

        assertEquals(emptySet<String>(), valid)
    }
}
