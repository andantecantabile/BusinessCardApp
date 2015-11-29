package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ContactListActivity extends AppCompatActivity
        implements ContactListFragment.Callbacks,
        ContactDetailFragment.Callbacks,
        OCRAsyncTask.Callbacks {

    private static final String TAG = "ContactListActivity";

    private static final int REQUEST_CODE_VIEW = 0;
    private static final int REQUEST_CODE_GET_IMAGE = 1;
    private static final int REQUEST_CODE_EDIT = 2;

    private ContactEntry mCurrContactEntry;
    private boolean mNeedUpdate = false;

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
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        removeDetailFragmentUI();   // remove the detail fragment
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
            startActivityForResult(intent, REQUEST_CODE_VIEW);
        } else {
            if(mCurrContactEntry == null || !mCurrContactEntry.getId().equals(ce.getId())) {
                mCurrContactEntry = ce;
                setDetailFragment(ce);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            return;
        }
        if(requestCode == REQUEST_CODE_VIEW) {
            UUID currID = ContactDetailActivity.lastViewedID(data);
            if(currID != null) {
                ContactEntry ce = ContactStore.get(this).getContactEntry(currID);
                if (findViewById(R.id.detail_fragment_container) != null) {
                    mCurrContactEntry = ce;
                    setDetailFragment(ce);
                }
            }
        } else if(requestCode == REQUEST_CODE_EDIT) {
            UUID currID = ContactEditDetailActivity.lastEditedId(data);
            if(currID != null) {
                mCurrContactEntry = ContactStore.get(this).getContactEntry(currID);
                mNeedUpdate = true;
            }
        } else if(requestCode == REQUEST_CODE_GET_IMAGE) {
            File BCFile = null;

            //Returned from the camera
            if(data == null) {
                File filesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                BCFile = new File( filesDir, ContactStore.get(this)
                            .getSuggestedBCFile(mCurrContactEntry).getName() + ".jpg");
            }
            //Returned from the gallery
            if(data != null && data.getData() != null) {
                Uri uri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this
                            .getContentResolver(), uri);
                    // Log.d(TAG, String.valueOf(bitmap));
                    File filesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                    // update business card image
                    File tmpFile = PictureUtils.persistImage(filesDir, bitmap,
                            mCurrContactEntry.getSuggestedBCFilename());
                    if (tmpFile != null) {
                        BCFile = tmpFile;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(BCFile != null) {
                String tempFile = mCurrContactEntry.getId() + ".xml";
                OCRAsyncTask parseBC = new OCRAsyncTask("Beginning Upload..", this, this);
                parseBC.execute(BCFile.getPath(), tempFile);
            }
        }
    }

    private void setDetailFragment(ContactEntry ce) {
        Fragment newDetail = ContactDetailFragment.newInstance(ce.getId());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_fragment_container, newDetail)
                .commit();
    }

    @Override
    public void onContactEntryEdit(ContactEntry ce) {
        Log.d(TAG, "onContactEntryEdit");

        // start the edit detail activity
        mCurrContactEntry = ce;
        Intent intent = ContactEditDetailActivity.newIntent(this, ce.getId(), false);
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    @Override
    public void onContactEntryDelete(ContactEntry ce) {
        Log.d(TAG, "onContactEntryDelete");

        // Need to update the views of the fragments.
        updateListFragmentUI();     // update list view
        removeDetailFragmentUI();   // clear detail view
    }

    @Override
    public void onAddBlankContact() {
        Log.d(TAG, "onContactAddBlank");
        // Start the EditDetailActivity
        Intent intent = ContactEditDetailActivity.newIntent(this, null, true);
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    @Override
    public void onAddNewContactCard() {
        Log.d(TAG, "onContactAddNewContactCard");
        mCurrContactEntry = new ContactEntry();
        File filesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Uri outputFileUri = Uri.fromFile(new File(filesDir, ContactStore.get(this)
                .getSuggestedBCFile(mCurrContactEntry).getName() + ".jpg"));
        String chooserText = getResources().getString(R.string.chooserBCImage);
        Intent intent = PictureUtils.getImageChooserIntent(outputFileUri, chooserText, this);
        startActivityForResult(intent, REQUEST_CODE_GET_IMAGE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if(mNeedUpdate) {
            if (findViewById(R.id.detail_fragment_container) != null) {
                setDetailFragment(mCurrContactEntry);
            }
            mNeedUpdate = false;
        }

        // Need to update the views of the fragments.
        updateListFragmentUI();
        updateDetailFragmentUI();
    }

    /**
     * This method is used to update the list fragment UI display.
     */
    public void updateListFragmentUI() {
        // Need to update the views of the fragments.

        FragmentManager fm = getSupportFragmentManager();
        // find the list fragment
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            if (fragment instanceof ContactListFragment) {
                ContactListFragment listFragment = (ContactListFragment) fragment;
                listFragment.refreshUI();   // refresh the UI for the contact list
            }
        }
    }

    /**
     * This method is used to update the detail fragment UI
     */
    public void updateDetailFragmentUI() {
        // check for the existence of the detailfragmentcontainer,
        // if it is present, the view needs to be updated.
        FragmentManager fm = getSupportFragmentManager();
        Fragment detailFragment = fm.findFragmentById(R.id.detail_fragment_container);
        if(detailFragment != null) {
            if (detailFragment instanceof ContactDetailFragment) {
                ContactDetailFragment cdFragment = (ContactDetailFragment) detailFragment;
                cdFragment.updateUI();
            }
        }
    }

    /**
     * Method to remove the detail fragment; if it is present. This has the result of clearing the detail view.
     */
    public void removeDetailFragmentUI() {
        // Find the detail fragment; if it is present, then remove it from the fragment manager.
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.detail_fragment_container);
        if(fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    /**
     * This method is the callback when async tasks have finished.
     * @param contactEntry  The returned ContactEntry from the async task (may or may not be applicable, depending on the async task)
     * @param success   Should return true if the async task was successful.
     * @param taskId    The id of the async task that has completed.
     */
    @Override
    public void onAsyncTaskFinished(ContactEntry contactEntry, boolean success, int taskId) {
        if(success) {
            mCurrContactEntry = contactEntry;
            ContactStore.get(this).setTemporaryContact(mCurrContactEntry);
            // Start the EditDetailActivity
            Intent intent = ContactEditDetailActivity.newIntent(this,
                    mCurrContactEntry == null ? null : mCurrContactEntry.getId(), true);
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        }
        /*
        else {
            //TODO: Alert the user that parsing the business card failed for some reason
        }
        */
    }
}
