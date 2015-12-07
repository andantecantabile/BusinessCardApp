package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * DialogAsyncTask is AsyncTask that handles the creation and dismissal of a dialog with
 * provided text for an AsyncTask that derives from it.  This class is especially useful when you
 * only need to implement doInBackground, as it takes care of the creation and dismissal of the
 * dialog in the onPreExecute and onPostExecute methods.
 * @param <S1> A parameter that was required for this to compile
 * @param <S2> A second parameter that was required for this to compile
 * @param <B> A third parameter that was required for this to compile
 *
 * @author Alex Pearson & Elizabeth Reed
 * Date:   December 6th, 2015
 */
public abstract class DialogAsyncTask<S1, S2, B> extends AsyncTask<String, String, Boolean> {
    private static final String TAG = "DialogAsyncTask";

    //Initial message to set in the dialog
    private String mInitialStatus;

    //Reference to the dialog we display
    private ProgressDialog mProgressDialog;

    //A ContactEntry object that might be associated with the task
    private ContactEntry mContactEntry = new ContactEntry();

    //A Callbacks that the callback will be called on, need since we override onPostExecute
    private Callbacks mCallbacks;

    //A Context that this is called from
    private Context mContext;

    //A taskID that we can use to distinguish between different tasks
    private int mTaskId;

    /**
     * Get the ContactEntry associated with this task
     * @return the ContactEntry
     */
    public ContactEntry getContactEntry() {
        return mContactEntry;
    }

    /**
     * Set the ContactEntry associated with this task
     * @param contactEntry A ContactEntry to assocaite with this task
     */
    public void setContactEntry(ContactEntry contactEntry) {
        mContactEntry = contactEntry;
    }

    /**
     * Get the Application Context this was called from
     * @return The Application's Context
     */
    public Context getContext() {
        return mContext.getApplicationContext();
    }

    /**
     * Callbacks interface that the call application needs to subscribe to
     * Used to notify that the task is finished (since we override onPostExecute
     * Ends up looking similar to how onActivityResult looks
     */
    public interface Callbacks {
        void onAsyncTaskFinished(ContactEntry contactEntry, boolean success, int taskId);
    }

    /**
     * Create a new DialogAsyncTask with the required information
     * @param initialStatus The initial message to display
     * @param context The Context this is called from
     * @param callbacks The callbacks that implement the Callbacks
     * @param taskId The taskID to assign so it can be distinguished when multiple classes
     *               derive from this one
     */
    public DialogAsyncTask(String initialStatus, Context context, Callbacks callbacks, int taskId) {
        mInitialStatus = initialStatus;
        mCallbacks = callbacks;
        mContext = context;
        mTaskId = taskId;

        //Create the new ProgressDialog
        mProgressDialog = new ProgressDialog(context);
    }

    /**
     * onPreExecute implementation that takes care of setting up the dialog
     */
    @Override
    protected void onPreExecute() {
        //Setup the dialog with the initial message
        mProgressDialog.setMessage(mInitialStatus);

        //Don't allow the dialog to be canceled by the user
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        //Show the dialog
        mProgressDialog.show();
    }

    /**
     * onPostExecute implementation that takes care of dismissing the dialog
     * @param result the boolean result from doInBackground()
     */
    @Override
    protected void onPostExecute(Boolean result) {
        //Check if the task has not been cancelled
        if(!this.isCancelled()) {
            checkAndDismissDialog();

            // perform the callback (result should be either true/false)
            mCallbacks.onAsyncTaskFinished(mContactEntry, result, mTaskId);
        }
    }

    /**
     * Helper method to check if the dialog is showing and dismiss it.
     * Also catches exceptions that occur in strange cases.
     */
    private void checkAndDismissDialog() {
        try {
            //Check if the dialog is still showing
            if (mProgressDialog.isShowing()) {
                //Dismiss the dialog
                mProgressDialog.dismiss();
            }
        } catch (IllegalArgumentException iae) {
            //Catch exceptions if the dialog couldn't be removed
            //This sometimes gets triggered due to rotation and even if we tried to cancel
            //the task it still tries to dismiss after the window manager doesn't exist anymore
            Log.e(TAG, "The dialog couldn't be dismissed");
        }
    }

    /**
     * onCancelled implementation that handles dismissing the dialog
     * @param result The boolean result from doInBackground()
     */
    @Override
    protected void onCancelled(Boolean result) {
        checkAndDismissDialog();
    }

    /**
     * onProgressUpdate implementation that sets the message to a provided value
     * @param values Passed in string to have the dialog message updated to
     */
    @Override
    protected void onProgressUpdate(String... values) {
        String stage = values[0];
        mProgressDialog.setMessage(stage);
    }
}
