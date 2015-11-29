package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class BitmapLoaderAsyncTask extends DialogAsyncTask<String, String, Boolean> {
    private static final String INIT_STATUS_MSG = "Loading Images..";
    public static final int TASK_ID = 104;
    private ImageView[] mImageViews;
    private Bitmap[] mBitmaps;

    BitmapLoaderAsyncTask(Context context, Callbacks callbacks, ImageView[] imageViews) {
        super(INIT_STATUS_MSG, context, callbacks, TASK_ID);
        mImageViews = imageViews;
    }
    @Override
    protected Boolean doInBackground(String... params) {
        mBitmaps = new Bitmap[params.length];
        for(int i = 0; i < params.length; i++) {
            mBitmaps[i] = PictureUtils.getScaledBitmap(params[i], 300, 300);
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(!this.isCancelled()) {
            if (result) {
                int size = mImageViews.length > mBitmaps.length ? mBitmaps.length : mImageViews.length;
                for (int i = 0; i < size; i++) {
                    mImageViews[i].setImageBitmap(mBitmaps[i]);
                }
            }
            super.onPostExecute(result);
        }
    }
}
