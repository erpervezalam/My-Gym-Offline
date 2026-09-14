package com.example.mygymoffline.ui.media

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mygymoffline.data.loader.ExerciseJsonLoader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BundledGifLibraryInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun bundledGifLibrary_coversEveryExerciseMediaReference() = runBlocking {
        val bundledNames = context.assets.list("videos").orEmpty().toSet()
        val exercises = ExerciseJsonLoader(context).loadExercises().getOrThrow()
        val referencedNames = exercises.map { GifSourceResolver.assetFileName(it.gifPath) }.toSet()

        assertTrue("The bundled GIF library must not be empty", bundledNames.isNotEmpty())
        assertEquals(emptySet<String>(), referencedNames - bundledNames)
        assertEquals(referencedNames, GifSourceResolver.validOverrideFileNames(referencedNames, bundledNames))
    }
}
