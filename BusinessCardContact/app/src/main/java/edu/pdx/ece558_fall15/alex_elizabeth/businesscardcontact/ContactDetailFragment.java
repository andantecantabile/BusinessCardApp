package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

/**
 * ContactDetailFragment handles the view of all fields for a single contact entry.
 *
 * Authors: Alex Pearson and Elizabeth Reed
 * Date:    December 6th, 2015
 */
public class ContactDetailFragment extends Fragment
        implements DialogAsyncTask.Callbacks {
    private static final String TAG = "ContactDetailFragment";  // tag for logcat

    private static final String ARG_CONTACT_ENTRY_ID = "contact_entry_id";

    private ContactEntry mContactEntry; // the contact entry to be displayed
    private Callbacks mCallbacks;       // the callbacks for the menu action items

    private UUID mContactEntryId;   // the id of the displayed contact entry

    private BitmapLoaderAsyncTask mBlat;    // async task for loading the bitmaps
    private LoadContactTask mLct;           // async task for loading the contact entry to be displayed
    private DeleteContactTask mDct;         // async task for deleting the contact entry

    // files for the contact photo and business card images
    private File mContactPhotoFile;
    private File mContactBCFile;

    // views in layout
    private ImageView mContactPhotoView;
    private TextView mContactNameView;
    private TextView mContactTitleView;
    private TextView mContactCompanyView;
    private TextView mContactDepartmentView;
    private TextView mContactPhoneNumView;
    private TextView mContactPhoneExtView;
    private TextView mContactFaxNumView;
    private TextView mContactEmailView;
    private TextView mContactCompanyWebsiteView;
    private TextView mContactNotesView;
    private ImageView mContactBCView;

    public interface Callbacks {
        void onContactEntryEdit(ContactEntry ce);   // "Edit" menu action item callback
        void onContactEntryDelete(ContactEntry ce); // "Delete" menu action item callback
    }

    /**
     * Creates a new contact detail fragment with the given contact entry id attached as an argument.
     * @param contactEntryId    the id of the contact entry to be displayed
     * @return  the new contact detail fragment
     */
    public static ContactDetailFragment newInstance(UUID contactEntryId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTACT_ENTRY_ID, contactEntryId);

        ContactDetailFragment fragment = new ContactDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * When the contact detail fragment is attached, assign the activity callbacks.
     * @param activity  the calling activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        mCallbacks = (Callbacks) activity;  // assign the activity callbacks
    }

    /**
     * On create, obtain the contact entry id argument, and get the data for the specified contact entry.
     * @param savedInstanceState    the saved instance variables
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setHasOptionsMenu(true);    // flag that the options menu should be used
        mContactEntryId = (UUID) getArguments().getSerializable(ARG_CONTACT_ENTRY_ID);

        // Get the current contact entry
        mContactEntry = ContactStore.get(getActivity()).getContactEntry(mContactEntryId);
    }

    /**
     * On Pause, no special actions.
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    /**
     * On resume, need to reload the current contact.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // Use an async task here to retrieve the data for the current contact id:
        if (mContactEntryId != null) {
            mLct = new LoadContactTask(getActivity(), this, mContactEntryId);
            mLct.execute();
        }
        // update the layout view
        updateUI();
    }

    /**
     * Save the instance state
     * @param outState  instance state variables
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        // check if the bitmap loader async task is still running
        if(mBlat != null && mBlat.getStatus() != AsyncTask.Status.FINISHED) {
            mBlat.cancel(true); // if so, cancel the operation of the async task
        }
        // check if the load contact entry async task is still running
        if(mLct != null && mLct.getStatus() != AsyncTask.Status.FINISHED) {
            mLct.cancel(true); // if so, cancel the operation of the async task
        }
        // check if the delete contact entry async task is still running
        if(mDct != null && mDct.getStatus() != AsyncTask.Status.FINISHED) {
            mDct.cancel(true); // if so, cancel the operation of the async task
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
        View v = inflater.inflate(R.layout.contact_detail, container, false);

        // obtain contact photo image view
        mContactPhotoView = (ImageView) v.findViewById(R.id.ContactPicture);
        // obtain business card image view
        mContactBCView = (ImageView) v.findViewById(R.id.ContactBusinessCardImg);
        // populate imageviews
        populateImageViews();

        // obtain textviews and populate all views
        mContactNameView = (TextView) v.findViewById(R.id.ContactNameVal);
        mContactTitleView = (TextView) v.findViewById(R.id.ContactTitleVal);
        mContactCompanyView = (TextView) v.findViewById(R.id.ContactCompanyVal);
        mContactDepartmentView = (TextView) v.findViewById(R.id.ContactDepartmentVal);
        mContactPhoneNumView = (TextView) v.findViewById(R.id.ContactPhoneNumVal);
        mContactPhoneExtView = (TextView) v.findViewById(R.id.ContactPhoneExtVal);
        mContactFaxNumView = (TextView) v.findViewById(R.id.ContactFaxNumVal);
        mContactEmailView = (TextView) v.findViewById(R.id.ContactEmailVal);
        mContactCompanyWebsiteView = (TextView) v.findViewById(R.id.ContactCompanyWebsiteVal);
        mContactNotesView = (TextView) v.findViewById(R.id.ContactNotesVal);
        // populates text field views
        populateFieldViews();

        return v;
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
        inflater.inflate(R.menu.menu_contact_detail, menu); // add the contact detail menu options ("Edit" and "Delete")
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
            case R.id.menu_item_edit_contact:
                // User chose the "Edit Contact" item, perform callback...
                mCallbacks.onContactEntryEdit(mContactEntry);
                return true;

            case R.id.menu_item_delete_contact:
                // User chose the "Delete Contact" action
                deleteContactCheck();   // need to check for confirmation that the contact should be deleted; dialog will be displayed
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Displays the provided image file in the referenced image view.
     * This method will convert the given image file to a bitmap and then use it to update
     * the given ImageView.
     * Note that BOTH the ImageView and the image file are passed to this method
     * to allow the method to be used for both the contact image and the contact
     * business card image.
     * @param imgView   The ImageView object that needs to be updated (view where the given image file should be displayed).
     * @param imgFile   The image file.
     */
    private void updatePhotoView(ImageView imgView, File imgFile) {
        // Make sure the given ImageView exists in the layout
        if (imgView != null) {
            // Check to see if the specified image file exists...
            if (imgFile == null || !imgFile.exists()) {
                // If no image file exists, display the default image.
                imgView.setImageResource(R.drawable.ic_photo_camera_light);
            } else {
                // Obtain a bitmap from the image file, and apply it to the view.
                Bitmap bitmap = PictureUtils.getScaledBitmap(
                    imgFile.getPath(), getActivity());
                imgView.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * Update the image views in the layout (contact photo and business card images)
     * and the text views for all the fields in the contact entry.
     */
    public void updateUI() {
        Log.d(TAG, "updateUI");
        populateImageViews();   // update photo and business card views
        populateFieldViews();   // update all text fields
    }

    /**
     * This method checks for existence of the ImageViews and calls an async task to update all ImageViews.
     */
    private void populateImageViews() {
        // get the photo file stored for the currently selected contact entry.
        mContactPhotoFile = ContactStore.get(getActivity()).getPhotoFile(mContactEntry);
        // get the business card file stored for the currently selected contact entry.
        mContactBCFile = ContactStore.get(getActivity()).getBCPhotoFile(mContactEntry);

        ArrayList<ImageView> imageViews = new ArrayList<>();
        ArrayList<String> paths = new ArrayList<>();
        // add the contact photo image view
        if(mContactPhotoView != null && mContactPhotoFile != null && mContactPhotoFile.exists()) {
            imageViews.add(mContactPhotoView);
            paths.add(mContactPhotoFile.getPath());
        }
        // add the business card image view
        if(mContactBCView != null && mContactBCFile != null && mContactBCFile.exists()) {
            imageViews.add(mContactBCView);
            paths.add(mContactBCFile.getPath());
        }
        // create a new async task to load the bitmaps
        mBlat = new BitmapLoaderAsyncTask(getActivity(), this,
                imageViews.toArray(new ImageView[imageViews.size()]));
        mBlat.execute(paths.toArray(new String[paths.size()]));    // start the async task
    }

    /**
     * Populates the existing textviews with values from the current contact entry.
     * For each text view, make sure that it exists, get the value from the contact entry,
     * and if the value is not null, update the text view.
     */
    private void populateFieldViews() {
        // populate all textviews
        if (mContactNameView != null) {
            String nameVal = mContactEntry.getName();
            if (nameVal != null)    // make sure that return value is not null
                mContactNameView.setText(nameVal);
            else
                mContactNameView.setText("");
        }

        if (mContactTitleView != null) {
            String titleVal = mContactEntry.getTitle();
            if (titleVal != null)
                mContactTitleView.setText(titleVal);
            else
                mContactTitleView.setText("");
        }

        if (mContactCompanyView != null) {
            String companyVal = mContactEntry.getCompany();
            if (companyVal != null)
                mContactCompanyView.setText(companyVal);
            else
                mContactCompanyView.setText("");
        }

        if (mContactDepartmentView != null) {
            String departmentVal = mContactEntry.getDivision();
            if (departmentVal != null)
                mContactDepartmentView.setText(departmentVal);
            else
                mContactDepartmentView.setText("");
        }

        if (mContactPhoneNumView != null) {
            String phoneNumVal = mContactEntry.getPhoneNumber();
            if (phoneNumVal != null)
                mContactPhoneNumView.setText(phoneNumVal, TextView.BufferType.EDITABLE);
            else
                mContactPhoneNumView.setText("", TextView.BufferType.EDITABLE);
        }

        if (mContactPhoneExtView != null) {
            String phoneExtVal = mContactEntry.getExtension();
            if (phoneExtVal != null)
                mContactPhoneExtView.setText(phoneExtVal, TextView.BufferType.EDITABLE);
            else
                mContactPhoneExtView.setText("", TextView.BufferType.EDITABLE);
        }

        if (mContactFaxNumView != null) {
            String faxNumVal = mContactEntry.getFaxNumber();
            if (faxNumVal != null)
                mContactFaxNumView.setText(faxNumVal, TextView.BufferType.EDITABLE);
            else
                mContactFaxNumView.setText("", TextView.BufferType.EDITABLE);
        }

        if (mContactEmailView != null) {
            String emailVal = mContactEntry.getEmail();
            if (emailVal != null)
                mContactEmailView.setText(emailVal, TextView.BufferType.EDITABLE);
            else
                mContactEmailView.setText("", TextView.BufferType.EDITABLE);
        }

        if (mContactCompanyWebsiteView != null) {
            String companyWebsiteVal = mContactEntry.getWebsite();
            if (companyWebsiteVal != null)
                mContactCompanyWebsiteView.setText(companyWebsiteVal, TextView.BufferType.EDITABLE);
            else
                mContactCompanyWebsiteView.setText("", TextView.BufferType.EDITABLE);
        }

        if (mContactNotesView != null) {
            String notesVal = mContactEntry.getNotes();
            if (notesVal != null)
                mContactNotesView.setText(notesVal, TextView.BufferType.EDITABLE);
            else
                mContactNotesView.setText("", TextView.BufferType.EDITABLE);
        }
    }

    /**
     * Method that starts the async task to actually delete the active contact.
     */
    private void deleteContact() {
        // so, delete the currently selected contact entry - needs to be done in an async task
        mDct = new DeleteContactTask(getActivity(), this, mContactEntry);
        mDct.execute();
    }

    /**
     * Wrapper method to first confirm that the user wants to delete the active contact, before allowing it to be deleted.
     */
    private void deleteContactCheck() {
        // get confirmation from user in a dialog that they want to go back without saving changes
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Delete this contact?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Yes, the user wants to delete the contact...
                deleteContact();
                // Note: after deletion, need to return to the list view...
                // but this will be handled by the activity callback.
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
        alert.show();   // show the delete confirmation dialog
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
        // First, determine the identity of the async task which has completed.
        if (taskId == LoadContactTask.TASK_ID) {
            // When the async task to load a contact entry from the database has been completed, need to verify success.
            if (success) {
                // if the Load Contact Task is successful, need to update the UI.
                mContactEntry = contactEntry;   // save the loaded contact entry
                updateUI();
            }
        }
        else if (taskId == DeleteContactTask.TASK_ID) {
            // When the async task to delete a contact from the database has been completed, need to verify success.
            if (success) {
                // once the contact is successfully deleted, just need to perform the callback to the activity so that the activity will be closed.
                mCallbacks.onContactEntryDelete(mContactEntry);
            }
            // if it was not successful, would probably be good to display some message... but this case is very unlikely to happen
        }
        // NOTE: nothing to do for (taskId == BitmapLoaderAsyncTask.TASK_ID)
    }

    /**
     * Async task used to commit a set of values for a contact entry to the database.
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
            // retrieve the contact entry data for the id of the entry to be displayed
            setContactEntry(ContactStore.get(getActivity()).getContactEntry(mContactEntryId));
            return true;
        }
    }

    /**
     * Async task used to commit a set of values for a contact entry to the database.
     */
    private class DeleteContactTask extends DialogAsyncTask<String, String, Boolean> {
        // Assign a unique task id.
        public static final int TASK_ID = 2;
        // Define the status message for the spinning dialog
        private static final String INIT_STATUS_MSG = "Deleting contact information...";

        /**
         * Constructor
         * @param context   parent context
         * @param callbacks callbacks for the delete async task (uses the callback that returns a single contact entry, but this parameter is unused for delete operation)
         * @param ce        the active (displayed) contact entry which should be deleted.
         */
        public DeleteContactTask(Context context, Callbacks callbacks, ContactEntry ce) {
            super(INIT_STATUS_MSG,context,callbacks, TASK_ID);
            setContactEntry(ce);
        }

        /**
         * Background task to delete the contact entry.
         * @param params    default parameters
         * @return          Returns true if operation was successful. (In this case, always true.)
         */
        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            // delete the given contact entry
            ContactStore.get(getActivity().getApplicationContext()).deleteContactEntry(getContactEntry());
            return true;
        }
    }
}
