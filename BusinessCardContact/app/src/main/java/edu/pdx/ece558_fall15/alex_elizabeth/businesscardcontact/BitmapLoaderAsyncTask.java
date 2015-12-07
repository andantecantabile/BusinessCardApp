package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * AsyncTask that loads bitmaps into memory and then sets them to the provided imageViews
 *
 * Authors: Alex Pearson and Elizabeth Reed
 * Date:    December 6th, 2015
 */
public class BitmapLoaderAsyncTask extends DialogAsyncTask<String, String, Boolean> {
    private static final String INIT_STATUS_MSG = "Loading Images..";
    public static final int TASK_ID = 104;

    //An array of imageViews
    private ImageView[] mImageViews;

    //An array of Bitmaps
    private Bitmap[] mBitmaps;

    /**
     * Constructor that takes the Context, Callbacks, and a array of imageViews to populate
     * @param context The Context this is called from
     * @param callbacks The Callbacks to call the callbacks on
     * @param imageViews The list of imageViews to poplutate
     */
    BitmapLoaderAsyncTask(Context context, Callbacks callbacks, ImageView[] imageViews) {
        super(INIT_STATUS_MSG, context, callbacks, TASK_ID);
        mImageViews = imageViews;
    }

    /**
     * Implementation of doInBackground that takes in a list of filenames to populate the
     * imageViews from
     * @param params List of image filenames
     * @return Result of the operation
     */
    @Override
    protected Boolean doInBackground(String... params) {

        //Loop through the list of filenames and load the bitmaps into memory
        mBitmaps = new Bitmap[params.length];
        for(int i = 0; i < params.length; i++) {
            mBitmaps[i] = PictureUtils.getScaledBitmap(params[i], 300, 300);
        }
        return true;
    }

    /**
     * Set the bitmaps to the file views
     * @param result the boolean result from doInBackground()
     */
    @Override
    protected void onPostExecute(Boolean result) {
        //Don't load anything if the task has been canceled
        if(!this.isCancelled()) {
            //Only load the bitmaps if the result was true
            if (result) {
                //Make sure to load only as many as the smaller of the 2 arrays
                int size = mImageViews.length > mBitmaps.length ? mBitmaps.length : mImageViews.length;
                for (int i = 0; i < size; i++) {
                    //Set the bitmap to the imageView in order
                    mImageViews[i].setImageBitmap(mBitmaps[i]);
                }
            }
            //execute the super method after assigning the bitmaps to the imageViews
            super.onPostExecute(result);
        }
    }
}
