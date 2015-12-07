package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.util.ArrayList;
import java.util.UUID;

/**
 * ContactEditDetailFragment handles the view of all fields and actions for editing a single contact entry.
 *
 * @author Alex Pearson & Elizabeth Reed
 * Date:   December 6th, 2015
 */
public class ContactEditDetailFragment extends Fragment
        implements DialogAsyncTask.Callbacks {
    private static final String TAG = "ContactEditDetailFrgmt"; // logcat tag

    private static final int PICK_CONTACT_IMAGE_REQUEST = 1;    // contact photo request
    private static final int PICK_BC_IMAGE_REQUEST = 2;         // business card photo request

    private static final String ARG_CONTACT_ENTRY_ID = "arg_contact_entry_id";  // argument/extra for the id of the contact to be edited
    private static final String ARG_NEW_CONTACT = "arg_new_contact";            // argument/extra indicating a new contact

    private static final String KEY_CONTACT_ENTRY_ID = "key_contact_entry_id";  // saved instance key for the id of the contact being edited
    private static final String KEY_NEW_CONTACT = "key_new_contact";            // saved instance key indicating that the contact being edited is new

    private ContactEntry mContactEntry; // contact entry that is being edited
    private Callbacks mCallbacks;       // menu option item callbacks

    private UUID mContactEntryId;   // id of the contact entry being edited
    private boolean mNewContact;    // true if editing a new contact entry; false if editing an existing contact entry

    // async task objects
    private LoadContactTask mLct;   // "Load Contact" task
    private CommitContactTask mCct; // "Save Contact" task
    private BitmapLoaderAsyncTask mBlat;    // Task for loading the bitmaps

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

    /**
     * Creates a new contact detail fragment with the given parameters (the contact id and the flag for new contact)
     * attached as arguments.
     * @param contactEntryId    id of the contact entry to be edited
     * @param newContact        Set to true if the edited contact entry is new; set to false for an existing contact entry.
     * @return  the new contact edit detail fragment
     */
    public static ContactEditDetailFragment newInstance(UUID contactEntryId, boolean newContact) {
        Bundle args = new Bundle();
        // set the fragment arguments
        args.putSerializable(ARG_CONTACT_ENTRY_ID, contactEntryId);
        args.putBoolean(ARG_NEW_CONTACT, newContact);

        //Log.d(TAG, "ContactEditDetailFragment: ce_id="+contactEntryId);

        ContactEditDetailFragment fragment = new ContactEditDetailFragment();   // create new fragment
        fragment.setArguments(args);    // assign the arguments to the new fragment
        return fragment;
    }

    /**
     * On attaching the fragment, assign the menu item callbacks.
     * @param activity  calling activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        mCallbacks = (Callbacks) activity;
    }

    /**
     * When the fragment is created, need to obtain the contact entry id and the new contact flag
     * from either the saved instance state (if it exists) or from the arguments to the fragment.
     * @param savedInstanceState    saved instance parameters
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setHasOptionsMenu(true);
        // if the saved instance state exists, or the contact entry id from the saved instance state was not specified,
        // check if arguments were given to the fragment.
        if(savedInstanceState == null || savedInstanceState.getSerializable(KEY_CONTACT_ENTRY_ID) == null) {
            if (getArguments() != null) {
                // if there are arguments to the fragment, then obtain the contact entry id and new contact flag
                mContactEntryId = (UUID) getArguments().getSerializable(ARG_CONTACT_ENTRY_ID);
                mNewContact = getArguments().getBoolean(ARG_NEW_CONTACT, true);
                if (mContactEntryId == null && mNewContact) {
                    // if the contact id does not exist and it is a new contact,
                    mContactEntry = new ContactEntry(); // create a new contact entry
                    mContactEntryId = mContactEntry.getId();    // save the id of the new contact
                    // set this contact entry as the temporary contact in the ContactStore class
                    ContactStore.get(getActivity()).setTemporaryContact(mContactEntry);
                } else {
                    // otherwise, load the contact entry for the current entry id
                    // NOTE: In the event that a new contact is still being edited, but has already been created with an id,
                    //       it will enter this section as well, and load the contact entry data from this id
                    mLct = new LoadContactTask(getActivity(), this, mContactEntryId);
                    mLct.execute();
                }
            } else {
                Log.e(TAG,"No arguments passed in for some reason.");
            }
        } else {
            // Otherwise, if the saved instance state exists, get the contact entry id
            mContactEntryId = (UUID) savedInstanceState.getSerializable(KEY_CONTACT_ENTRY_ID);
            // load the temporary contact as the current contact entry
            mContactEntry = ContactStore.get(getActivity()).getTemporaryContact();
            mNewContact = savedInstanceState.getBoolean(KEY_NEW_CONTACT, true); // save the new contact flag state
            ContactStore.get(getActivity()).setTemporaryContact(mContactEntry); // set the current contact entry as the "temporary contact"
        }
    }

    /**
     * On pause of the activity, need to save the current values from the form layout to a contact entry.
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mContactEntry = buildContactEntryFromInput();   // save the values from the form to a new contact entry
        ContactStore.get(getActivity()).setTemporaryContact(mContactEntry); // and save it as a temporary contact entry
        // Note that the distinction of the temporary contact entry is required in both cases for new contacts and for existing
        // contact entries so that the changes the user has made to the current entry may be persisted when the fragment
        // is paused without changing the actual database copy (which should only be done when the entry is saved).
    }

    /**
     * Method for resuming the fragment. (Note: The loading of the edited contact entry is now handled only in onCreate().)
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Save the instance state
     * @param outState  instance state variables
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        outState.putSerializable(KEY_CONTACT_ENTRY_ID, mContactEntryId);    // save contact id
        outState.putBoolean(KEY_NEW_CONTACT, mNewContact);  // save new contact flag
    }

    /**
     * When the fragment is detached, remove the callbacks.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
        mCallbacks = null;
    }

    /**
     * When the view is destroyed, need to check status of async tasks...
     * if an async task is still running (hasn't finished), need to cancel
     * the task before destroying the fragment.
     */
    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        // check if the commit (save) contact async task is still running
        if(mCct != null && mCct.getStatus() != AsyncTask.Status.FINISHED) {
            mCct.cancel(true); // if so, cancel the operation of the async task
        }
        // check if the load contact entry async task is still running
        if(mLct != null && mLct.getStatus() != AsyncTask.Status.FINISHED) {
            mLct.cancel(true); // if so, cancel the operation of the async task
        }
        // check if the bitmap loader async task is still running
        if(mBlat != null && mBlat.getStatus() != AsyncTask.Status.FINISHED) {
            mBlat.cancel(true); // if so, cancel the operation of the async task
        }
        super.onDestroyView();
    }

    /**
     * Create the view for the fragment.
     * @param inflater  the LayoutInflater
     * @param container the ViewGroup
     * @param savedInstanceState    the saved state variables
     * @return  the inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.edit_contact_detail, container, false);

        // find the ImageView for the contact photo image
        mContactPhotoView = (ImageView) v.findViewById(R.id.ContactPicture);
        if (mContactPhotoView != null) {
            // if the ImageView exists, create a new onClickListener for the image view,
            // so that when clicked, the user can add/modify the saved image.
            mContactPhotoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // need to create an intent for the image chooser here
                    // (parameter indicates that the contact photo is being changed/specified)
                    openImageIntent(PICK_CONTACT_IMAGE_REQUEST);
                }
            });
        }

        // find the ImageView for the business card image
        mContactBCView = (ImageView) v.findViewById(R.id.ContactBusinessCardImg);
        if (mContactBCView != null) {
            // if the ImageView exists, create a new onClickListener for the image view,
            // so that when clicked, the user can add/modify the saved image.
            mContactBCView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // need to launch the chooser here for the business card image;
                    // added a parameter to distinguish between contact photo and business card
                    openImageIntent(PICK_BC_IMAGE_REQUEST);
                }
            });
        }

        // obtain all EditText objects
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

        // update the starting images/values for the ImageViews and TextViews
        updateUI();

        return v;
    }

    /**
     * Refresh the UI fields
     */
    public void updateUI() {
        Log.d(TAG, "updateUI");

        populateImageViews();   // set the ImageViews
        populateFieldViews();   // set the TextView values
    }

    /**
     * Populates all imageviews
     */
    private void populateImageViews() {
        if(mContactEntry != null) {
            // if the contact entry exists, obtain the stored image files here
            mContactPhotoFile = ContactStore.get(getActivity()).getPhotoFile(mContactEntry);
            mContactBCFile = ContactStore.get(getActivity()).getBCPhotoFile(mContactEntry);

            ArrayList<ImageView> imageViews = new ArrayList<>();
            ArrayList<String> paths = new ArrayList<>();
            if (mContactPhotoView != null && mContactPhotoFile != null && mContactPhotoFile.exists()) {
                // if the ImageView and the image file exist,
                // add the contact photo image view and path
                imageViews.add(mContactPhotoView);
                paths.add(mContactPhotoFile.getPath());
            }
            if (mContactBCView != null && mContactBCFile != null && mContactBCFile.exists()) {
                // if the ImageView and the image file exist,
                // add the business card image view and path
                imageViews.add(mContactBCView);
                paths.add(mContactBCFile.getPath());
            }
            // create a new async task to load the bitmaps
            mBlat = new BitmapLoaderAsyncTask(getActivity(), this,
                    imageViews.toArray(new ImageView[imageViews.size()]));
            mBlat.execute(paths.toArray(new String[paths.size()])); // start the async task
        }
    }

    /**
     * Populates the existing EditText widgets with values from the current contact entry.
     * For each text view, make sure that it exists, get the value from the contact entry,
     * and if the value is not null, update the text view.
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
            }
            if (mContactTitleEdit != null) {
                String titleVal = mContactEntry.getTitle();
                if (titleVal != null) {
                    mContactTitleEdit.setText(titleVal, TextView.BufferType.EDITABLE);
                }
            }

            if (mContactCompanyEdit != null) {
                String companyVal = mContactEntry.getCompany();
                if (companyVal != null) {
                    mContactCompanyEdit.setText(companyVal, TextView.BufferType.EDITABLE);
                }
            }

            if (mContactDepartmentEdit != null) {
                String departmentVal = mContactEntry.getDivision();
                if (departmentVal != null) {
                    mContactDepartmentEdit.setText(departmentVal, TextView.BufferType.EDITABLE);
                }
            }

            if (mContactPhoneNumEdit != null) {
                String phoneNumVal = mContactEntry.getPhoneNumber();
                if (phoneNumVal != null) {
                    mContactPhoneNumEdit.setText(phoneNumVal, TextView.BufferType.EDITABLE);
                }
            }

            if (mContactPhoneExtEdit != null) {
                String phoneExtVal = mContactEntry.getExtension();
                if (phoneExtVal != null) {
                    mContactPhoneExtEdit.setText(phoneExtVal, TextView.BufferType.EDITABLE);
                }
            }

            if (mContactFaxNumEdit != null) {
                String faxNumVal = mContactEntry.getFaxNumber();
                if (faxNumVal != null) {
                    mContactFaxNumEdit.setText(faxNumVal, TextView.BufferType.EDITABLE);
                }
            }

            if (mContactEmailEdit != null) {
                String emailVal = mContactEntry.getEmail();
                if (emailVal != null) {
                    mContactEmailEdit.setText(emailVal, TextView.BufferType.EDITABLE);
                }
            }

            if (mContactCompanyWebsiteEdit != null) {
                String companyWebsiteVal = mContactEntry.getWebsite();
                if (companyWebsiteVal != null) {
                    mContactCompanyWebsiteEdit.setText(companyWebsiteVal, TextView.BufferType.EDITABLE);
                }
            }

            if (mContactNotesEdit != null) {
                String notesVal = mContactEntry.getNotes();
                if (notesVal != null) {
                    mContactNotesEdit.setText(notesVal, TextView.BufferType.EDITABLE);
                }
            }
        }
    }

    /**
     * Create the options menu
     * @param menu      the menu object
     * @param inflater  the inflater object
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_edit_contact_detail, menu);    // add the edit contact detail options
    }

    /**
     * Method called when a menu item has been selected.
     * @param item  the selected menu action item
     * @return  Need to return true to display the menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.menu_item_save_changes:
                // User chose the "Save" item...

                // BEFORE allowing either the addition of a new contact, or the update of an existing contact,
                // need to verify that at least the name is not null...
                String strName = mContactNameEdit.getText().toString();
                //if ((strName == null) || (strName.equals(""))) {
                if (strName.trim().equals("")) {
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
                else {  // Otherwise, the contact name field is not null, so allow the contact to be added or updated.
                    // First, create the contact entry...
                    ContactEntry ce = buildContactEntryFromInput();
                    // If this is an "Add" operation, then add the new contact
                    if (mContactEntryId == null || mNewContact) {
                        // create an async task for adding a new contact to the database
                        mCct = new CommitContactTask(getActivity(), this, true, ce);
                        mCct.execute();
                    }
                    // Otherwise, it is a "Modify"/"Update" operation, so update the entry
                    else {
                        // create an async task for the update operation
                        mCct = new CommitContactTask(getActivity(), this, false, ce);
                        mCct.execute();
                    }
                }

                // hide the popup keyboard on the current view (if it is actively displayed)
                View v = getActivity().findViewById(android.R.id.content);
                hidePopUpKeyboard(getActivity(), v);

                return true;

            case R.id.menu_item_cancel_changes:
                // User chose the "Cancel" action (do not save! just return)
                // get confirmation from user in a dialog that they want to go back without saving changes
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Return without saving changes?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Yes, the user wants to exit, so close the activity
                        mCallbacks.onContactEntryCancelChanges(mContactEntry);
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel the closing of the activity here
                        dialog.cancel();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Build the contact entry from the current values in the EditText and ImageViews on the layout.
     * @return  A contact entry object with values loaded from the current state of the layout.
     */
    private ContactEntry buildContactEntryFromInput() {
        // use the contact entry builder to construct the new ContactEntry object
        return new ContactEntryBuilder(mContactEntry)
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
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);   // hide keyboard
    }

    /**
     * Method called when an AsyncTask is finished that returns only a single contact entry.
     * @param contactEntry  the singular ContactEntry object obtained in the async task
     * @param success       flag indicating success of async task
     * @param taskId        the id of the async task that completed
     */
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
                ContactStore.get(getActivity()).setTemporaryContact(mContactEntry); // note that it is the "temporary" contact entry
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

        /**
         * Constructor for the load contact async task
         * @param context   parent context
         * @param callbacks the callbacks for this async task (note: it will return one contact entry)
         * @param ceId      the id of the contact entry to be obtained from the database.
         */
        public LoadContactTask(Context context, Callbacks callbacks, UUID ceId) {
            super(INIT_STATUS_MSG,context,callbacks, TASK_ID);
            mContactEntryId = ceId;
        }

        /**
         * Task that needs to be performed in the background to load the contact entry data.
         * @param params    default parameters
         * @return          Returns true to indicate the task was successful.
         */
        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            // retrieve the contact entry data for the given id
            setContactEntry(ContactStore.get(getActivity()).getContactEntry(mContactEntryId));
            return true;
        }
    }

    /**
     * Async task class used to commit a set of values for a contact entry to the database.
     */
    private class CommitContactTask extends DialogAsyncTask<String, String, Boolean> {
        private boolean mNewContact;    // flag indicating if the given contact is a new contact

        // Assign a unique task id.
        public static final int TASK_ID = 2;
        // Define the status message for the spinning dialog
        private static final String INIT_STATUS_MSG = "Saving contact information...";

        /**
         * Constructor for the commit (save) contact async task
         * @param context       parent context
         * @param callbacks     the callbacks for this async task (note: it will return one contact entry)
         * @param isNewContact  true if saving a new contact, false if saving an existing contact
         * @param ce            contact entry to be saved
         */
        public CommitContactTask(Context context, Callbacks callbacks, boolean isNewContact, ContactEntry ce) {
            super(INIT_STATUS_MSG, context, callbacks, TASK_ID);
            mNewContact = isNewContact;
            setContactEntry(ce); // save the given contact entry
        }

        /**
         * Task that needs to be performed in the background to save the contact entry data.
         * @param params    default parameters
         * @return Returns true to indicate the task was successful.
         */
        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            // check if the contact entry is new
            if(mNewContact) {
                // add the new contact entry
                ContactEntry ce = getContactEntry();
                ContactStore.get(getActivity()).addContactEntry(ce);
            } else {
                // save the existing contact extry
                ContactEntry ce = getContactEntry();
                ContactStore.get(getActivity()).updateContactEntry(ce);
            }
            return true;
        }
    }

    /**
     * Open an image chooser.
     * Based on stackoverflow answer by David Manpearl and Austyn Mahoney
     * @param requestCode Used to indicate either a contact image or a business card image.
     */
    private void openImageIntent(int requestCode) {
        Log.d(TAG, "openImageIntent");
        Uri outputFileUri = null;
        String chooserText = "";
        File filesDir = getActivity()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if(requestCode == PICK_CONTACT_IMAGE_REQUEST) {
            // if requesting a contact photo image,
            // set the output file uri accordingly (get the photo file name for the current contact entry)
            outputFileUri = Uri.fromFile(new File( filesDir,
                    ContactStore.get(this.getActivity())
                            .getSuggestedPhotoFile(mContactEntry).getName() + ".jpg"));
            mContactPhotoFile = new File(outputFileUri.getPath());  // assign a new file for this path
            chooserText = getResources().getString(R.string.chooserContactImage);   // set the chooser dialog text
        } else if(requestCode == PICK_BC_IMAGE_REQUEST) {
            // if requesting a business card image,
            // set the output file uri accordingly (get the business card file name for the current contact entry)
            outputFileUri = Uri.fromFile(new File( filesDir,
                    ContactStore.get(this.getActivity())
                            .getSuggestedBCFile(mContactEntry).getName() + ".jpg"));
            mContactBCFile = new File(outputFileUri.getPath()); // assign a new file for this path
            chooserText = getResources().getString(R.string.chooserBCImage);    // set the chooser dialog text
        }

        // Build a list of Camera sources that could provide the correct data
        if(outputFileUri != null) {
            final Intent chooserIntent = PictureUtils.getImageChooserIntent(outputFileUri, chooserText, getActivity());

            if (chooserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // if the chooser intent was created correctly, launch the chooser activity
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
                // if no image file exists, display the default image.
                imgView.setImageResource(R.drawable.ic_add_a_photo_holo_light);
                // would potentially like to change the default photo image with themes;
                // so use a string here to reference the photo image?
                //imgView.setImageResource(getResources().getIdentifier(getResources().getString(R.string.default_photo_img), "drawable", PACKAGE_NAME ));
            } else {
                // Obtain a bitmap from the image file, and apply it to the view.
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

    /**
     * This method handles result values sent back to this activity from other activities.
     * @param requestCode   The code which indicates the activity returning the value (which image chooser returned the value).
     * @param resultCode    The result code (expect this to be RESULT_OK).
     * @param data          The returned data (i.e. the image returned by the image chooser/camera).
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            //Returned from the camera
            if(data == null) {
                if(requestCode == PICK_CONTACT_IMAGE_REQUEST) {
                    // contact photo image returned
                    mContactEntry.setPhotoFilePath(mContactPhotoFile.getPath());
                    updatePhotoView(mContactPhotoView, mContactPhotoFile);  // update the view
                } else if (requestCode == PICK_BC_IMAGE_REQUEST) {
                    // contact business card image returned
                    mContactEntry.setBCFilePath(mContactBCFile.getPath());
                    updatePhotoView(mContactBCView, mContactBCFile);    // update the view
                }
            }

            //Returned from the gallery
            if(data != null && data.getData() != null){
                Uri uri = data.getData();

                try {
                    // get a bitmap from the file
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity()
                            .getContentResolver(), uri);
                    // Log.d(TAG, String.valueOf(bitmap));
                    File filesDir = getActivity()
                            .getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                    if (requestCode == PICK_CONTACT_IMAGE_REQUEST) {
                        // store the contact photo
                        File tmpFile = PictureUtils.persistImage(filesDir, bitmap,
                                mContactEntry.getSuggestedPhotoFilename());
                        if (tmpFile != null) {
                            // if the file exists (is successfully saved)
                            // update the contact photo file and save the file path
                            mContactPhotoFile = tmpFile;
                            mContactEntry.setPhotoFilePath(tmpFile.getPath());
                            // then update the image view
                            updatePhotoView(mContactPhotoView, mContactPhotoFile);
                        }
                    } else if (requestCode == PICK_BC_IMAGE_REQUEST) {
                        // store the business card image
                        File tmpFile = PictureUtils.persistImage(filesDir, bitmap,
                                mContactEntry.getSuggestedBCFilename());
                        if (tmpFile != null) {
                            // if the file exists (is successfully saved)
                            // update the business card file and save the file path
                            mContactBCFile = tmpFile;
                            mContactEntry.setBCFilePath(tmpFile.getPath());
                            // then update the image view
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
