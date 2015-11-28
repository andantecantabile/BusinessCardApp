package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class ContactEditDetailActivity extends AppCompatActivity
        implements ContactEditDetailFragment.Callbacks {
    private static final String TAG = "ContactEditActivity";

    private static final String EXTRA_CONTACT_ENTRY_ID =
            "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact.contactEntryId";

    private ContactEntry mContactEntry;

    public static Intent newIntent(Context packageContext, UUID contactEntryId) {
        Intent intent = new Intent(packageContext, ContactEditDetailActivity.class);
        intent.putExtra(EXTRA_CONTACT_ENTRY_ID, contactEntryId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_fragment);

        UUID contactId = (UUID) getIntent().getSerializableExtra(EXTRA_CONTACT_ENTRY_ID);

        mContactEntry = ContactStore.get(this).getContactEntry(contactId);
        FragmentManager fm = getSupportFragmentManager();

        //Check if the id for placing the ContactListFragment in exists
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment == null) {
            fragment = ContactEditDetailFragment.newInstance(contactId);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onContactEntrySaveChanges(ContactEntry ce) {
        Log.d(TAG, "onContactEntrySaveChanges");

        // Note that the save operation of the contact has already been performed in the fragment.
        // Here, need to close the current activity so that it would return to the previous view (either list or detail?)
        //finish();
    }

    @Override
    public void onContactEntryCancelChanges(ContactEntry ce) {
        Log.d(TAG,"onContactEntryCancelChanges");

        // On cancel, want to return to the previous activity.
        finish();
    }

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
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the closing of the activity here
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
