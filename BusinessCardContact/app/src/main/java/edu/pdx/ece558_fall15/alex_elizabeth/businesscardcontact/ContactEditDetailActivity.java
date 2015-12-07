package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.UUID;

/**
 * Activity to display the ContactEditDetailFragment (used in both portrait and landscape mode).
 *
 * @author Alex Pearson & Elizabeth Reed
 * Date:   December 6th, 2015
 */
public class ContactEditDetailActivity extends AppCompatActivity
        implements ContactEditDetailFragment.Callbacks {
    private static final String TAG = "ContactEditActivity";    // logcat tag

    // the id of the contact entry to be edited
    private static final String EXTRA_CONTACT_ENTRY_ID =
            "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact.contactEntryId";
    // flag that indicates edit of a new contact
    private static final String EXTRA_NEW_CONTACT =
            "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact.newContact";

    private ContactEntry mContactEntry; // contact entry that is currently being edited

    /**
     * Create a new intent for a ContactEditDetailActivity instance (puts the given parameters as extras in the intent).
     * @param packageContext    context of calling activity
     * @param contactEntryId    id of the contact entry to be edited
     * @param newContact        flag indicating if the contact entry is new
     * @return  the activity intent with contact id and new contact flag extra parameters
     */
    public static Intent newIntent(Context packageContext, UUID contactEntryId, boolean newContact) {
        Intent intent = new Intent(packageContext, ContactEditDetailActivity.class);
        intent.putExtra(EXTRA_CONTACT_ENTRY_ID, contactEntryId);
        intent.putExtra(EXTRA_NEW_CONTACT, newContact);
        return intent;
    }

    /**
     * Extracts the id of the last edited contact entry (from the extra parameter) from the given intent.
     * @param intent    the activity intent
     * @return      the contact entry id that was passed in the given intent
     */
    public static UUID lastEditedId(Intent intent) {
        return (UUID) intent.getSerializableExtra(EXTRA_CONTACT_ENTRY_ID);
    }

    /**
     * On creation of the activity, the theme for the activity should be set, and the arguments (id of the
     * contact entry to be edited and new contact flag) need to be extracted the intent extra
     * @param savedInstanceState the saved instance state variables
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        SettingsUtils.onActivityCreateSetTheme(this);   // set the activity theme
        setContentView(R.layout.activity_fragment);     // get the layout placeholder

        // obtain the arguments for the contact id to be edited, and the new contact flag
        UUID contactId = (UUID) getIntent().getSerializableExtra(EXTRA_CONTACT_ENTRY_ID);
        boolean newContact = getIntent().getBooleanExtra(EXTRA_NEW_CONTACT, true);

        // if the contact id exists, obtain the contact entry from the database
        mContactEntry = contactId == null ? null : ContactStore.get(this).getContactEntry(contactId);

        FragmentManager fm = getSupportFragmentManager();
        //Check if the id for placing the ContactEditDetailFragment in exists
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment == null) {
            fragment = ContactEditDetailFragment.newInstance(contactId, newContact);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * Handles the "Save" menu action item functionality.
     * @param ce    contact entry (that was being edited) to be saved
     */
    @Override
    public void onContactEntrySaveChanges(ContactEntry ce) {
        Log.d(TAG, "onContactEntrySaveChanges");

        mContactEntry = ce;
        // Note that the save operation of the contact has already been performed in the fragment.
        // Here, need to close the current activity so that it would return to the previous view (either list or detail)
        finishActivity(true);
    }

    /**
     * Handles the "Cancel" menu action item functionality.
     * @param ce    contact entry (currently selected)
     */
    @Override
    public void onContactEntryCancelChanges(ContactEntry ce) {
        Log.d(TAG,"onContactEntryCancelChanges");

        mContactEntry = ce;
        // On cancel, want to return to the previous activity.
        finishActivity(false);
    }

    /**
     * Method called when the back button is pressed. Displays a dialog to get confirmation from the
     * user that they want to close the edit activity without saving changes.
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG,"onBackPressed");

        // get confirmation from user in a dialog that they want to go back without saving changes
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Return without saving changes?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Yes, the user wants to exit, so close the activity
                finishActivity(false);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the closing of the activity here
                dialog.cancel();   // close the dialog
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Handle the closing of the edit detail activity. (Returns the id of the current contact entry if necessary,
     * so that the previous activity may be updated if the DetailFragment is displayed.)
     * @param returnContact If true, then the contact entry id should be stored be sent back as an extra in the result.
     */
    private void finishActivity(boolean returnContact) {
        Intent data = new Intent();
        if(returnContact) {
            // if the contact id should be returned, send it back as an extra.
            setResult(RESULT_OK, data);
            data.putExtra(EXTRA_CONTACT_ENTRY_ID, mContactEntry.getId());
        } else {
            // otherwise send a "Canceled" flag to indicate that the contact was not added or edited.
            setResult(RESULT_CANCELED, data);
        }
        finish();   // end the activity
    }
}
