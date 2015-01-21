package org.coursera.camppotlatch.client.commons;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Fabio Cicerre on 01/11/2014.
 */
public class ImageUtils {
    public static enum FitMode {
        FIT_ALL, FIT_MINOR_SIDE
    }

    public static final String JPEG_MIME_TYPE = "image/jpeg";

    public static final String TEMP_IMAGE_DIR = ".image";

    public static final String NORMAL_IMAGE_SIZE_SUFFIX = "normal";
    public static final String THUMBNAIL_IMAGE_SIZE_SUFFIX = "thumbnail";

    public static final String JPEG_FILE_EXTENSION = ".jpg";

    public static String generateImageId() {
        return UUID.randomUUID().toString();
    }

    public static File getImageDir() throws IOException {
        File appDir = AppContext.getAppDirectory();
        File imageDir = new File(appDir, TEMP_IMAGE_DIR);
        if (!imageDir.exists())
            imageDir.mkdir();

        return imageDir;
    }

    public static File getImageFile(String imageId) throws IOException {
        return getImageFile(imageId, NORMAL_IMAGE_SIZE_SUFFIX);
    }

    public static File getThumbnailImageFile(String imageId) throws IOException {
        return getImageFile(imageId, THUMBNAIL_IMAGE_SIZE_SUFFIX);
    }

    public static File getImageFile(String imageId, String imageSizeSuffix) throws IOException {
        String imageFileName = "jpeg-" + imageId + "-" + imageSizeSuffix + JPEG_FILE_EXTENSION;

        File imageDir = getImageDir();

        /*
        File imageFile = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                imageDir        // directory
        );
        */

        File imageFile = new File(imageDir, imageFileName);

        return imageFile;
    }

    public static File createImageFile(String imageId) throws IOException {
        return createImageFile(imageId, NORMAL_IMAGE_SIZE_SUFFIX);
    }

    public static File createThumbnailImageFile(String imageId) throws IOException {
        return createImageFile(imageId, THUMBNAIL_IMAGE_SIZE_SUFFIX);
    }

    public static File createImageFile(String imageId, String imageSizeSuffix) throws IOException {
        File imageFile = getImageFile(imageId, imageSizeSuffix);

        if (imageFile.exists()) {
            if (!imageFile.delete())
                throw new IOException("The image file of id " + imageId + " couldn't be deleted");
        }

        if (!imageFile.createNewFile())
            throw new IOException("The image file of id " + imageId + " couldn't be created");

        return imageFile;
    }

    /*
    public static File createImageFile() throws IOException {
        // Create an image file name
        String uuid = UUID.randomUUID().toString();
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "jpeg-" + uuid;
        //File storageDir = Environment.getExternalStoragePublicDirectory(
        //        Environment.DIRECTORY_PICTURES);
        File imageDir = getImageDir();

        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                imageDir        // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        // mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    */

    public static Bitmap getBitmap(String imagePath, int targetW, int targetH, boolean crop) {
        Bitmap result = null;
        if (crop) {
            Bitmap bitmap = getBitmap(imagePath, targetW, targetH, FitMode.FIT_MINOR_SIDE);

            int curW = bitmap.getWidth();
            int curH = bitmap.getHeight();

            int offsetX = 0;
            if (curW > targetW) {
                offsetX = (curW - targetW) / 2;
            }
            int offsetY = 0;
            if (curH > targetH) {
                offsetY = (curH - targetH) / 2;
            }

            result = Bitmap.createBitmap(bitmap, offsetX, offsetY, targetH, targetW);
        } else {
            result = getBitmap(imagePath, targetW, targetH, FitMode.FIT_ALL);
        }

        return result;
    }

    public static Bitmap getBitmap(String imagePath, int targetW, int targetH, FitMode fitMode) {
        // Get the dimensions of the bitmap
        int[] dimensions = getBitmapDimensions(imagePath);
        int imageW = dimensions[0];
        int imageH = dimensions[1];

        // Compute the greater scale factor to preserve the aspect ratio
        float scaleFactor = getScaleFactor(imageW, imageH, targetW, targetH, fitMode);

        int targetPhotoW = (int)(imageW * scaleFactor);
        int targetPhotoH = (int)(imageH * scaleFactor);

        // Decode the image file into a Bitmap sized to fill the View
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetPhotoW, targetPhotoH, true);

        return scaledBitmap;
    }

    public static float getScaleFactor(int imageW, int imageH, int targetW, int targetH, FitMode fitMode) {
        // Compute the greater scale factor to preserve the aspect ratio
        float scaleFactorW = (float)targetW / (float)imageW;
        float scaleFactorH = (float)targetH / (float)imageH;

        float scaleFactor = 1.0F;
        if (fitMode == FitMode.FIT_ALL) {
            scaleFactor = Math.min(scaleFactorW, scaleFactorH);
        } else {
            scaleFactor = Math.max(scaleFactorW, scaleFactorH);
        }
        if (scaleFactor < 0.1F)
            scaleFactor = 0.1F;
        else if (scaleFactor > 10.0F)
            scaleFactor = 10.0F;

        return scaleFactor;
    }

    private static int[] getBitmapDimensions(String imagePath) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        return new int[] {photoW, photoH};
    }

    public static Bitmap getBitmap(String imagePath) {
        return BitmapFactory.decodeFile(imagePath);
    }

    public static Bitmap getBitmap(byte[] imageBytes) {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public static void saveBitmap(String imagePath, Bitmap bitmap) throws FileNotFoundException{
        FileOutputStream out = new FileOutputStream(imagePath);

        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException ex) {
                Log.e(ImageUtils.class.getName(), "Error closing output image file: " + ex.toString());
            }
        }
    }
}
