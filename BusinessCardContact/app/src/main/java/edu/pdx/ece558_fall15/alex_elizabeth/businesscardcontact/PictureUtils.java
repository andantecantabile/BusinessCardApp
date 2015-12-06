package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for utility methods for picture handling, modified from BNRG
 */
public class PictureUtils {
    private static final String TAG = "PictureUtils";   // logcat tag

    /**
     * Get the scaled bitmap from the given file path
     * @param path      file path
     * @param activity  calling activity
     * @return  image bitmap
     */
    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        // get the size of the activity window
        activity.getWindowManager().getDefaultDisplay()
                .getSize(size);

        return getScaledBitmap(path, size.x, size.y);
    }

    /**
     * Returns a bitmap of the image file located at the specified path.
     * @param path          image file path
     * @param destWidth     scaled image width
     * @param destHeight    scaled image height
     * @return              scaled image bitmap
     */
    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        // read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        // calculate the target width and height of the bitmap
        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / destHeight);
            } else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;    // set the target bitmap image size

        // convert the file to a image file to a bitmap
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Create the image chooser intent.
     * @param outputFileUri the destination uri for the returned image
     * @param chooserText   string to be displayed in the image chooser activity
     * @param context       calling context
     * @return      the intent for the image chooser
     */
    public static Intent getImageChooserIntent(Uri outputFileUri, String chooserText, Context context) {
        final List<Intent> cameraIntents = new ArrayList<>();   // array for camera intents
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            // create the intent for the camera
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            Log.d(TAG, outputFileUri.getPath());
            cameraIntents.add(intent);  // add the camera intent
        }

        //Build a list of FileSystem sources that could provided the correct data
        final Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //Create chooser of FileSystem options
        final Intent chooserIntent = Intent.createChooser(galleryIntent, chooserText);

        //Add the camera options
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        return chooserIntent;
    }

    /**
     * Helper method to save a bitmap to file, modified from stackoverflow
     * http://stackoverflow.com/questions/22784656/convert-android-graphics-bitmap-to-java-io-file/22785013#22785013
     * @param filesDir  directory where the file should be stored
     * @param bitmap    bitmap to be saved to file
     * @param name      the name that should be given to the image file (will be saved with a .jpg extension)
     * @return          the saved image file
     */
    public static File persistImage(File filesDir, Bitmap bitmap, String name) {
        File imageFile = new File(filesDir, name + ".jpg");
        Log.d(TAG, "Image File Path: " + imageFile.getAbsolutePath());
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(TAG, "Error writing bitmap", e);
            return null;
        }

        return imageFile;
    }
}
