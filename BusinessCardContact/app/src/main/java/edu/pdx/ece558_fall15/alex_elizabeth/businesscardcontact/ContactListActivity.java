package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class ContactListActivity extends FragmentActivity
        implements ContactListFragment.Callbacks {

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
    public void onContactSelected(ContactEntry ce) {
        //Check if the id for placing the ContactDetailFragment in exists
        /*if(findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = ContactDetailActivity.newInstance(ce.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = ContactDetailFragment.newInstance(ce.getId());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }*/
    }
}
