package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.UUID;

public class ContactEditDetailFragment extends Fragment{
    private static final String TAG = "ContactDetailFragment";
    private static final String PACKAGE_NAME = "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact";

    private static final String ARG_CONTACT_ENTRY_ID = "contact_entry_id";

    private ContactEntry mContactEntry;
    private Callbacks mCallbacks;

    private UUID mContactEntryId;

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

    public static ContactEditDetailFragment newInstance(UUID contactEntryId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTACT_ENTRY_ID, contactEntryId);

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
            if (mContactEntryId == null)
                mContactEntry = null;
            else {
                // otherwise, the contact entry id was provided, so this is a modify operation;
                mContactEntry = ContactStore.get(getActivity()).getContactEntry(mContactEntryId);
            }
        }
        else {
            mContactEntryId = null;
            mContactEntry = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
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
        mContactPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // need to launch the chooser here
                pickImage();

                // and then need to create a separate method to
                // process the returned file from the activity
                // and refresh the photo view - move later
                //updatePhotoView(mContactPhotoView, mContactPhotoFile);
            }
        });
        // business card image
        mContactBCView = (ImageView) v.findViewById(R.id.ContactBusinessCardImg);

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

        if (mContactEntryId != null) {
            // need to obtain the existing contact entry and populate all of the EditText fields
            // with the existing data

            // obtain the photo here and then update the image view
            mContactPhotoFile = ContactStore.get(getActivity()).getPhotoFile(mContactEntry);
            updatePhotoView(mContactPhotoView, mContactPhotoFile);
            // business card image
            mContactBCFile = ContactStore.get(getActivity()).getBCPhotoFile(mContactEntry);
            updatePhotoView(mContactBCView, mContactBCFile);

            if (mContactNameEdit != null) {
                String nameVal = mContactEntry.getName();
                if (nameVal != null)    // make sure that return value is not null
                    mContactNameEdit.setText(nameVal, TextView.BufferType.EDITABLE);
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
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG,"onCreateOptionsMenu");
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
                        .phoneNumber(mContactPhoneNumEdit.getText().toString(),mContactPhoneExtEdit.getText().toString())
                        .faxNumber(mContactFaxNumEdit.getText().toString())
                        .email(mContactEmailEdit.getText().toString())
                        .website(mContactCompanyWebsiteEdit.getText().toString())
                        .notes(mContactNotesEdit.getText().toString())
                        .build();
                // If this is an "Add" operation, then add the new contact
                if (mContactEntryId == null) {
                    ContactStore.get(getActivity()).addContactEntry(ce);
                }
                // Otherwise, it is a "Modify"/"Update" operation, so update the entry
                else {
                    ContactStore.get(getActivity()).updateContactEntry(ce);
                }
                mCallbacks.onContactEntrySaveChanges(mContactEntry);
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

    private void pickImage() {
        // need to start activity to pick an image.
        /*
        Intent intent = new Intent(Intent.ACTION_SEND);
        String title = getResources().getString(R.string.img_chooser_title);
        // Create intent to show chooser
        Intent chooser = Intent.createChooser(intent, title);
        // Verify the intent will resolve to at least one activity
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(chooser);
        }
        */
    }

    // Displays the provided image file in the referenced image view.
    private void updatePhotoView(ImageView imgView, File imgFile) {
        if (imgView != null) {
            if (imgFile == null || !imgFile.exists()) {
                //imgView.setImageDrawable(null);   // would display no image.
                // instead, if no image file exists, display the default image.
                //imgView.setImageResource(R.drawable.ic_add_a_photo_holo_light);
                // would potentially like to change the default photo image with themes;
                // so use a string here to reference the photo image.
                imgView.setImageResource(getResources().getIdentifier(getResources().getString(R.string.default_photo_img), "drawable", PACKAGE_NAME ));
            } else {
                // Uncomment this section when PictureUtils is set up.
                Bitmap bitmap = PictureUtils.getScaledBitmap(
                        imgFile.getPath(), getActivity());
                imgView.setImageBitmap(bitmap);
            }
        }
    }
}
