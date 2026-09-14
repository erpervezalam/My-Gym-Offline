package com.example.mygymoffline.ui.media

import android.graphics.drawable.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/** Renders exercise media and honors the user's animation preference. */
@Composable
fun ExerciseGif(
    model: Any,
    contentDescription: String,
    autoPlay: Boolean,
    modifier: Modifier = Modifier,
    fallbackModel: Any? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    var modelToLoad by remember(model, fallbackModel) { mutableStateOf(model) }
    // Recreate the image request when playback changes so a stopped GIF is restarted reliably.
    key(modelToLoad, autoPlay) {
        AsyncImage(
            model = modelToLoad,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            onSuccess = { result ->
                (result.result.drawable as? Animatable)?.let { drawable ->
                    if (autoPlay) drawable.start() else drawable.stop()
                }
            },
            onError = {
                if (fallbackModel != null && modelToLoad != fallbackModel) {
                    modelToLoad = fallbackModel
                }
            }
        )
    }
}
