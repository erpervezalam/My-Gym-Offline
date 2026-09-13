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
}
