package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContactEditDetailFragment extends Fragment{
    private static final String TAG = "ContactEditDetailFrgmt";
    private static final String PACKAGE_NAME = "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact";

    private static final int PICK_CONTACT_IMAGE_REQUEST = 1;    // contact photo request
    private static final int PICK_BC_IMAGE_REQUEST = 2;         // business card photo request

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
                        .phoneNumber(mContactPhoneNumEdit.getText().toString(), mContactPhoneExtEdit.getText().toString())
                        .faxNumber(mContactFaxNumEdit.getText().toString())
                        .email(mContactEmailEdit.getText().toString())
                        .website(mContactCompanyWebsiteEdit.getText().toString())
                        .notes(mContactNotesEdit.getText().toString())
                        .photo(mContactPhotoFile)
                        .businessCard(mContactBCFile)
                        .build();
                // If this is an "Add" operation, then add the new contact
                if (mContactEntryId == null) {
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
                        new CommitContactTask(mContactEntry, true).execute();
                        mCallbacks.onContactEntrySaveChanges(mContactEntry);
                    }
                }
                // Otherwise, it is a "Modify"/"Update" operation, so update the entry
                else {
                    new CommitContactTask(mContactEntry, false).execute();
                    mCallbacks.onContactEntrySaveChanges(mContactEntry);
                }

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

    private class CommitContactTask extends AsyncTask<Void, Void, Void> {
        private ContactEntry mContactEntry;
        private boolean mNewContact;

        public CommitContactTask(ContactEntry contactEntry, boolean newContact) {
            mContactEntry = contactEntry;

        }

        @Override
        protected Void doInBackground(Void... params) {
            if(mNewContact) {
                ContactStore.get(getActivity()).addContactEntry(mContactEntry);
            } else {
                ContactStore.get(getActivity()).updateContactEntry(mContactEntry);
            }
            return null;
        }
    }

    /**
     * Based on stackoverflow answer by David Manpearl and Austyn Mahoney
     * @param requestCode
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
            final List<Intent> cameraIntents = new ArrayList<>();
            final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = getActivity().getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for(ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                cameraIntents.add(intent);
            }

            //Build a list of FileSystem sources that could provided the correct data
            final Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //galleryIntent.setType("image/*");
            //galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            //Create chooser of FileSystem options
            final Intent chooserIntent = Intent.createChooser(galleryIntent, chooserText);

            //Add the camera options
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

            if (chooserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(chooserIntent, requestCode);
            }
        }
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            //Returned from the camera
            if(data == null) {
                if(requestCode == PICK_CONTACT_IMAGE_REQUEST) {
                    mContactPhotoFile = ContactStore.get(this.getActivity()).getSuggestedPhotoFile(mContactEntry);
                    updatePhotoView(mContactPhotoView, mContactPhotoFile);
                } else if (requestCode == PICK_BC_IMAGE_REQUEST) {
                    mContactBCFile = ContactStore.get(this.getActivity()).getSuggestedBCFile(mContactEntry);
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
                        File tmpFile = persistImage(filesDir, bitmap,
                                mContactEntry.getSuggestedPhotoFilename());
                        if (tmpFile != null) {
                            mContactPhotoFile = tmpFile;
                            updatePhotoView(mContactPhotoView, mContactPhotoFile);
                        }
                    } else if (requestCode == PICK_BC_IMAGE_REQUEST) {
                        // update business card image
                        File tmpFile = persistImage(filesDir, bitmap,
                                mContactEntry.getSuggestedBCFilename());
                        if (tmpFile != null) {
                            mContactBCFile = tmpFile;
                            updatePhotoView(mContactBCView, mContactBCFile);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // helper method modified from stackoverflow
    private static File persistImage(File filesDir, Bitmap bitmap, String name) {
        File imageFile = new File(filesDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(TAG, "Error writing bitmap", e);
            return null;
        }

        return imageFile;
    }
}
