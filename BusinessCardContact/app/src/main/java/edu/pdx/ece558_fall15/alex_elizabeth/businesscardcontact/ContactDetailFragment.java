package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.content.Intent;
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
    private static final String PACKAGE_NAME = "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact";

    private static final String ARG_CONTACT_ENTRY_ID = "contact_entry_id";

    private ContactEntry mContactEntry;
    private Callbacks mCallbacks;

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
        void onContactEntryUpdated(ContactEntry ce);
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
        UUID contactEntryId = (UUID) getArguments().getSerializable(ARG_CONTACT_ENTRY_ID);
        mContactEntry = ContactStore.get(getActivity()).getContactEntry(contactEntryId);
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
        View v = inflater.inflate(R.layout.contact_detail, container, false);

        // contact photo image
        mContactPhotoView = (ImageView) v.findViewById(R.id.ContactPicture);
        // obtain the photo here and then update the image view
        mContactPhotoFile = ContactStore.get(getActivity()).getPhotoFile(mContactEntry);
        updatePhotoView(mContactPhotoView, mContactPhotoFile);
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
        mContactBCFile = ContactStore.get(getActivity()).getBCPhotoFile(mContactEntry);
        updatePhotoView(mContactBCView, mContactBCFile);

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
            default:
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
