package br.com.sistemasthexample.sistemasth;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

public final class Helpers {

    public static Bitmap getBitMapFromUri(Uri imageUri, ContentResolver cr) {
        try {

            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(cr, imageUri));
            }
            else {
                bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri);
            }
            return getResizedBitmap(bitmap, (int)(bitmap.getWidth() * 0.5), (int)(bitmap.getHeight() * 0.5));

        } catch (Exception ex) {

        }
        return null;

    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

}
