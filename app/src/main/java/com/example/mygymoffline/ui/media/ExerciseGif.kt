package com.example.mygymoffline.ui.media

import android.graphics.drawable.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
    contentScale: ContentScale = ContentScale.Crop
) {
    // Recreate the image request when playback changes so a stopped GIF is restarted reliably.
    key(model, autoPlay) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            onSuccess = { result ->
                (result.result.drawable as? Animatable)?.let { drawable ->
                    if (autoPlay) drawable.start() else drawable.stop()
                }
            }
        )
    }
}
