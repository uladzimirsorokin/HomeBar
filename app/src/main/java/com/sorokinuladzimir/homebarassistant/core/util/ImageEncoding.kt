package com.sorokinuladzimir.homebarassistant.core.util

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Encodes a bitmap as a base64 JPEG (no MIME prefix), downscaling to [maxDimension].
 */
fun Bitmap.toBase64Jpeg(maxDimension: Int = 1280, quality: Int = 85): String {
    val longest = max(width, height)
    val scaled = if (longest > maxDimension) {
        val scale = maxDimension.toFloat() / longest
        Bitmap.createScaledBitmap(
            this,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true
        )
    } else {
        this
    }
    val output = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
    return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
}
