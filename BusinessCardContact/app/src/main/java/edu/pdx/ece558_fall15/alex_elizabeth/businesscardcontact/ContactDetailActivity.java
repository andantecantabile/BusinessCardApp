package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.content.Intent;
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

    private ViewPager mViewPager;
    private List<ContactEntry> mContactEntries;

    public static Intent newIntent(Context packageContext, UUID contactEntryId) {
        Intent intent = new Intent(packageContext, ContactDetailActivity.class);
        intent.putExtra(EXTRA_CONTACT_ENTRY_ID, contactEntryId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_view_pager);

        UUID crimeId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_CONTACT_ENTRY_ID);

        mViewPager = (ViewPager) findViewById(R.id.activity_view_pager);

        mContactEntries = ContactStore.get(this).getContactEntries();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                ContactEntry crime = mContactEntries.get(position);
                return ContactDetailFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mContactEntries.size();
            }
        });

        for (int i = 0; i < mContactEntries.size(); i++) {
            if (mContactEntries.get(i).getId().equals(crimeId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void onContactEntryUpdated(ContactEntry ce) {
        Log.d(TAG,"onContactEntryUpdated");
    }
}
