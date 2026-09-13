package com.example.mygymoffline.ui.components

import java.util.Locale

/** Formats data-sourced exercise and category names consistently for display. */
fun String.toTitleCase(): String =
    trim().split(Regex("\\s+")).joinToString(" ") { word ->
        word.lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
    }
