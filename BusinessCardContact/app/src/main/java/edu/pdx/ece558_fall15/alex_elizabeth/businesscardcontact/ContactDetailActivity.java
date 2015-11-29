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

public class ContactDetailActivity extends AppCompatActivity
    implements ContactDetailFragment.Callbacks {
    private static final String TAG = "ContactDetailActivity";

    private static final String EXTRA_CONTACT_ENTRY_ID =
            "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact.contactEntryId";
    private static final String KEY_ENTRY_ID = "entry_id";

    private ViewPager mViewPager;
    private List<ContactEntry> mContactEntries;
    private ContactEntry mCurrContactEntry;
    private int mCurrPosition;

    public static Intent newIntent(Context packageContext, UUID contactEntryId) {
        Intent intent = new Intent(packageContext, ContactDetailActivity.class);
        intent.putExtra(EXTRA_CONTACT_ENTRY_ID, contactEntryId);
        return intent;
    }

    public static UUID lastViewedID(Intent intent) {
        return (UUID) intent.getSerializableExtra(EXTRA_CONTACT_ENTRY_ID);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        SettingsUtils.onActivityCreateSetTheme(this);   // set the activity theme

        UUID contactId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_CONTACT_ENTRY_ID);

        if(savedInstanceState != null) {
            UUID currId = (UUID) savedInstanceState.getSerializable(KEY_ENTRY_ID);
            if(currId != null) {
                mCurrContactEntry = ContactStore.get(this).getContactEntry(currId);
            }
        }

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Intent data = new Intent();
            data.putExtra(EXTRA_CONTACT_ENTRY_ID, mCurrContactEntry.getId());
            setResult(RESULT_OK, data);
            this.finish();
        }

        setContentView(R.layout.activity_view_pager);

        mViewPager = (ViewPager) findViewById(R.id.activity_view_pager);

        mContactEntries = ContactStore.get(this).getContactEntries();
        FragmentManager fragmentManager = getSupportFragmentManager();
        //FragmentStatePagerAdapter fsPagerAdapter = new FragmentStatePagerAdapter(fragmentManager);

        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return ContactDetailFragment.newInstance(ContactStore.get(getApplicationContext())
                        .getContactEntryAtPosition(position).getId());
            }

            @Override
            public int getCount() {
                return mContactEntries.size();
            }
        });


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                mCurrContactEntry = mContactEntries.get(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        for (int i = 0; i < mContactEntries.size(); i++) {
            ContactEntry nextEntry = mContactEntries.get(i);
            if (nextEntry.getId().equals(contactId)) {
                mCurrContactEntry = nextEntry;
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void onContactEntryEdit(ContactEntry ce) {
        Log.d(TAG, "onContactEntryEdit");

        mCurrContactEntry = ce;

        // start the edit detail activity
        Intent intent = ContactEditDetailActivity.newIntent(this, ce.getId(), false);
        startActivity(intent);
    }

    @Override
    public void onContactEntryDelete(ContactEntry ce) {
        Log.d(TAG,"onContactEntryDelete");

        finish(); // if a contact entry has been deleted, just exit the activity since the delete operation has already been performed in the fragment
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mCurrPosition = mViewPager.getCurrentItem();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // refresh the fragment view
        // TODO: only update the single entry that has been changed rather than re-obtaining the entire list.
        mContactEntries = ContactStore.get(this).getContactEntries();
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_ENTRY_ID, mCurrContactEntry.getId());
    }
}
