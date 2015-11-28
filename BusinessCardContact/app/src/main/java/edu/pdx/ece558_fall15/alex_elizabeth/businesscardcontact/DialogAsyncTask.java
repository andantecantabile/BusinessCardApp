package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;


public abstract class DialogAsyncTask<S1, S2, B> extends AsyncTask<String, String, Boolean> {
    private String mInitialStatus;
    private ProgressDialog mProgressDialog;
    private ContactEntry mContactEntry = new ContactEntry();
    private Callbacks mCallbacks;
    private Context mContext;

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
        void onAsyncTaskFinished(ContactEntry contactEntry, boolean success);
    }

    public DialogAsyncTask(String initialStatus, Context context, Callbacks callbacks) {
        mInitialStatus = initialStatus;
        mCallbacks = callbacks;
        mProgressDialog = new ProgressDialog(context);
        mContext = context;
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
        if(mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        // perform the callback (result should be either true/false)
        mCallbacks.onAsyncTaskFinished(mContactEntry, result);

        /*
        // Alternatively, test the result for null explicitly;
        // set to false if for some reason, result is not null.
        if (result != null) // test result for null
            mCallbacks.onAsyncTaskFinished(mContactEntry, result);
        else
            mCallbacks.onAsyncTaskFinished(mContactEntry, false);
        */
    }

    @Override
    protected void onProgressUpdate(String... values) {
        String stage = values[0];
        mProgressDialog.setMessage(stage);
    }
}
