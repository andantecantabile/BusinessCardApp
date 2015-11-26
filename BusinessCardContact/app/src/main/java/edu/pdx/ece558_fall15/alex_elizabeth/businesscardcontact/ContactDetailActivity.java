package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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

public class ContactDetailActivity extends AppCompatActivity
    implements ContactDetailFragment.Callbacks {
    private static final String TAG = "ContactDetailActivity";

    private static final String EXTRA_CONTACT_ENTRY_ID =
            "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact.contactEntryId";

    private ViewPager mViewPager;
    private List<ContactEntry> mContactEntries;
    private ContactEntry mCurrContactEntry;

    public static Intent newIntent(Context packageContext, UUID contactEntryId) {
        Intent intent = new Intent(packageContext, ContactDetailActivity.class);
        intent.putExtra(EXTRA_CONTACT_ENTRY_ID, contactEntryId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.finish();
        }

        setContentView(R.layout.activity_view_pager);

        UUID contactId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_CONTACT_ENTRY_ID);

        mViewPager = (ViewPager) findViewById(R.id.activity_view_pager);

        mContactEntries = ContactStore.get(this).getContactEntries();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                ContactEntry currContact = mContactEntries.get(position);
                return ContactDetailFragment.newInstance(currContact.getId());
            }

            @Override
            public int getCount() {
                return mContactEntries.size();
            }
        });

        for (int i = 0; i < mContactEntries.size(); i++) {
            if (mContactEntries.get(i).getId().equals(contactId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void onContactEntryEdit(ContactEntry ce) {
        Log.d(TAG, "onContactEntryEdit");

        // start the edit detail activity
        Intent intent = ContactEditDetailActivity.newIntent(this, ce.getId());
        //intent.putExtra(EXTRA_CONTACT_ENTRY_ID, ce.getId());
        startActivity(intent);
    }

    @Override
    public void onContactEntryDelete(ContactEntry ce) {
        Log.d(TAG,"onContactEntryDelete");

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

                // after deletion, need to return to the list view...
                // which should be the previous activity in the backstack, so need to close this activity
                finish();
                // NOTE: Alternatively, if this doesn't work, would need to start the list activity here.
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
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // refresh the fragment view
        FragmentManager fm = getSupportFragmentManager();
        //Check if the id for placing the ContactListFragment in exists
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .detach(fragment)
                    .attach(fragment)
                    .commit();
        }
    }
}
