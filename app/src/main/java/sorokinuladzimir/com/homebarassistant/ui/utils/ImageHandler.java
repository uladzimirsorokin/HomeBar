package sorokinuladzimir.com.homebarassistant.ui.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


public final class ImageHandler {

    private final static String TAG = "ImageHandler";

    private static final String IMG_FILE_PREFIX = "IMG_";

    private static final String JPEG_FILE_SUFFIX = ".jpg";


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    public File createImageFile(String albumName) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = IMG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumStorageDir(albumName);
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }


    public Bitmap getBitmapFromUri(Context context, Uri uri, int imageSize) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor,null,bitmapOptions);
        int photoW = bitmapOptions.outWidth;
        int photoH = bitmapOptions.outHeight;

        int scaleFactor = Math.min(photoW/imageSize, photoH/imageSize);
        bitmapOptions.inJustDecodeBounds = false;
        bitmapOptions.inSampleSize = scaleFactor;
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor,null,bitmapOptions);

        parcelFileDescriptor.close();
        return image;
    }

    //Save in PNG
    public String saveImage(Bitmap bitmap, String albumName) throws IOException {

        if(isExternalStorageWritable()){

            File imageFile = createImageFile(albumName);

            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
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

    public String copyImage(Context context, Uri imgUri, String albumName) throws IOException {
        if(isExternalStorageWritable()){
            final int chunkSize = 1024;  // We'll read in one kB at a time
            byte[] imageData = new byte[chunkSize];

            File imageFile = createImageFile(albumName);

            try {
                InputStream in = context.getContentResolver().openInputStream(imgUri);
                OutputStream out = new FileOutputStream(imageFile);

                int bytesRead;
                while ((bytesRead = in.read(imageData)) > 0) {
                    out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                }

                in.close();
                out.close();

                return imageFile.getAbsolutePath();

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        return null;
    }

    public void deleteImage(Context context, String path, String authority){
        File photoFile = new File(path);
        Uri photoUri = FileProvider.getUriForFile(context,
                authority,
                photoFile);
        context.getContentResolver().delete(photoUri, null, null);
    }

}
