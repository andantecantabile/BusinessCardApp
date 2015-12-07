package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * ContactListActivity is the initial startup activity, and must implement both the ContactListFragment
 * callbacks and ContactDetailFragment callbacks because it may operate in both portrait and
 * landscape mode.  In portrait mode, only the ContactListFragment will be displayed, while
 * in landscape mode, both ContactListFragment and ContactDetailFragment will be displayed.
 *
 * @author Alex Pearson & Elizabeth Reed
 * Date:   December 6th, 2015
 */
public class ContactListActivity extends AppCompatActivity
        implements ContactListFragment.Callbacks,
        ContactDetailFragment.Callbacks,
        OCRAsyncTask.Callbacks {

    private static final String TAG = "ContactListActivity";    // identifier for the activity in logcat

    private static final int REQUEST_CODE_VIEW = 0;         // code sent to DetailActivity
    private static final int REQUEST_CODE_GET_IMAGE = 1;    // code sent to the image chooser
    private static final int REQUEST_CODE_EDIT = 2;         // code sent to EditDetailActivity

    private static final String KEY_ENTRY_ID = "entry_id";  // used to save the id of the currently displayed contact entry detail

    private int mSelectedTheme;     // stores the index of the active theme
    private int mTmpSelectedTheme;  // stores the index of the selected theme in the settings dialog
    private Activity mActivity; // used to reference the current activity to correctly update the theme

    private ContactEntry mCurrContactEntry; // contact entry that is currently selected
    private boolean mNeedUpdate = false;    // a flag used on return from EditDetailActivity to indicate that an update is required for the detail display
    private boolean mNoRefresh = false;     // a flag used to skip refresh of the list and detail views on the event
                                            // that a business card image has been returned from the image chooser, and
                                            // will be immediately used in a call to the OCRAsyncTask. (Since after the async
                                            // task is complete, it will immediately launch the EditDetailActivity, there is
                                            // no need to refresh the views at this time.

    /**
     * Obtains the corresponding layout id based on the current orientation of the device.
     * @return  id of the active layout
     */
    @LayoutRes
        private int getLayoutResId() {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return R.layout.activity_twopane;
        } else {
            return R.layout.activity_fragment;
        }
    }

    /**
     * Create the activity
     * @param savedInstanceState    the saved instance state (may include the id of the contact entry that should be currently selected).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Obtain the previously saved color theme preference, and load the activity with that theme.
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int defaultColorTheme = SettingsUtils.DEFAULT_COLOR_THEME;
        int colorTheme = sharedPref.getInt(getString(R.string.saved_color_theme), defaultColorTheme);
        SettingsUtils.setActiveTheme(colorTheme);
        SettingsUtils.onActivityCreateSetTheme(this);   // set the activity theme

        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();

        //Check if the id for placing the ContactListFragment in exists
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if(fragment == null) {
            fragment = new ContactListFragment();   // add the contact list fragment
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        // check if there is a contact entry id saved
        String currIdStr = sharedPref.getString(getString(R.string.saved_ce_uuid), null);
        //if ((currIdStr != null) && !(currIdStr.equals("")) && !(currIdStr.equals("null"))) { // verify that the currId exists/is not null
        if ((currIdStr != null) && !(currIdStr.equals(""))) { // verify that the currId exists/is not null
            UUID currId = UUID.fromString(currIdStr);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_ce_uuid), null);  // put null in the saved uuid to reset it; only want to maintain on dismiss from settings
            editor.commit();
            onContactSelected(ContactStore.get(this).getContactEntry(currId));
        }

        if(savedInstanceState != null) {
            UUID currId = (UUID) savedInstanceState.getSerializable(KEY_ENTRY_ID);
            if(currId != null) {    // if there is a contact entry id, load the detail of that entry
                mCurrContactEntry = ContactStore.get(this).getContactEntry(currId);
                if(mCurrContactEntry != null) {
                    onContactSelected(mCurrContactEntry);
                }
            }
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
        // the following does NOT work when the activity is dismissed
        if (mCurrContactEntry != null) {    // if the current contact entry exists,
            outState.putSerializable(KEY_ENTRY_ID, mCurrContactEntry.getId());  // save the id of the currently displayed detail
        }
        else {
            // otherwise clear the saved instance state
            outState.putSerializable(KEY_ENTRY_ID,null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*  // The following was not working consistently in this location (might need to be before calling the super method.
            // Also, only want to save the currently selected entry if the settings are being changed --> so moved to just before the settings dialog is created.
        // Save the currently selected entry, if one is present
        if (mCurrContactEntry != null) {
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_ce_uuid), mCurrContactEntry.getId().toString());
            editor.commit();
        }
        */
        Log.d(TAG, "onDestroy");

    }

    /**
     * This method handles the situation when a contact entry has been
     * selected in the contact list fragment.
     * @param ce   the contact entry which has been selected
     */
    @Override
    public void onContactSelected(ContactEntry ce) {
        Log.d(TAG, "onContactSelected");
        //Check if the id for placing the ContactDetailFragment in exists
        if(findViewById(R.id.detail_fragment_container) == null) {
            // if the container for the detail fragment does not exist,
            // launch the contact detail activity
            Intent intent = ContactDetailActivity.newIntent(this, ce.getId());
            startActivityForResult(intent, REQUEST_CODE_VIEW);
        } else {
            // otherwise, if the container for the detail fragment exists in the current layout,
            // set the detail fragment with the currently selected contact entry if there is
            // one active.
            if(mCurrContactEntry == null || !mCurrContactEntry.getId().equals(ce.getId())) {
                mCurrContactEntry = ce;
                setDetailFragment(ce);
            }
        }
    }

    /**
     * This method handles result values sent back to this activity from other activities.
     * @param requestCode   The code which indicates the activity returning the value.
     * @param resultCode    The result code (expect this to be RESULT_OK).
     * @param data          The returned data (i.e. id of contact entry last viewed or edited, or the image returned by the image chooser/camera).
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if(resultCode != RESULT_OK) {
            mCurrContactEntry = null;
            removeDetailFragmentUI();   // if for some reason the result code is not ok, just remove the detail fragment; only the list fragment will be displayed
            return;
        }
        if(requestCode == REQUEST_CODE_VIEW) {  // indicates return from ContactDetailActivity  (handles rotation from portrait to landscape mode)
            UUID currID = ContactDetailActivity.lastViewedID(data); // get the id of the contact entry last viewed
            if(currID != null) {    // if the id exists,
                ContactEntry ce = ContactStore.get(this).getContactEntry(currID);   // obtain the contact entry
                if (findViewById(R.id.detail_fragment_container) != null) { // check that the detail_fragment_container exists; i.e. is in landscape mode
                    mCurrContactEntry = ce; // assign the currently viewed contact entry
                    setDetailFragment(ce);  // display the contact entry in the detail fragment view
                }
            }
        } else if(requestCode == REQUEST_CODE_EDIT) {   // indicates return from ContactEditDetailActivity
            UUID currID = ContactEditDetailActivity.lastEditedId(data); // obtain id of the edited contact entry
            if(currID != null) {    // if it exists,
                mCurrContactEntry = ContactStore.get(this).getContactEntry(currID); // assign the currently viewed contact entry
                mNeedUpdate = true; // and indicate that the detail fragment view should be updated on resume
            }
        } else if(requestCode == REQUEST_CODE_GET_IMAGE) {  // indicates return from the image chooser
            File BCFile = null;

            // Returned from the camera
            if(data == null) {
                File filesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                // save the business card image to a file with the filename associated with the current entry
                BCFile = new File( filesDir, ContactStore.get(this)
                            .getSuggestedBCFile(mCurrContactEntry).getName() + ".jpg");
            }
            // Returned from the gallery
            if(data != null && data.getData() != null) {
                Uri uri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this
                            .getContentResolver(), uri);
                    // Log.d(TAG, String.valueOf(bitmap));
                    File filesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                    // save the business card image to file
                    File tmpFile = PictureUtils.persistImage(filesDir, bitmap,
                            mCurrContactEntry.getSuggestedBCFilename());
                    if (tmpFile != null) {  // make sure that the file save was successful
                        BCFile = tmpFile;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // if a business card image (from either camera or gallery) was successfully saved to a file,
            if(BCFile != null) {
                Log.d(TAG, "onActivityResult: BCFile successfully saved");
                mNoRefresh = true;  // set the noRefresh flag so that the activity list and detail views will not be updated on resume
                                    // (will be calling the OCRAsyncTask here, and on return, the edit activity will be launched)
                ContactListFragment clFragment = (ContactListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                clFragment.setNoRefresh(true);  // list fragment should not be refreshed

                String tempFile = mCurrContactEntry.getId() + ".xml";
                OCRAsyncTask parseBC = new OCRAsyncTask("Beginning Upload..", this, this);  // start the async task to connect to the OCR API
                parseBC.execute(BCFile.getPath(), tempFile);    // provide the path to the image file, and the xml file that results should be written to.
            }
            /*
            else {
                Log.d(TAG, "onActivityResult: BCFile not successfully saved");
                mCurrContactEntry = null;   // need to clear the current temporary contact entry if the file was not saved.
            }
            */
        }
    }

    /**
     * Create a new detail fragment with the given contact entry and
     * attach it to the detail_fragment_container.
     * @param ce    the contact entry to be displayed
     */
    private void setDetailFragment(ContactEntry ce) {
        Fragment newDetail = ContactDetailFragment.newInstance(ce.getId()); // create the detail fragment

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_fragment_container, newDetail) // put it in the layout
                .commit();
    }

    /**
     * Handles the "Edit" menu action item functionality.
     * @param ce    contact entry (currently selected) to be edited
     */
    @Override
    public void onContactEntryEdit(ContactEntry ce) {
        Log.d(TAG, "onContactEntryEdit");

        // start the edit detail activity
        mCurrContactEntry = ce;
        Intent intent = ContactEditDetailActivity.newIntent(this, ce.getId(), false);   // send the id of the contact entry
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    /**
     * Handles the "Delete" menu action item functionality.
     * @param ce    contact entry (currently selected) to be deleted
     */
    @Override
    public void onContactEntryDelete(ContactEntry ce) {
        Log.d(TAG, "onContactEntryDelete");

        // Need to update the views of the fragments.
        updateListFragmentUI();     // update list view
        removeDetailFragmentUI();   // clear detail view
    }

    /**
     * Handles the "Add Blank Contact" menu action item functionality.
     */
    @Override
    public void onAddBlankContact() {
        Log.d(TAG, "onContactAddBlank");
        // Start the EditDetailActivity
        Intent intent = ContactEditDetailActivity.newIntent(this, null, true);
        // Note: Second argument sent in the intent (the contact entry id) is null because a new id will need to be created.
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    /**
     * Handles the "Add New Contact Card" menu action item functionality.
     */
    @Override
    public void onAddNewContactCard() {
        Log.d(TAG, "onContactAddNewContactCard");

        // Check to make sure the network is active.
        ConnectivityManager connMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMan.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            mCurrContactEntry = new ContactEntry();
            ContactStore.get(this).setTemporaryContact(mCurrContactEntry);  // set temporary contact here
            File filesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            Uri outputFileUri = Uri.fromFile(new File(filesDir, ContactStore.get(this)
                    .getSuggestedBCFile(mCurrContactEntry).getName() + ".jpg"));
            String chooserText = getResources().getString(R.string.chooserBCImage);
            Intent intent = PictureUtils.getImageChooserIntent(outputFileUri, chooserText, this);
            startActivityForResult(intent, REQUEST_CODE_GET_IMAGE);
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.action_unavailable)
                    .setMessage(R.string.network_not_available)
                    .setPositiveButton(R.string.okay, null)
                    .create();
            alertDialog.show();
        }
    }

    /**
     * Handles the "Settings" menu action item functionality.
     */
    @Override
    public void onDisplaySettings() {
        mActivity = this;   // save reference to the current activity
        AlertDialog alert = createSettingsDialog(); // create the settings dialog

        alert.show();   // display the dialog

        // set the radio button here based on the currently active theme; find the radio group object
        RadioGroup colorSelectRadioGrp = (RadioGroup) alert.findViewById(R.id.RadioGrpSelectTheme);
        if (colorSelectRadioGrp != null) {
            //Log.d(TAG, "onDisplaySettings; mSelectedTheme: "+mSelectedTheme);
            switch (mSelectedTheme) {
                case 0:
                    colorSelectRadioGrp.check(R.id.radio_color_theme_1);  // set the corresponding radio button for the currently selected theme.
                    break;
                case 1:
                    colorSelectRadioGrp.check(R.id.radio_color_theme_2);
                    break;
                case 2:
                    colorSelectRadioGrp.check(R.id.radio_color_theme_3);
                    break;
                case 3:
                    colorSelectRadioGrp.check(R.id.radio_color_theme_4);
                    break;
                case 4:
                    colorSelectRadioGrp.check(R.id.radio_color_theme_5);
                    break;
                case 5:
                    colorSelectRadioGrp.check(R.id.radio_color_theme_6);
                    break;
                case 6:
                    colorSelectRadioGrp.check(R.id.radio_color_theme_7);
                    break;
                case 7:
                default:
                    colorSelectRadioGrp.check(R.id.radio_color_theme_8);
                    break;
            }
        }
    }

    /**
     * This method creates the dialog to select the Color Theme setting for the application (the selected theme will be applied to all activities).
     * @return  the settings dialog for color theme selection
     */
    public AlertDialog createSettingsDialog() {
        // Save the currently selected entry, if one is present
        if (mCurrContactEntry != null) {
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_ce_uuid), mCurrContactEntry.getId().toString());
            editor.commit();
        }

        mSelectedTheme = SettingsUtils.getActiveTheme();    // load the index of the active theme
        mTmpSelectedTheme = mSelectedTheme; // set temporary selected theme to be the same as the active theme initially
        String[] themeList = SettingsUtils.getThemeListStr();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Set the dialog title
        builder.setTitle(R.string.settings_title)
                .setView(inflater.inflate(R.layout.settings_dialog, null))
                // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if the selected theme is different from the original active theme, need to update the display and save the new theme choice
                        if (mSelectedTheme != mTmpSelectedTheme) {
                            mSelectedTheme = mTmpSelectedTheme; // save the newly selected theme
                            SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt(getString(R.string.saved_color_theme), mSelectedTheme);
                            editor.commit();
                            SettingsUtils.changeToTheme(mActivity, mSelectedTheme);   // update the current activity
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mTmpSelectedTheme = mSelectedTheme; // no update
                    }
                });

        return builder.create();
    }

    /**
     * Method called onClick of radio buttons in the Color Settings radio group.
     * @param view  the current view
     */
    public void onColorThemeRadioBtnClicked(View view) {
        // handle radio button selection here
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked, and store the corresponding value of the currently selected theme.
        switch(view.getId()) {
            case R.id.radio_color_theme_1:
                if (checked)
                    mTmpSelectedTheme = 0;
                    break;
            case R.id.radio_color_theme_2:
                if (checked)
                    mTmpSelectedTheme = 1;
                    break;
            case R.id.radio_color_theme_3:
                if (checked)
                    mTmpSelectedTheme = 2;
                break;
            case R.id.radio_color_theme_4:
                if (checked)
                    mTmpSelectedTheme = 3;
                break;
            case R.id.radio_color_theme_5:
                if (checked)
                    mTmpSelectedTheme = 4;
                break;
            case R.id.radio_color_theme_6:
                if (checked)
                    mTmpSelectedTheme = 5;
                break;
            case R.id.radio_color_theme_7:
                if (checked)
                    mTmpSelectedTheme = 6;
                break;
            case R.id.radio_color_theme_8:
                if (checked)
                    mTmpSelectedTheme = 7;
                break;
        }
    }

    /**
     * Handles the "About" menu action item functionality.
     */
    @Override
    public void onDisplayAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.app_about, null));
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Refresh of the fragment views is handled on resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if(mNeedUpdate) {   // if an update is flagged on resume, set the detail fragment with the currently saved contact entry
            if (findViewById(R.id.detail_fragment_container) != null) {
                setDetailFragment(mCurrContactEntry);
            }
            mNeedUpdate = false;    // and then clear the flag
        }

        if(mNoRefresh) {    // if the flag is set to skip refresh on resume,
            mNoRefresh = false; // clear the flag
        } else {    // otherwise, need to refresh:
            // Need to update the views of the fragments.
            updateListFragmentUI();
            updateDetailFragmentUI();
        }
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
            // If there is a selected contact entry, send the id of the contact entry in the intent,
            // otherwise, send null in the intent.
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        }
    }
}
