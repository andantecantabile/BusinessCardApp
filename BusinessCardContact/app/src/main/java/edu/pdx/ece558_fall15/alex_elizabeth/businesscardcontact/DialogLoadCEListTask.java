package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Async task used to load the contact list.
 */
public class DialogLoadCEListTask extends DialogAsyncTask<String, String, Boolean> {
    private static final String TAG = "DialogLoadCEListTask";

    // Assign a unique task id.
    public static final int TASK_ID = 9;

    // Define the status message for the spinning dialog
    private static final String INIT_STATUS_MSG = "Loading contact list...";
    private Context mContext;
    private List<ContactEntry> mContactEntries;

    // Special list callbacks since this needs to provide different information than
    // the DialogAsyncTask
    private ListCallbacks mListCallbacks;

    /**
     * Constructor that calls the super and sets up the list callbacks
     * @param context Context this is called from
     * @param callbacks Callbacks that implement the DialogAsyncTask Callbacks
     * @param listCallbacks Callbacks that implement the ListCallbacks interface
     */
    public DialogLoadCEListTask(Context context, Callbacks callbacks, ListCallbacks listCallbacks) {
        //Call the super to set the dialog
        super(INIT_STATUS_MSG, context, callbacks, TASK_ID);
        mContext = context;

        //Setup the list callbacks
        mListCallbacks = listCallbacks;
    }

    /**
     * Interface that we use to send a callback with the list of loaded ContactEntry objects to
     * the calling Activity
     */
    public interface ListCallbacks {
        void onAsyncListTaskDone(List<ContactEntry> contactEntries, boolean success, int taskId);
    }

    /**
     * Implementation of doInBackground() that gets the list of contacts from the Contact Storage
     * @param params String params that the async task could use
     * @return The result of the operation
     */
    @Override
    protected Boolean doInBackground(String... params) {
        Log.d(TAG, "doInBackground");
        // retrieve the list of contact entries
        mContactEntries = ContactStore.get(mContext).getContactEntries();
        return true;
    }

    /**
     * If the task isn't cancelled, call the super method to dismiss the dialog and use the
     * list callback to return the ContactEntry object list
     * @param result the boolean result from doInBackground()
     */
    @Override
    protected void onPostExecute(Boolean result) {
        if(!this.isCancelled()) {
            super.onPostExecute(result);

            // perform the callback (result should be either true/false)
            mListCallbacks.onAsyncListTaskDone(mContactEntries, result, TASK_ID);
        }
    }
}
