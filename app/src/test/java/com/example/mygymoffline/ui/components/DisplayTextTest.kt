package com.example.mygymoffline.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayTextTest {
    @Test
    fun toTitleCase_normalizesDataSourcedNames() {
        assertEquals("Lower Arms", "lower arms".toTitleCase())
        assertEquals("Cable Side Bend", "  CABLE   SIDE bend ".toTitleCase())
    }

    @Test
    fun toTitleCase_handlesBlankValues() {
        assertEquals("", "   ".toTitleCase())
    }
}
