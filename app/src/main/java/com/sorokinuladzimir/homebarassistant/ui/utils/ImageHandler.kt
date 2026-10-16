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

    private fun isExternalStorageWritable(): Boolean =
        Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    private fun getAlbumStorageDir(): File {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Constants.Strings.ALBUM_NAME
        )
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created")
        }
        return file
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = IMG_FILE_PREFIX + timeStamp + "_"
        val albumF = getAlbumStorageDir()
        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF)
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
    fun saveImage(bitmap: Bitmap?): String? {
        if (isExternalStorageWritable()) {
            try {
                val imageFile = createImageFile()
                val fos = FileOutputStream(imageFile)
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                fos.close()
                return imageFile.absolutePath
            } catch (e: FileNotFoundException) {
                Log.d(TAG, "File not found: ${e.message}")
            } catch (e: IOException) {
                Log.d(TAG, "Error accessing file: ${e.message}")
            }
        }
        return null
    }

    fun createImageFile(context: Context): Uri? {
        val photoFile = try {
            createImageFile()
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
