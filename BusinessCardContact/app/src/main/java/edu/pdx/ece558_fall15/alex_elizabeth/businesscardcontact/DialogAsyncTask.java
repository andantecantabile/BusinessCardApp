package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public abstract class DialogAsyncTask<S1, S2, B> extends AsyncTask<String, String, Boolean> {
    private static final String TAG = "DialogAsyncTask";

    private String mInitialStatus;
    private ProgressDialog mProgressDialog;
    private ContactEntry mContactEntry = new ContactEntry();
    private Callbacks mCallbacks;
    private Context mContext;
    private int mTaskId;

    public ContactEntry getContactEntry() {
        return mContactEntry;
    }

    public void setContactEntry(ContactEntry contactEntry) {
        mContactEntry = contactEntry;
    }

    public Context getContext() {
        return mContext.getApplicationContext();
    }

    public interface Callbacks {
        void onAsyncTaskFinished(ContactEntry contactEntry, boolean success, int taskId);
    }

    public DialogAsyncTask(String initialStatus, Context context, Callbacks callbacks, int taskId) {
        mInitialStatus = initialStatus;
        mCallbacks = callbacks;
        mProgressDialog = new ProgressDialog(context);
        mContext = context;
        mTaskId = taskId;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.setMessage(mInitialStatus);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(!this.isCancelled()) {
            try {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            } catch (IllegalArgumentException iae) {
                Log.e(TAG, "The dialog couldn't be dismissed");
            }

            // perform the callback (result should be either true/false)
            mCallbacks.onAsyncTaskFinished(mContactEntry, result, mTaskId);

        /*
        // Alternatively, test the result for null explicitly;
        // set to false if for some reason, result is not null.
        if (result != null) // test result for null
            mCallbacks.onAsyncTaskFinished(mContactEntry, result);
        else
            mCallbacks.onAsyncTaskFinished(mContactEntry, false);
        */
        }
    }

    @Override
    protected void onCancelled(Boolean result) {
        try {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "The dialog couldn't be dismissed");
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        String stage = values[0];
        mProgressDialog.setMessage(stage);
    }
}
