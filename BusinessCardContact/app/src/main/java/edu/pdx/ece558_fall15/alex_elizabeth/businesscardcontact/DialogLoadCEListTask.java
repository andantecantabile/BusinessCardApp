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
    private ListCallbacks mListCallbacks;

    public DialogLoadCEListTask(Context context, Callbacks callbacks, ListCallbacks listCallbacks) {
        super(INIT_STATUS_MSG,context,callbacks, TASK_ID);
        mContext = context;
        mListCallbacks = listCallbacks;
    }

    public interface ListCallbacks {
        void onAsyncListTaskDone(List<ContactEntry> contactEntries, boolean success, int taskId);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Log.d(TAG, "doInBackground");
        // retrieve the list of contact entries
        mContactEntries = ContactStore.get(mContext).getContactEntries();
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        // perform the callback (result should be either true/false)
        mListCallbacks.onAsyncListTaskDone(mContactEntries, result, TASK_ID);

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
