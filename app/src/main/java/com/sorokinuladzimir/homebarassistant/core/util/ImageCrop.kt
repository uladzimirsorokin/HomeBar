package com.sorokinuladzimir.homebarassistant.core.util

import android.graphics.Bitmap
import kotlin.math.roundToInt

private const val BOX_SCALE = 1000f

/**
 * Converts a bounding box in a 0-1000 scale (as returned by the vision model) into pixel
 * coordinates `[x, y, width, height]`, clamped to the image. Returns null when the box is
 * missing or too small to be a real drink picture.
 */
fun imageBoxToPixels(
    box: List<Int>?,
    width: Int,
    height: Int,
    minFraction: Float = 0.05f
): IntArray? {
    if (box == null || box.size < 4 || width <= 0 || height <= 0) return null
    val x1 = (box[0] / BOX_SCALE * width).roundToInt().coerceIn(0, width - 1)
    val y1 = (box[1] / BOX_SCALE * height).roundToInt().coerceIn(0, height - 1)
    val x2 = (box[2] / BOX_SCALE * width).roundToInt().coerceIn(x1 + 1, width)
    val y2 = (box[3] / BOX_SCALE * height).roundToInt().coerceIn(y1 + 1, height)
    val w = x2 - x1
    val h = y2 - y1
    val minWidth = (width * minFraction).roundToInt().coerceAtLeast(1)
    val minHeight = (height * minFraction).roundToInt().coerceAtLeast(1)
    if (w < minWidth || h < minHeight) return null
    return intArrayOf(x1, y1, w, h)
}

/**
 * Crops the drink picture out of a menu photo using a 0-1000 bounding box.
 * Returns null when there is no usable box.
 */
fun Bitmap.cropToImageBox(box: List<Int>?): Bitmap? {
    val rect = imageBoxToPixels(box, width, height) ?: return null
    return runCatching {
        Bitmap.createBitmap(this, rect[0], rect[1], rect[2], rect[3])
    }.getOrNull()
}
