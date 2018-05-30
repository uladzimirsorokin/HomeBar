package com.sorokinuladzimir.homebarassistant.ui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.sorokinuladzimir.homebarassistant.Constants;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static android.support.v4.content.FileProvider.getUriForFile;


public final class ImageHandler {

    private static final String TAG = "ImageHandler";

    private static final String IMG_FILE_PREFIX = "IMG_";

    private static final String JPEG_FILE_SUFFIX = ".jpg";


    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private File getAlbumStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), Constants.Strings.ALBUM_NAME);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = IMG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumStorageDir();
        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
    }


    public Bitmap getBitmapFromUri(Context context, Uri uri, int imageSize) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = Objects.requireNonNull(parcelFileDescriptor).getFileDescriptor();
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bitmapOptions);
        int photoW = bitmapOptions.outWidth;
        int photoH = bitmapOptions.outHeight;

        int scaleFactor = Math.min(photoW / imageSize, photoH / imageSize);
        bitmapOptions.inJustDecodeBounds = false;
        bitmapOptions.inSampleSize = scaleFactor;
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bitmapOptions);

        parcelFileDescriptor.close();
        return image;
    }

    //Save in PNG
    public String saveImage(Bitmap bitmap) throws IOException {

        if (isExternalStorageWritable()) {

            File imageFile = createImageFile();

            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }
                fos.close();
                return imageFile.getAbsolutePath();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }

        return null;
    }

    public Uri createImageFile(Context context) {
        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException ignored) {
            //oh noo...
        }

        if (photoFile != null) {
            return getUriForFile(context,
                    Constants.Strings.AUTHORITY,
                    photoFile);
        }

        return null;
    }

    public void deleteImage(Context context, Uri imageUri) {
        context.getContentResolver().delete(imageUri, null, null);
    }

}
