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

public class ContactListActivity extends AppCompatActivity
        implements ContactListFragment.Callbacks,
        ContactDetailFragment.Callbacks,
        OCRAsyncTask.Callbacks {

    private static final String TAG = "ContactListActivity";

    private static final int REQUEST_CODE_VIEW = 0;
    private static final int REQUEST_CODE_GET_IMAGE = 1;
    private static final int REQUEST_CODE_EDIT = 2;

    private static final String KEY_ENTRY_ID = "entry_id";  // used to save the id of the currently displayed contact entry detail

    private int mSelectedTheme;     // stores the index of the active theme
    private int mTmpSelectedTheme;  // stores the index of the selected theme in the settings dialog
    private Activity mActivity; // used to reference the current activity to correctly update the theme

    private ContactEntry mCurrContactEntry;
    private boolean mNeedUpdate = false;
    private boolean mNoRefresh = false;

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
            fragment = new ContactListFragment();
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
        /*
        if(savedInstanceState != null) {
            UUID currId = (UUID) savedInstanceState.getSerializable(KEY_ENTRY_ID);
            if(currId != null) {    // if there is a contact entry id, load the detail of that entry
                onContactSelected(ContactStore.get(this).getContactEntry(currId));
            }
        }
        */
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
        /*  // the following does NOT work when the activity is dismissed
        if (mCurrContactEntry != null) {    // if the current contact entry exists,
            outState.putSerializable(KEY_ENTRY_ID, mCurrContactEntry.getId());  // save the id of the currently displayed detail
        }
        */
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
                mNoRefresh = true;
                ContactListFragment clFragment = (ContactListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                clFragment.setNoRefresh(true);

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

        // Check to make sure the network is active.
        ConnectivityManager connMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMan.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            mCurrContactEntry = new ContactEntry();
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

    @Override
    public void onDisplaySettings() {
        mActivity = this;   // save reference to the current activity
        AlertDialog alert = createSettingsDialog();

        alert.show();

        // try setting the radio button here; find the radio group object
        RadioGroup colorSelectRadioGrp = (RadioGroup) alert.findViewById(R.id.RadioGrpSelectTheme);
        if (colorSelectRadioGrp != null) {
            //Log.d(TAG, "onDisplaySettings; mSelectedTheme: "+mSelectedTheme);
            switch (mSelectedTheme) {
                case 0:
                    colorSelectRadioGrp.check(R.id.radio_color_theme_1);  // set the corresponding radio button for the currently selected theme.
                    break;
                case 1:
                    colorSelectRadioGrp.check(R.id.radio_color_theme_2);  // set the corresponding radio button for the currently selected theme.
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
               /* // Original:
                // Specify the list array, the items to be selected by default (null for none),
                       // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(themeList, mSelectedTheme,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int activeItem) {
                                        mTmpSelectedTheme = activeItem;
                            }
                        })
                */
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

        /*
        AlertDialog alert = builder.create();

        //2. now setup to change color of the button
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(android:textColorLink);
            }
        };

        return alert;
        */
    }

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

    @Override
    public void onDisplayAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        //builder.setTitle(R.string.about_title);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.app_about, null))
                // Add action buttons
        //        .setPositiveButton(R.string.about_ok_btn, new DialogInterface.OnClickListener() {
        //            @Override
        //            public void onClick(DialogInterface dialog, int id) {
        //                // sign in the user ...
        //            }
        //        })
                ;
        AlertDialog alert = builder.create();
        alert.show();
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

        if(mNoRefresh) {
            mNoRefresh = false;
        } else {
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
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        }
        /*
        else {
            //TODO: Alert the user that parsing the business card failed for some reason
        }
        */
    }
}
