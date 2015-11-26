package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ContactListActivity extends AppCompatActivity
        implements ContactListFragment.Callbacks,
        ContactDetailFragment.Callbacks {
    private static final String TAG = "ContactListActivity";
    private ContactEntry mCurrContactEntry;

    @LayoutRes
        private int getLayoutResId() {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return R.layout.activity_twopane;
        } else {
            return R.layout.activity_fragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();

        //Check if the id for placing the ContactListFragment in exists
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment == null) {
            fragment = new ContactListFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.detail_fragment_container);
        if(fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }

    @Override
    public void onContactSelected(ContactEntry ce) {
        Log.d(TAG, "onContactSelected");
        //Check if the id for placing the ContactDetailFragment in exists
        if(findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = ContactDetailActivity.newIntent(this, ce.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = ContactDetailFragment.newInstance(ce.getId());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    @Override
    public void onContactEntryEdit(ContactEntry ce) {
        Log.d(TAG, "onContactEntryEdit");

        // start the edit detail activity
        Intent intent = ContactEditDetailActivity.newIntent(this, ce.getId());
        startActivity(intent);
    }

    @Override
    public void onContactEntryDelete(ContactEntry ce) {
        Log.d(TAG, "onContactEntryDelete");

        mCurrContactEntry = ce;

        // TODO: before checking for confirmation from user that the entry should be deleted... may need to check first that the given contact entry is not null... (but this case shouldn't actually happen...)

        // get confirmation from user in a dialog that they want to go back without saving changes
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete this contact?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Yes, the user wants to delete the contact...

                // delete the currently selected contact entry
                ContactStore.get(getApplicationContext()).deleteContactEntry(mCurrContactEntry);
                //ContactStore.get(this).deleteContactEntry(ce);

                // after deletion, need to refresh the list view.
                FragmentManager fm = getSupportFragmentManager();
                // find the list fragment and update
                Fragment fragment = fm.findFragmentById(R.id.fragment_container);
                if(fragment != null) {
                    if (fragment instanceof ContactListFragment) {
                        ContactListFragment listFragment = (ContactListFragment) fragment;
                        listFragment.updateUI();
                    }
                }

                // check for the existence of the detailfragmentcontainer,
                // if it is present, it needs to be cleared.
                Fragment detailFragment = fm.findFragmentById(R.id.detail_fragment_container);
                if(detailFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(detailFragment)
                            .commit();
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the dialog here, if the user decides not to delete the currently active contact entry.
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onAddBlankContact() {
        Log.d(TAG, "onContactAddBlank");
        // Start the EditDetailActivity
        Intent intent = ContactEditDetailActivity.newIntent(this, null);
        startActivity(intent);
    }

    @Override
    public void onAddNewContactCard() {
        Log.d(TAG, "onContactAddNewContactCard");
        // Start the EditDetailActivity
        // -- NOTE: This needs to be modified later to process the business card first.
        Intent intent = ContactEditDetailActivity.newIntent(this, null);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // Need to update the views of the fragments.

        FragmentManager fm = getSupportFragmentManager();
        // find the list fragment
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment != null) {
            if (fragment instanceof ContactListFragment) {
                ContactListFragment listFragment = (ContactListFragment) fragment;
                listFragment.updateUI();
            }
        }

        // check for the existence of the detailfragmentcontainer,
        // if it is present, the view needs to be updated.
        Fragment detailFragment = fm.findFragmentById(R.id.detail_fragment_container);
        if(detailFragment != null) {
            if (detailFragment instanceof ContactDetailFragment) {
                ContactDetailFragment cdFragment = (ContactDetailFragment) detailFragment;
                cdFragment.updateUI();
            }
            /*
            getSupportFragmentManager().beginTransaction()
                    .detach(detailFragment)
                    .commit();
            getSupportFragmentManager().beginTransaction()
                    .attach(detailFragment)
                    .commit();
            */
        }
    }
}
