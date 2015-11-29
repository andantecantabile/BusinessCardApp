package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ContactEditDetailFragment extends Fragment
        implements DialogAsyncTask.Callbacks {
    private static final String TAG = "ContactEditDetailFrgmt";
    private static final String PACKAGE_NAME = "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact";

    private static final int PICK_CONTACT_IMAGE_REQUEST = 1;    // contact photo request
    private static final int PICK_BC_IMAGE_REQUEST = 2;         // business card photo request

    private static final String ARG_CONTACT_ENTRY_ID = "contact_entry_id";
    private static final String ARG_NEW_CONTACT = "new_contact";

    private ContactEntry mContactEntry;
    private Callbacks mCallbacks;

    private UUID mContactEntryId;
    private boolean mNewContact;

    // images
    private File mContactPhotoFile;
    private File mContactBCFile;

    // image views
    private ImageView mContactPhotoView;
    private ImageView mContactBCView;
    // edit fields in layout
    private EditText mContactNameEdit;
    private EditText mContactTitleEdit;
    private EditText mContactCompanyEdit;
    private EditText mContactDepartmentEdit;
    private EditText mContactPhoneNumEdit;
    private EditText mContactPhoneExtEdit;
    private EditText mContactFaxNumEdit;
    private EditText mContactEmailEdit;
    private EditText mContactCompanyWebsiteEdit;
    private EditText mContactNotesEdit;

    public interface Callbacks {
        void onContactEntrySaveChanges(ContactEntry ce);
        void onContactEntryCancelChanges(ContactEntry ce);
    }

    public static ContactEditDetailFragment newInstance(UUID contactEntryId, boolean newContact) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTACT_ENTRY_ID, contactEntryId);
        args.putBoolean(ARG_NEW_CONTACT, newContact);

        //Log.d(TAG, "ContactEditDetailFragment: ce_id="+contactEntryId);

        ContactEditDetailFragment fragment = new ContactEditDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mContactEntryId = (UUID) getArguments().getSerializable(ARG_CONTACT_ENTRY_ID);
            mNewContact = getArguments().getBoolean(ARG_NEW_CONTACT,true);
            if (mContactEntryId == null) {
                mContactEntry = new ContactEntry();
                mContactEntryId = mContactEntry.getId();
                ContactStore.get(getActivity()).setTemporaryContact(mContactEntry);
            }
            // previously used an else clause here to start the async task when the contact entry id was not null - moved to onResume()
        }
        else {
            mContactEntry = new ContactEntry();
            mContactEntryId = mContactEntry.getId();
            ContactStore.get(getActivity()).setTemporaryContact(mContactEntry);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        //Add an async task here to retrieve the data for the given contact id:
        if (mContactEntryId != null) {
            new LoadContactTask(getActivity(), this, mContactEntryId).execute();
        } else {
            mContactEntry = new ContactEntry();
            mContactEntryId = mContactEntry.getId();
            ContactStore.get(getActivity()).setTemporaryContact(mContactEntry);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
        mCallbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.edit_contact_detail, container, false);

        // contact photo image
        mContactPhotoView = (ImageView) v.findViewById(R.id.ContactPicture);
        if (mContactPhotoView != null) {
            mContactPhotoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // need to launch the chooser here
                    //pickImage(PICK_CONTACT_IMAGE_REQUEST);
                    openImageIntent(PICK_CONTACT_IMAGE_REQUEST);

                    // and then need to create a separate method to
                    // process the returned file from the activity
                    // and refresh the photo view - move later
                    //updatePhotoView(mContactPhotoView, mContactPhotoFile);
                }
            });
        }
        // business card image
        mContactBCView = (ImageView) v.findViewById(R.id.ContactBusinessCardImg);
        if (mContactBCView != null) {
            mContactBCView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // need to launch the chooser here for the business card image;
                    // added a parameter to pickImage() to distinguish between contact photo and business card
                    //pickImage(PICK_BC_IMAGE_REQUEST);
                    openImageIntent(PICK_BC_IMAGE_REQUEST);
                }
            });
        }

        // obtain textviews and populate all views
        mContactNameEdit = (EditText) v.findViewById(R.id.ContactNameVal);
        mContactTitleEdit = (EditText) v.findViewById(R.id.ContactTitleVal);
        mContactCompanyEdit = (EditText) v.findViewById(R.id.ContactCompanyVal);
        mContactDepartmentEdit = (EditText) v.findViewById(R.id.ContactDepartmentVal);
        mContactPhoneNumEdit = (EditText) v.findViewById(R.id.ContactPhoneNumVal);
        mContactPhoneExtEdit = (EditText) v.findViewById(R.id.ContactPhoneExtVal);
        mContactFaxNumEdit = (EditText) v.findViewById(R.id.ContactFaxNumVal);
        mContactEmailEdit = (EditText) v.findViewById(R.id.ContactEmailVal);
        mContactCompanyWebsiteEdit = (EditText) v.findViewById(R.id.ContactCompanyWebsiteVal);
        mContactNotesEdit = (EditText) v.findViewById(R.id.ContactNotesVal);

        updateUI();

        return v;
    }

    /**
     * Refresh the UI fields
     */
    public void updateUI() {
        Log.d(TAG, "updateUI");

        populateImageViews();
        populateFieldViews();
    }

    /**
     * Populates all imageviews
     */
    private void populateImageViews() {
        if (mContactEntry != null) {
            // need to obtain the existing contact entry and populate all of the EditText fields
            // with the existing data
            // obtain the photo here and then update the image view
            mContactPhotoFile = ContactStore.get(getActivity()).getPhotoFile(mContactEntry);
            // business card image
            mContactBCFile = ContactStore.get(getActivity()).getBCPhotoFile(mContactEntry);
        }

        updatePhotoView(mContactPhotoView, mContactPhotoFile);
        updatePhotoView(mContactBCView, mContactBCFile);
    }

    /**
     * Populates the existing EditText widgets
     */
    private void populateFieldViews() {
        // NOTE: need to check first that the contact entry is not null, before attempting to get values for the current contact entry.
        if (mContactEntry != null) {
            Log.d(TAG, "populateFieldViews");

            // populate all EditText widgets in the layout
            if (mContactNameEdit != null) {
                String nameVal = mContactEntry.getName();
                if (nameVal != null)    // make sure that return value is not null
                {
                    mContactNameEdit.setText(nameVal, TextView.BufferType.EDITABLE);
                }
                else
                    mContactNameEdit.setText("", TextView.BufferType.EDITABLE);
            }
            if (mContactTitleEdit != null) {
                String titleVal = mContactEntry.getTitle();
                if (titleVal != null)
                    mContactTitleEdit.setText(titleVal, TextView.BufferType.EDITABLE);
                else
                    mContactTitleEdit.setText("", TextView.BufferType.EDITABLE);
            }

            if (mContactCompanyEdit != null) {
                String companyVal = mContactEntry.getCompany();
                if (companyVal != null)
                    mContactCompanyEdit.setText(companyVal, TextView.BufferType.EDITABLE);
                else
                    mContactCompanyEdit.setText("", TextView.BufferType.EDITABLE);
            }

            if (mContactDepartmentEdit != null) {
                String departmentVal = mContactEntry.getDivision();
                if (departmentVal != null)
                    mContactDepartmentEdit.setText(departmentVal, TextView.BufferType.EDITABLE);
                else
                    mContactDepartmentEdit.setText("", TextView.BufferType.EDITABLE);
            }

            if (mContactPhoneNumEdit != null) {
                String phoneNumVal = mContactEntry.getPhoneNumber();
                if (phoneNumVal != null)
                    mContactPhoneNumEdit.setText(phoneNumVal, TextView.BufferType.EDITABLE);
                else
                    mContactPhoneNumEdit.setText("", TextView.BufferType.EDITABLE);
            }

            if (mContactPhoneExtEdit != null) {
                String phoneExtVal = mContactEntry.getExtension();
                if (phoneExtVal != null)
                    mContactPhoneExtEdit.setText(phoneExtVal, TextView.BufferType.EDITABLE);
                else
                    mContactPhoneExtEdit.setText("", TextView.BufferType.EDITABLE);
            }

            if (mContactFaxNumEdit != null) {
                String faxNumVal = mContactEntry.getFaxNumber();
                if (faxNumVal != null)
                    mContactFaxNumEdit.setText(faxNumVal, TextView.BufferType.EDITABLE);
                else
                    mContactFaxNumEdit.setText("", TextView.BufferType.EDITABLE);
            }

            if (mContactEmailEdit != null) {
                String emailVal = mContactEntry.getEmail();
                if (emailVal != null)
                    mContactEmailEdit.setText(emailVal, TextView.BufferType.EDITABLE);
                else
                    mContactEmailEdit.setText("", TextView.BufferType.EDITABLE);
            }

            if (mContactCompanyWebsiteEdit != null) {
                String companyWebsiteVal = mContactEntry.getWebsite();
                if (companyWebsiteVal != null)
                    mContactCompanyWebsiteEdit.setText(companyWebsiteVal, TextView.BufferType.EDITABLE);
                else
                    mContactCompanyWebsiteEdit.setText("", TextView.BufferType.EDITABLE);
            }

            if (mContactNotesEdit != null) {
                String notesVal = mContactEntry.getNotes();
                if (notesVal != null)
                    mContactNotesEdit.setText(notesVal, TextView.BufferType.EDITABLE);
                else
                    mContactNotesEdit.setText("", TextView.BufferType.EDITABLE);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_edit_contact_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.menu_item_save_changes:
                // User chose the "Save" item,
                // First, create the contact entry...
                ContactEntry ce = new ContactEntryBuilder(mContactEntry)
                        .name(mContactNameEdit.getText().toString())
                        .title(mContactTitleEdit.getText().toString())
                        .company(mContactCompanyEdit.getText().toString())
                        .division(mContactDepartmentEdit.getText().toString())
                        .phoneNumber(mContactPhoneNumEdit.getText().toString(), mContactPhoneExtEdit.getText().toString())
                        .faxNumber(mContactFaxNumEdit.getText().toString())
                        .email(mContactEmailEdit.getText().toString())
                        .website(mContactCompanyWebsiteEdit.getText().toString())
                        .notes(mContactNotesEdit.getText().toString())
                        .photo(mContactPhotoFile)
                        .businessCard(mContactBCFile)
                        .build();
                // If this is an "Add" operation, then add the new contact
                if (mContactEntryId == null || mNewContact) {
                    // BEFORE adding the new contact, need to verify that at least the name is not null...
                    String strName = ce.getName();
                    if ((strName == null) || (strName.equals(""))) {
                        // If the name is null, do NOT add the entry, and notify the user that the contact was not saved.
                        // get confirmation from user in a dialog that they want to go back without saving changes
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setTitle("Error: Could not save contact");
                        builder.setMessage("Contact name needs to be specified.");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Yes, the user wants to exit, so close the activity
                                dialog.cancel();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else {
                        // create an async task for adding a new contact to the database
                        new CommitContactTask(getActivity(),this, true, ce).execute();
                    }
                }
                // Otherwise, it is a "Modify"/"Update" operation, so update the entry
                else {
                    // create an async task for the update operation
                    new CommitContactTask(getActivity(),this, false, ce).execute();
                }

                View v = getActivity().findViewById(android.R.id.content);
                hidePopUpKeyboard(getActivity(), v);

                return true;

            case R.id.menu_item_cancel_changes:
                // User chose the "Cancel" action (do not save! just return)
                mCallbacks.onContactEntryCancelChanges(mContactEntry);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This helper function is used to hide the popup keyboard (on events such as save/cancel).
     * Code snippet from: http://stackoverflow.com/questions/18414804/android-edittext-remove-focus-after-clicking-a-button
     * @param activity  current activity
     * @param view  current view
     */
    public static void hidePopUpKeyboard(Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    @Override
    public void onAsyncTaskFinished(ContactEntry contactEntry, boolean success, int taskId) {
        //Log.d(TAG, "onAsyncTaskFinished; ceId: "+mContactEntryId);
        Log.d(TAG, "onAsyncTaskFinished");
        // When the async task to save a contact has been completed, need to verify success.
        // First, determine the identity of the async task which has completed.

        if (taskId == CommitContactTask.TASK_ID) {
            // if the Save (Update or Add Contact) Task is successful, then perform the callback to the activity.
            // If successful, the activity should be closed (occurs in ContactEditDetailActivity).
            // However, if not successful, then needs to NOT close the activity.
            if (success) {
                // save the contact entry locally:
                // Actually, no need to do this here, because on the following callback the activity will be destroyed.
                //mContactEntry = contactEntry;
                mCallbacks.onContactEntrySaveChanges(mContactEntry);
            }
        }
        else if (taskId == LoadContactTask.TASK_ID) {
            // if the Load Contact Task is successful, need to update the UI.
            if (success) {
                mContactEntry = contactEntry;   // save the loaded contact entry
                updateUI();
            }
        }
    }

    /**
     * Async task used to load a set of values for a contact entry from the database.
     */
    private class LoadContactTask extends DialogAsyncTask<String, String, Boolean> {
        private UUID mContactEntryId;    // the contact id to be loaded

        // Assign a unique task id.
        public static final int TASK_ID = 1;
        // Define the status message for the spinning dialog
        private static final String INIT_STATUS_MSG = "Loading contact information...";

        public LoadContactTask(Context context, Callbacks callbacks, UUID ceId) {
            super(INIT_STATUS_MSG,context,callbacks, TASK_ID);
            mContactEntryId = ceId;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            // retrieve the contact entry data for the given id
            setContactEntry(ContactStore.get(getActivity()).getContactEntry(mContactEntryId));
            return true;
        }
    }

    /**
     * Async task used to commit a set of values for a contact entry to the database.
     */
    private class CommitContactTask extends DialogAsyncTask<String, String, Boolean> {
        private boolean mNewContact;    // flag indicating if the given contact is a new contact

        // Assign a unique task id.
        public static final int TASK_ID = 2;
        // Define the status message for the spinning dialog
        private static final String INIT_STATUS_MSG = "Saving contact information...";

        public CommitContactTask(Context context, Callbacks callbacks, boolean isNewContact, ContactEntry ce) {
            super(INIT_STATUS_MSG, context, callbacks, TASK_ID);
            mNewContact = isNewContact;
            setContactEntry(ce); // save the given contact entry
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            if(mNewContact) {
                ContactEntry ce = getContactEntry();
                ContactStore.get(getActivity()).addContactEntry(ce);
            } else {
                ContactEntry ce = getContactEntry();
                ContactStore.get(getActivity()).updateContactEntry(ce);
            }
            return true;
        }
    }

    /**
     * Based on stackoverflow answer by David Manpearl and Austyn Mahoney
     * @param requestCode Used to indicate either a contact image or a business card image.
     */
    private void openImageIntent(int requestCode) {
        Log.d(TAG, "openImageIntent");
        Uri outputFileUri = null;
        String chooserText = "";
        if(requestCode == PICK_CONTACT_IMAGE_REQUEST) {
            outputFileUri = Uri.fromFile(ContactStore.get(this.getActivity()).getSuggestedPhotoFile(mContactEntry));
            chooserText = getResources().getString(R.string.chooserContactImage);
        } else if(requestCode == PICK_BC_IMAGE_REQUEST) {
            outputFileUri = Uri.fromFile(ContactStore.get(this.getActivity()).getSuggestedBCFile(mContactEntry));
            chooserText = getResources().getString(R.string.chooserBCImage);
        }

        // Build a list of Camera sources that could provide the correct data
        if(outputFileUri != null) {
            final Intent chooserIntent = PictureUtils.getImageChooserIntent(outputFileUri, chooserText, getActivity());

            if (chooserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(chooserIntent, requestCode);
            }
        }
    }

    /**
     *  Displays the provided image file in the referenced image view.
     */
    private void updatePhotoView(ImageView imgView, File imgFile) {
        Log.d(TAG, "updatePhotoView");
        if (imgView != null) {
            if (imgFile == null || !imgFile.exists()) {
                //imgView.setImageDrawable(null);   // would display no image.
                // instead, if no image file exists, display the default image.
                imgView.setImageResource(R.drawable.ic_add_a_photo_holo_light);
                // would potentially like to change the default photo image with themes;
                // so use a string here to reference the photo image.
                //imgView.setImageResource(getResources().getIdentifier(getResources().getString(R.string.default_photo_img), "drawable", PACKAGE_NAME ));
            } else {
                // Uncomment this section when PictureUtils is set up.
                Bitmap bitmap = PictureUtils.getScaledBitmap(
                        imgFile.getPath(), getActivity());
                Log.d(TAG, "Bitmap path: " + imgFile.getPath());
                if(!imgFile.exists()) {
                    Log.d(TAG, "Bitmap doesn't exist.");
                }
                imgView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            //Returned from the camera
            if(data == null) {
                if(requestCode == PICK_CONTACT_IMAGE_REQUEST) {
                    mContactPhotoFile = ContactStore.get(this.getActivity()).getSuggestedPhotoFile(mContactEntry);
                    mContactEntry.setPhotoFilePath(mContactPhotoFile.getPath());
                    updatePhotoView(mContactPhotoView, mContactPhotoFile);
                } else if (requestCode == PICK_BC_IMAGE_REQUEST) {
                    mContactBCFile = ContactStore.get(this.getActivity()).getSuggestedBCFile(mContactEntry);
                    mContactEntry.setBCFilePath(mContactBCFile.getPath());
                    updatePhotoView(mContactBCView, mContactBCFile);
                }
            }

            //Returned from the gallery
            if(data != null && data.getData() != null){
                Uri uri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity()
                            .getContentResolver(), uri);
                    // Log.d(TAG, String.valueOf(bitmap));
                    File filesDir = getActivity()
                            .getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                    if (requestCode == PICK_CONTACT_IMAGE_REQUEST) {
                        // update contact photo
                        File tmpFile = PictureUtils.persistImage(filesDir, bitmap,
                                mContactEntry.getSuggestedPhotoFilename());
                        if (tmpFile != null) {
                            mContactPhotoFile = tmpFile;
                            mContactEntry.setPhotoFilePath(tmpFile.getPath());
                            updatePhotoView(mContactPhotoView, mContactPhotoFile);
                        }
                    } else if (requestCode == PICK_BC_IMAGE_REQUEST) {
                        // update business card image
                        File tmpFile = PictureUtils.persistImage(filesDir, bitmap,
                                mContactEntry.getSuggestedBCFilename());
                        if (tmpFile != null) {
                            mContactBCFile = tmpFile;
                            mContactEntry.setBCFilePath(tmpFile.getPath());
                            updatePhotoView(mContactBCView, mContactBCFile);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
