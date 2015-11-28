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
import android.widget.TextView;

import java.io.File;
import java.util.UUID;

public class ContactDetailFragment extends Fragment {
    private static final String TAG = "ContactDetailFragment";

    private static final String PACKAGE_NAME =
            "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact";

    private static final String ARG_CONTACT_ENTRY_ID = "contact_entry_id";

    private ContactEntry mContactEntry;
    private Callbacks mCallbacks;

    private UUID mContactEntryId;

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
        mContactEntry = ContactStore.get(getActivity()).getContactEntry(mContactEntryId);
        updateUI();
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
                mCallbacks.onContactEntryDelete(mContactEntry);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    private void pickImage() {
        // need to start activity to pick an image.
        Intent intent = new Intent(Intent.ACTION_SEND);
        String title = getResources().getString(R.string.img_chooser_title);
        // Create intent to show chooser
        Intent chooser = Intent.createChooser(intent, title);
        // Verify the intent will resolve to at least one activity
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(chooser);
        }
    }
    */

    // Displays the provided image file in the referenced image view.
    private void updatePhotoView(ImageView imgView, File imgFile) {
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
        // update the image view
        mContactPhotoFile = ContactStore.get(getActivity()).getPhotoFile(mContactEntry);
        updatePhotoView(mContactPhotoView, mContactPhotoFile);

        // update business card image
        mContactBCFile = ContactStore.get(getActivity()).getBCPhotoFile(mContactEntry);
        updatePhotoView(mContactBCView, mContactBCFile);
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
}
