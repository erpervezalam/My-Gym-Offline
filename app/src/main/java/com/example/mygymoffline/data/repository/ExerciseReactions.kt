package com.example.mygymoffline.data.repository

import com.example.mygymoffline.data.db.Exercise

/** Keeps the two mutually exclusive exercise reactions consistent across all UI entry points. */
internal fun Exercise.toggleFavoriteReaction(): Exercise =
    if (isFavorite) {
        copy(isFavorite = false)
    } else {
        copy(isFavorite = true, isDisliked = false)
    }

internal fun Exercise.toggleDislikeReaction(): Exercise =
    if (isDisliked) {
        copy(isDisliked = false)
    } else {
        copy(isDisliked = true, isFavorite = false)
    }
