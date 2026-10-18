package com.sorokinuladzimir.homebarassistant.core.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageCropTest {

    @Test
    fun `converts 0-1000 box to pixels`() {
        val rect = imageBoxToPixels(listOf(62, 120, 367, 505), width = 800, height = 1000)

        assertArrayEquals(intArrayOf(50, 120, 244, 385), rect)
    }

    @Test
    fun `null or short box yields null`() {
        assertNull(imageBoxToPixels(null, 800, 1000))
        assertNull(imageBoxToPixels(listOf(1, 2, 3), 800, 1000))
    }

    @Test
    fun `too small box yields null`() {
        assertNull(imageBoxToPixels(listOf(0, 0, 10, 10), width = 800, height = 1000))
    }

    @Test
    fun `box is clamped to image bounds`() {
        val rect = imageBoxToPixels(listOf(-50, -50, 1200, 1200), width = 800, height = 1000)

        assertArrayEquals(intArrayOf(0, 0, 800, 1000), rect)
    }
}
