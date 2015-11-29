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

public class ContactDetailFragment extends Fragment
        implements DialogAsyncTask.Callbacks {
    private static final String TAG = "ContactDetailFragment";

    private static final String PACKAGE_NAME =
            "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact";

    private static final String ARG_CONTACT_ENTRY_ID = "contact_entry_id";

    private ContactEntry mContactEntry;
    private Callbacks mCallbacks;

    private UUID mContactEntryId;

    private BitmapLoaderAsyncTask mBlat;
    private LoadContactTask mLct;
    private DeleteContactTask mDct;

    // images
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
        void onContactEntryEdit(ContactEntry ce);
        void onContactEntryDelete(ContactEntry ce);
    }

    public static ContactDetailFragment newInstance(UUID contactEntryId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTACT_ENTRY_ID, contactEntryId);

        ContactDetailFragment fragment = new ContactDetailFragment();
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
        mContactEntryId = (UUID) getArguments().getSerializable(ARG_CONTACT_ENTRY_ID);

        //Todo: Add an async task here to get the current contact entry
        mContactEntry = ContactStore.get(getActivity()).getContactEntry(mContactEntryId);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        //Add an async task here to retrieve the data for the given contact id:
        if (mContactEntryId != null) {
            mLct = new LoadContactTask(getActivity(), this, mContactEntryId);
            mLct.execute();
        }
        //mContactEntry = ContactStore.get(getActivity()).getContactEntry(mContactEntryId);
        updateUI();
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
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        if(mBlat != null && mBlat.getStatus() != AsyncTask.Status.FINISHED) {
            mBlat.cancel(true);
        }
        if(mLct != null && mLct.getStatus() != AsyncTask.Status.FINISHED) {
            mLct.cancel(true);
        }
        if(mDct != null && mDct.getStatus() != AsyncTask.Status.FINISHED) {
            mDct.cancel(true);
        }
        super.onDestroyView();
    }

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG,"onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_contact_detail, menu);
    }

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
                deleteContactCheck();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    // Displays the provided image file in the referenced image view.
    private void updatePhotoView(ImageView imgView, File imgFile) {
        if (imgView != null) {
            if (imgFile == null || !imgFile.exists()) {
                //imgView.setImageDrawable(null);   // would display no image.
                // instead, if no image file exists, display the default image.
                imgView.setImageResource(R.drawable.ic_photo_camera_light);
                // would potentially like to change the default photo image with themes;
                // so use a string here to reference the photo image.
                //imgView.setImageResource(getResources().getIdentifier(getResources().getString(R.string.default_photo_img), "drawable", PACKAGE_NAME ));
            } else {
                // Uncomment this section when PictureUtils is set up.
                Bitmap bitmap = PictureUtils.getScaledBitmap(
                    imgFile.getPath(), getActivity());
                imgView.setImageBitmap(bitmap);
            }
        }
    }

    public void updateUI() {
        Log.d(TAG, "updateUI");
        populateImageViews();
        populateFieldViews();
    }

    // Populates all imageviews
    private void populateImageViews() {
        /*
        // update the image view
        mContactPhotoFile = ContactStore.get(getActivity()).getPhotoFile(mContactEntry);
        updatePhotoView(mContactPhotoView, mContactPhotoFile);

        // update business card image
        mContactBCFile = ContactStore.get(getActivity()).getBCPhotoFile(mContactEntry);
        updatePhotoView(mContactBCView, mContactBCFile);*/

        mContactPhotoFile = ContactStore.get(getActivity()).getPhotoFile(mContactEntry);
        mContactBCFile = ContactStore.get(getActivity()).getBCPhotoFile(mContactEntry);

        ArrayList<ImageView> imageViews = new ArrayList<>();
        ArrayList<String> paths = new ArrayList<>();
        if(mContactPhotoView != null && mContactPhotoFile != null && mContactPhotoFile.exists()) {
            imageViews.add(mContactPhotoView);
            paths.add(mContactPhotoFile.getPath());
        }
        if(mContactBCView != null && mContactBCFile != null && mContactBCFile.exists()) {
            imageViews.add(mContactBCView);
            paths.add(mContactBCFile.getPath());
        }
        mBlat = new BitmapLoaderAsyncTask(getActivity(), this,
                imageViews.toArray(new ImageView[imageViews.size()]));
        mBlat.execute( paths.toArray(new String[paths.size()]));
    }

    // Populates the existing textviews
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
        // TODO: before checking for confirmation from user that the entry should be deleted... may need to check first that the given contact entry is not null... (but this case shouldn't actually happen...)

        // get confirmation from user in a dialog that they want to go back without saving changes
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Delete this contact?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Yes, the user wants to delete the contact...
                deleteContact();
                //ContactStore.get(getActivity().getApplicationContext()).deleteContactEntry(mContactEntry);

                // Note: after deletion, need to return to the list view...
                // which should be the previous activity in the backstack, so need to close this activity
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
        alert.show();

    }

    @Override
    public void onAsyncTaskFinished(ContactEntry contactEntry, boolean success, int taskId) {
        //Log.d(TAG, "onAsyncTaskFinished; ceId: "+mContactEntryId);
        Log.d(TAG, "onAsyncTaskFinished");
        // When the async task to save a contact has been completed, need to verify success.
        // First, determine the identity of the async task which has completed.

        if (taskId == LoadContactTask.TASK_ID) {
            if (success) {
                // if the Load Contact Task is successful, need to update the UI.
                mContactEntry = contactEntry;   // save the loaded contact entry
                updateUI();
            }
        }
        else if (taskId == DeleteContactTask.TASK_ID) {
            if (success) {
                // once the contact is successfully deleted, just need to perform the callback to the activity so that the activity will be closed.
                mCallbacks.onContactEntryDelete(mContactEntry);
            }
            // if it was not successful, would probably be good to display some message... but this case is very unlikely to happen
        }
        else if (taskId == BitmapLoaderAsyncTask.TASK_ID) {
            //nothing to do
        }
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
    private class DeleteContactTask extends DialogAsyncTask<String, String, Boolean> {
        // Assign a unique task id.
        public static final int TASK_ID = 2;
        // Define the status message for the spinning dialog
        private static final String INIT_STATUS_MSG = "Deleting contact information...";

        public DeleteContactTask(Context context, Callbacks callbacks, ContactEntry ce) {
            super(INIT_STATUS_MSG,context,callbacks, TASK_ID);
            setContactEntry(ce);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            // delete the given contact entry
            ContactStore.get(getActivity().getApplicationContext()).deleteContactEntry(getContactEntry());
            return true;
        }
    }
}
