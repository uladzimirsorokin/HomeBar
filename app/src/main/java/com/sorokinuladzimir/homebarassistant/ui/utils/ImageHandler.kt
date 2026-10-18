package com.sorokinuladzimir.homebarassistant.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.sorokinuladzimir.homebarassistant.Constants
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageHandler {

    private fun getAlbumStorageDir(context: Context): File {
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            Constants.Strings.ALBUM_NAME
        )
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "Directory not created")
        }
        return dir
    }

    @Throws(IOException::class)
    private fun createTempImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = IMG_FILE_PREFIX + timeStamp + "_"
        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, getAlbumStorageDir(context))
    }

    @Throws(IOException::class)
    fun getBitmapFromUri(context: Context, uri: Uri, imageSize: Int): Bitmap? {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = requireNotNull(parcelFileDescriptor).fileDescriptor
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bitmapOptions)
        val photoW = bitmapOptions.outWidth
        val photoH = bitmapOptions.outHeight

        val scaleFactor = minOf(photoW / imageSize, photoH / imageSize)
        bitmapOptions.inJustDecodeBounds = false
        bitmapOptions.inSampleSize = scaleFactor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bitmapOptions)

        parcelFileDescriptor.close()
        return image
    }

    //Save in PNG
    fun saveImage(context: Context, bitmap: Bitmap?): String? {
        return try {
            val imageFile = createTempImageFile(context)
            FileOutputStream(imageFile).use { fos ->
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
            }
            imageFile.absolutePath
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: ${e.message}")
            null
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: ${e.message}")
            null
        }
    }

    fun createImageFile(context: Context): Uri? {
        val photoFile = try {
            createTempImageFile(context)
        } catch (ignored: IOException) {
            null
        }

        return photoFile?.let {
            FileProvider.getUriForFile(context, Constants.Strings.AUTHORITY, it)
        }
    }

    fun deleteImage(context: Context, imageUri: Uri) {
        context.contentResolver.delete(imageUri, null, null)
    }

    private companion object {
        private const val TAG = "ImageHandler"
        private const val IMG_FILE_PREFIX = "IMG_"
        private const val JPEG_FILE_SUFFIX = ".jpg"
    }
}
