package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Activity to display the ContactDetailFragment (used in portrait mode).
 *
 * @author Alex Pearson & Elizabeth Reed
 * Date:   December 6th, 2015
 */
public class ContactDetailActivity extends AppCompatActivity
    implements ContactDetailFragment.Callbacks {
    private static final String TAG = "ContactDetailActivity";  // activity tag for logcat

    // id of the contact entry to be displayed
    private static final String EXTRA_CONTACT_ENTRY_ID =
            "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact.contactEntryId";
    // key for currently displayed contact entry (used in saved instance state)
    private static final String KEY_ENTRY_ID = "entry_id";

    private ViewPager mViewPager;
    private List<ContactEntry> mContactEntries; // list of all contact entry objects
    private ContactEntry mCurrContactEntry;     // currently displayed contact entry
    private int mCurrPosition;                  // position of the currently displayed entry in the list

    /**
     * Create a new intent for a ContactDetailActivity instance (puts the supplied contact entry id in an extra).
     * @param packageContext    context of calling activity
     * @param contactEntryId    id of the contact entry to be displayed in the new contact detail activity
     * @return the activity intent with contact id extra
     */
    public static Intent newIntent(Context packageContext, UUID contactEntryId) {
        Intent intent = new Intent(packageContext, ContactDetailActivity.class);
        intent.putExtra(EXTRA_CONTACT_ENTRY_ID, contactEntryId);    // the contact id need to be passed as a parameter in the intent
        return intent;
    }

    /**
     * Extracts the id of the last viewed contact entry (from the extra parameter) from the given intent.
     * @param intent    the activity intent
     * @return      the contact entry id that was passed in the given intent
     */
    public static UUID lastViewedID(Intent intent) {
        return (UUID) intent.getSerializableExtra(EXTRA_CONTACT_ENTRY_ID);
    }

    /**
     * On creation of the activity, the theme for the activity should be set, and the id of the contact entry to be displayed
     * needs to be extracted from either the intent extra or from the key id from the saved instance state.
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        SettingsUtils.onActivityCreateSetTheme(this);   // set the activity theme

        // get id from intent extra
        UUID contactId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_CONTACT_ENTRY_ID);

        // if the saved instance state exists, get the saved contact id
        if(savedInstanceState != null) {
            UUID currId = (UUID) savedInstanceState.getSerializable(KEY_ENTRY_ID);
            if(currId != null) {
                mCurrContactEntry = ContactStore.get(this).getContactEntry(currId);
            }
        }

        // determine the current orientation of the device
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // if in landscape mode, create a new intent that will contain the id of the currently displayed contact entry
            Intent data = new Intent();
            if (mCurrContactEntry != null) {
                data.putExtra(EXTRA_CONTACT_ENTRY_ID, mCurrContactEntry.getId());
            }
            setResult(RESULT_OK, data); // return this data to the ContactListActivity
            this.finish();  // close the DetailActivity (the detail will be displayed in the list activity for landscape mode)
        }

        // otherwise, the device is in portrait mode, so set up the view pager
        setContentView(R.layout.activity_view_pager);

        mViewPager = (ViewPager) findViewById(R.id.activity_view_pager);

        // obtain the list of contact entries from the ContactStore class (from the database)
        mContactEntries = ContactStore.get(this).getContactEntries();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            /**
             * Returns a new ContactDetailFragment for the contact entry at the specified position in the list.
             * @param position  Index of the contact entry in the list.
             * @return          New instance of the contact detail fragment for the contact entry at the given list position.
             */
            @Override
            public Fragment getItem(int position) {
                return ContactDetailFragment.newInstance(ContactStore.get(getApplicationContext())
                        .getContactEntryAtPosition(position).getId());
            }

            /**
             * Returns the number of entries in the contact entry list.
             * @return  number of contact entries
             */
            @Override
            public int getCount() {
                return mContactEntries.size();
            }
        });

        // create a listener for the page change event
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                // when a new page is selected, need to update the locally stored currently displayed contact entry
                mCurrContactEntry = mContactEntries.get(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        // attach the entries in the ContactEntry list to the view pager
        for (int i = 0; i < mContactEntries.size(); i++) {
            ContactEntry nextEntry = mContactEntries.get(i);
            if (nextEntry.getId().equals(contactId)) {
                mCurrContactEntry = nextEntry;
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    /**
     * Handles the "Edit" menu action item functionality.
     * @param ce    contact entry (currently selected) to be edited
     */
    @Override
    public void onContactEntryEdit(ContactEntry ce) {
        Log.d(TAG, "onContactEntryEdit");

        mCurrContactEntry = ce;

        // start the edit detail activity
        Intent intent = ContactEditDetailActivity.newIntent(this, ce.getId(), false);   // send the id of the contact entry
        startActivity(intent);
    }

    /**
     * Handles the "Delete" menu action item functionality.
     * @param ce    contact entry (currently selected) to be deleted
     */
    @Override
    public void onContactEntryDelete(ContactEntry ce) {
        Log.d(TAG,"onContactEntryDelete");

        finish(); // if a contact entry has been deleted, just exit the activity since the delete operation has already been performed in the fragment
    }

    /**
     * On pause of the activity, save the index position of the currently displayed contact entry in the list.
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mCurrPosition = mViewPager.getCurrentItem();    // obtain index position from the view pager
    }

    /**
     * On resume of the activity, obtain the entire list of entries again (in order to get the current version
     * of all items in the list; refreshes the contact entry list).
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // refresh the fragment view
        mContactEntries = ContactStore.get(this).getContactEntries();   // get updated list of contact entries
        mViewPager.getAdapter().notifyDataSetChanged(); // notify data set changed so that the view pager will update the view with the new values
    }

    /**
     * Save the instance state: need to remember the id of current contact entry.
     * @param outState  bundle will contain the id of the currently displayed contact entry.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_ENTRY_ID, mCurrContactEntry.getId());
    }
}
