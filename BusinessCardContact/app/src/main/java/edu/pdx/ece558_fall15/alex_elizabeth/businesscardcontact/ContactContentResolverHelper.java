package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.provider.ContactsContract.CALLER_IS_SYNCADAPTER;
import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.Note;
import static android.provider.ContactsContract.CommonDataKinds.Organization;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.Photo;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.CommonDataKinds.Website;
import static android.provider.ContactsContract.Data;
import static android.provider.ContactsContract.RawContacts;

/**
 * Class to facilitate storage of ContactEntry objects into the SQLite backed
 * Android Contact Storage.
 */
public class ContactContentResolverHelper {
    private static final String TAG = "ContactCntResolvHelper";

    //This is the account type that we will store the contact information under
    private static final String ACT_TYPE =
            "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact";

    //Context under which this is called
    private Context mContext;

    /**
     * Constructor to create an instance of the ContactContentResolverHelper
     * that is used to add, modify, deleted, and get items from the Android
     * Contact Storage
     * @param context the context which this is called from
     */
    public ContactContentResolverHelper(Context context) {
        mContext = context;
    }

    /**
     * Adds a new contact to the Android Contact Storage
     * @param contactEntry the ContactEntry to add
     */
    public void addNewContact(ContactEntry contactEntry) {
        Log.d(TAG, "addNewContact");
        long rawContactId = addRawContact(contactEntry.getId());
        addNameData(rawContactId, contactEntry);
        addEmailData(rawContactId, contactEntry);
        addPhoneData(rawContactId, contactEntry);
        addFaxData(rawContactId, contactEntry);
        addCompanyData(rawContactId, contactEntry);
        addWebsiteData(rawContactId, contactEntry);
        addNoteData(rawContactId, contactEntry);
        addPhotoData(rawContactId, contactEntry);
        addBusinessCardData(rawContactId, contactEntry);
    }

    /**
     * Updates a currently existing contact in the Android
     * Contact Storage
     * @param contactEntry the updated ContactEntry
     */
    public void updateContact(ContactEntry contactEntry) {
        Log.d(TAG, "updateContact");
        long rawContactId = getRawContact(contactEntry.getId());
        updateNameData(rawContactId, contactEntry);
        updateEmailData(rawContactId, contactEntry);
        updatePhoneData(rawContactId, contactEntry);
        updateFaxData(rawContactId, contactEntry);
        updateCompanyData(rawContactId, contactEntry);
        updateWebsiteData(rawContactId, contactEntry);
        updateNoteData(rawContactId, contactEntry);
        //writeDisplayPhoto(rawContactId, contactEntry);
        updatePhotoData(rawContactId, contactEntry);
        updateBusinessCardData(rawContactId, contactEntry);
    }

    //adapted from stackoverflow answer by Anton Klimov
    /*public void writeDisplayPhoto(long rawContactId, ContactEntry contactEntry) {
        if(contactEntry.getPhotoFilePath() != null) {
            Bitmap b = BitmapFactory.decodeFile(contactEntry.getPhotoFilePath());
            int bytes = b.getByteCount();
            ByteBuffer photo = ByteBuffer.allocate(bytes);
            b.copyPixelsToBuffer(photo);
            Uri rawContactPhotoUri = Uri.withAppendedPath(
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                    RawContacts.DisplayPhoto.CONTENT_DIRECTORY
            );

            try {
                AssetFileDescriptor fd = mContext.getApplicationContext()
                        .getContentResolver().openAssetFileDescriptor(
                        rawContactPhotoUri,
                        "rw"
                );

                BufferedOutputStream os = null;
                if (fd != null) {
                    os = new BufferedOutputStream(fd.createOutputStream());
                    os.write(photo.array());
                    os.close();
                    fd.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Problem writing display photo:", e);
            }
        }
    }*/

    /*public Bitmap getDisplayPhoto(long rawContactId) {
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, rawContactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.DISPLAY_PHOTO);
        InputStream is = Contacts.openContactPhotoInputStream(mContext.getContentResolver(), displayPhotoUri, true);

        //Adapted from stackoverflow answer by Adamski
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        try {
            while((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem reading display photo:", e);
        }

        return BitmapFactory.decodeByteArray(buffer.toByteArray(), 0, buffer.size());
    }*/

    /**
     * Returns a list of ContactEntry objects from the Android
     * Contact Storage
     * @return a list of ContactEntry objects
     */
    public List<ContactEntry> getAllContacts() {
        Log.d(TAG, "getAllContacts");
        List<ContactEntry> contactEntries = new ArrayList<>();

        //Select all RawContacts _ID and ACCOUNT_NAME columns where the account
        //type matches our account type
        ContactEntryCursorWrapper cursor = new ContactEntryCursorWrapper(
                mContext.getContentResolver().query(
                        RawContacts.CONTENT_URI,
                        new String[] { RawContacts._ID, RawContacts.ACCOUNT_NAME },
                        RawContacts.ACCOUNT_TYPE + "='" + ACT_TYPE + "'",
                        null,
                        null
                )
        );
        cursor.getRawContacts(contactEntries, this);

        //Close the cursor to not leak resources
        cursor.close();

        //Return the list of ContactEntry objects
        return contactEntries;
    }

    /**
     * Deletes a contact from the Android Contact Storage
     * @param contactEntry the ContactEntry to delete
     */
    public void deleteContact(ContactEntry contactEntry) {
        Log.d(TAG, "deleteContact");

        //Build a Uri for the RawContacts table and add a query parameter that
        //indicates we are a "SyncAdapter" meaning that the contact is entirely removed
        //and not just flagged for deletion.
        Uri contentSyncAdapter = RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(CALLER_IS_SYNCADAPTER, "true")
                .build();

        //Delete the contact only if the account name matches the specified Id and
        //the account type matches our account type
        int numRowsDeleted = mContext.getContentResolver()
                .delete(contentSyncAdapter,
                        RawContacts.ACCOUNT_NAME + "='" + contactEntry.getId().toString() +
                                "' AND " + RawContacts.ACCOUNT_TYPE + "='" + ACT_TYPE + "'",
                        null);

        //If for some reason something goes wrong and we delete multiple contacts, log an
        //error message
        if(numRowsDeleted > 1) {
            Log.e(TAG, "Multiple Contacts Deleted in Error");
        }
    }

    /**
     * Add a RawContact of our type with the specified UUID
     * @param uuid the UUID for the ContactEntry
     * @return the rawContactId for the just inserted RawContact
     */
    private long addRawContact(UUID uuid) {
        Log.d(TAG, "addRawContact");

        //Create a ContentValues with the specified account type and account name (the UUID)
        ContentValues person = new ContentValues();
        person.put(RawContacts.ACCOUNT_TYPE, ACT_TYPE);
        person.put(RawContacts.ACCOUNT_NAME, uuid.toString());

        //Insert into the RawContacts table
        Uri rawContactUri = mContext.getContentResolver()
                .insert(RawContacts.CONTENT_URI, person);

        //Return the rawContactId for the just inserted RawContact entry
        return ContentUris.parseId(rawContactUri);
    }

    /**
     * Add a Data table entry for the name data
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void addNameData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addNameData");

        //Create a ContentValues with the RawContact _ID and a type of StructuredName
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);

        //Add all of the name values from the ContactEntry
        addNameValues(contactEntry, values);

        //Insert the Data table entry for the name data
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    /**
     * Add a Email table entry for the email data
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void addEmailData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addEmailData");

        //Create a ContentValues with the RawContact _ID and a type of Email
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);

        //Add all of the email values for the ContactEntry
        addEmailValues(contactEntry, values);

        //Insert the Data table entry for the email data
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    /**
     * Add a Phone table entry for the phone data
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void addPhoneData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addPhoneData");

        //Create a ContentValues with the RawContact _ID and a type of Phone
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);

        //Add all of the phone values for the ContactEntry
        addPhoneValues(contactEntry, values);

        //Insert the Data table entry for the phone data
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    /**
     * Add a Phone table entry for the fax data
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void addFaxData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addFaxData");

        //Create a ContentValues with the RawContact _ID and a type of Phone
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);

        //Add all of the fax values for the ContactEntry
        addFaxValues(contactEntry, values);

        //Insert the Data table entry for the fax data
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    /**
     * Add a Organization table entry for the company data
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void addCompanyData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addCompanyData");

        //Create a ContentValues with the RawContact _ID and a type of Organization
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);

        //Add all of the company values for the ContactEntry
        addCompanyValues(contactEntry, values);

        //Insert the Data table entry for the company data
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    /**
     * Add a Website table entry for the website data
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void addWebsiteData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addWebsiteData");

        //Create a ContentValues with the RawContact _ID and a type of Website
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Website.CONTENT_ITEM_TYPE);

        //Add all of the website values for the ContactEntry
        addWebsiteValues(contactEntry, values);

        //Insert the Data table entry for the website data
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    /**
     * Add a Note table entry for the note data
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void addNoteData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addNoteData");

        //Create a ContentValues with the RawContact _ID and a type of Note
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE);

        //Add all of the note values for the ContactEntry
        addNoteValues(contactEntry, values);

        //Insert the Data table entry for the note data
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    /**
     * Add a Photo table entry for the photo data
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void addPhotoData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addPhotoData");
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
        addPhotoValues(contactEntry, values);
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    /**
     * Add a Business Card table entry for the photo data
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void addBusinessCardData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addBusinessCardData");
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, BusinessCardPhoto.CONTENT_ITEM_TYPE);
        addBusinessCardValues(contactEntry, values);
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    /**
     * Get the RawContact _ID based on the specified UUID
     * @param uuid Id of the RawContact entry to get
     * @return the RawContact _ID value
     */
    private long getRawContact(UUID uuid) {
        Log.d(TAG, "getRawContact");

        //Get the RawContacts _ID column where the account name matches the UUID and the
        //account type matches our account type
        ContactEntryCursorWrapper cursor = new ContactEntryCursorWrapper(
                mContext.getContentResolver().query(
                        RawContacts.CONTENT_URI,
                        new String[] { RawContacts._ID },
                        RawContacts.ACCOUNT_NAME + "='" + uuid.toString() + "' AND " +
                        RawContacts.ACCOUNT_TYPE + "='" + ACT_TYPE + "'",
                        null,
                        null
                )
        );

        //If for some reason something goes wrong and we match multiple contacts, log an
        //error message
        if(cursor.getCount() > 1) {
            Log.e(TAG, "Multiple matching contacts found in Error");
        }

        //Get the RawContact _ID value if it exists, otherwise 0
        long rawContactId = cursor.getRawContactId();
        //Close the cursor to prevent resource leaks
        cursor.close();

        //Return the retrieved value
        return rawContactId;
    }

    /**
     * Update a name data entry from the Data table
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void updateNameData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateNameData");

        //Create a ContentValues and add the name values
        ContentValues values = new ContentValues();
        addNameValues(contactEntry, values);

        //Update the entry with the matching RawContact _ID and matching MIMETYPE
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                                Data.MIMETYPE + "='" + StructuredName.CONTENT_ITEM_TYPE + "'",
                        null);

        //If for some reason something goes wrong and we update multiple contacts, log an
        //error message
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    /**
     * Update an email data entry from the Data table
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void updateEmailData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateEmailData");

        //Create a ContentValues and add the email values
        ContentValues values = new ContentValues();
        addEmailValues(contactEntry, values);

        //Update the entry with the matching RawContact _ID and matching MIMETYPE
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                        Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'",
                        null);

        //If for some reason something goes wrong and we update multiple contacts, log an
        //error message
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    /**
     * Update a phone data entry from the Data table
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void updatePhoneData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updatePhoneData");

        //Create a ContentValues and add the phone values
        ContentValues values = new ContentValues();
        addPhoneValues(contactEntry, values);

        //Update the entry with the matching RawContact _ID, matching MIMETYPE, and TYPE
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                        Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "' AND " +
                        Phone.TYPE + "='" + Phone.TYPE_WORK + "'",
                        null);

        //If for some reason something goes wrong and we update multiple contacts, log an
        //error message
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    /**
     * Update a fax data entry from the Data table
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void updateFaxData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateFaxData");

        //Create a ContentValues and add the fax values
        ContentValues values = new ContentValues();
        addFaxValues(contactEntry, values);

        //Update the entry with the matching RawContact _ID, matching MIMETYPE, and TYPE
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                                Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "' AND " +
                                Phone.TYPE + "='" + Phone.TYPE_FAX_WORK + "'",
                        null);

        //If for some reason something goes wrong and we update multiple contacts, log an
        //error message
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    /**
     * Update a company data entry from the Data table
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void updateCompanyData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateCompanyData");

        //Create a ContentVaues and add the company values
        ContentValues values = new ContentValues();
        addCompanyValues(contactEntry, values);

        //Update the entry with the  matching RawContact _ID and matching MIMETYPE
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                                Data.MIMETYPE + "='" + Organization.CONTENT_ITEM_TYPE + "'",
                        null);

        //If for some reason something goes wrong and we update multiple contacts, log an
        //error message
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    /**
     * Update a website data entry from the Data table
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void updateWebsiteData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateWebsiteData");

        //Create a ContentValues and add the website values
        ContentValues values = new ContentValues();
        addWebsiteValues(contactEntry, values);

        //Update the entry with the  matching RawContact _ID and matching MIMETYPE
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                                Data.MIMETYPE + "='" + Website.CONTENT_ITEM_TYPE + "'",
                        null);

        //If for some reason something goes wrong and we update multiple contacts, log an
        //error message
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    /**
     * Update a note data entry from the Data table
     * @param rawContactId the RawContact _ID to use as a foreign key
     * @param contactEntry the ContactEntry to get the data from
     */
    private void updateNoteData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateNoteData");

        //Create a ContentValues and add the website values
        ContentValues values = new ContentValues();
        addNoteValues(contactEntry, values);

        //Update the entry with the  matching RawContact _ID and matching MIMETYPE
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                                Data.MIMETYPE + "='" + Note.CONTENT_ITEM_TYPE + "'",
                        null);

        //If for some reason something goes wrong and we update multiple contacts, log an
        //error message
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    private void updatePhotoData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updatePhotoData");

        //Create a ContentValues and add the photo values
        ContentValues values = new ContentValues();
        addPhotoValues(contactEntry, values);

        //Update the entry with the matching RawContact _ID and matching MIMETYPE
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                            Data.MIMETYPE + "='" + Photo.CONTENT_ITEM_TYPE + "'",
                        null);

        //If for some reason something goes wrong and we update multiple contacts, log an
        //error message
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    private void updateBusinessCardData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateBCData");

        //Create a ContentValues and add the photo values
        ContentValues values = new ContentValues();
        addBusinessCardValues(contactEntry, values);

        //Update the entry with the matching RawContact _ID and matching MIMETYPE
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                                Data.MIMETYPE + "='" + BusinessCardPhoto.CONTENT_ITEM_TYPE + "'",
                        null);

        //If for some reason something goes wrong and we update multiple contacts, log an
        //error message
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    /**
     * Adds name values to the provided ContentValues
     * @param contactEntry ContactEntry to get the data from
     * @param values ContentValues to add to
     */
    private void addNameValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getName() != null) {
            values.put(StructuredName.DISPLAY_NAME, contactEntry.getName());
        }
        if(contactEntry.getFirstName() != null) {
            values.put(StructuredName.GIVEN_NAME, contactEntry.getFirstName());
        }
        if(contactEntry.getLastName() != null) {
            values.put(StructuredName.FAMILY_NAME, contactEntry.getLastName());
        }
    }

    /**
     * Adds email values to the provided ContentValues
     * @param contactEntry ContactEntry to get the data from
     * @param values ContentValues to add to
     */
    private void addEmailValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getEmail() != null) {
            values.put(Email.ADDRESS, contactEntry.getEmail());
            values.put(Email.TYPE, Email.TYPE_WORK);
        }
    }

    /**
     * Adds phone values to the provided ContentValues
     * @param contentEntry ContactEntry to get the data from
     * @param values ContentValues to add to
     */
    private void addPhoneValues(ContactEntry contentEntry, ContentValues values) {
        if(contentEntry.getPhoneNumber() != null) {
            values.put(Phone.NUMBER, contentEntry.getPhoneNumber());
            values.put(Phone.TYPE, Phone.TYPE_WORK);
        }

        //If there is an extension store it in a custom column
        if(contentEntry.getExtension() != null) {
            values.put(Phone.DATA5, contentEntry.getExtension());
        }
    }

    /**
     * Adds fax values to the provided ContentValues
     * @param contactEntry ContactEntry to get the data from
     * @param values ContentValues to add to
     */
    private void addFaxValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getFaxNumber() != null) {
            values.put(Phone.NUMBER, contactEntry.getFaxNumber());
            values.put(Phone.TYPE, Phone.TYPE_FAX_WORK);
        }
    }

    /**
     * Adds company values to the provided ContentValues
     * @param contactEntry ContactEntry to get the data from
     * @param values ContentValues to add to
     */
    private void addCompanyValues(ContactEntry contactEntry, ContentValues values) {
        values.put(Organization.TYPE, Organization.TYPE_WORK);
        if(contactEntry.getCompany() != null) {
            values.put(Organization.COMPANY, contactEntry.getCompany());
        }
        if(contactEntry.getDivision() != null) {
            values.put(Organization.DEPARTMENT, contactEntry.getDivision());
        }
        if(contactEntry.getTitle() != null) {
            values.put(Organization.TITLE, contactEntry.getTitle());
        }
    }

    /**
     * Adds website values to the provided ContentValues
     * @param contactEntry ContactEntry to get the data from
     * @param values ContentValues to add to
     */
    private void addWebsiteValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getWebsite() != null) {
            values.put(Website.TYPE, Website.TYPE_WORK);
            values.put(Website.URL, contactEntry.getWebsite());
        }
    }

    /**
     * Adds note values to the provided ContentValues
     * @param contactEntry ContactEntry to get the data from
     * @param values ContentValues to add to
     */
    private void addNoteValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getNotes() != null) {
            values.put(Note.NOTE, contactEntry.getNotes());
        }
    }

    /**
     * Adds photo values to the provided ContentValues
     * @param contactEntry ContactEntry to get the data from
     * @param values ContentValues to add to
     */
    private void addPhotoValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getPhotoFilePath() != null) {
            Log.d(TAG, contactEntry.getPhotoFilePath());
            values.put(Photo.PHOTO_FILE_ID, contactEntry.getPhotoFilePath());
            /*values.put(Data.IS_PRIMARY, 1);
            values.put(Data.IS_SUPER_PRIMARY, 1);
            Bitmap b = PictureUtils.getScaledBitmap(
                    contactEntry.getPhotoFilePath(), 256, 256);
            if(b != null) {
                int bytes = b.getByteCount();
                ByteBuffer buffer = ByteBuffer.allocate(bytes);
                b.copyPixelsToBuffer(buffer);
                values.put(Photo.PHOTO, buffer.array());
            }*/
        }
    }

    /**
     * Adds business card values to the provided ContentValues
     * @param contactEntry ContactEntry to get the data from
     * @param values ContentValues to add to
     */
    private void addBusinessCardValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getBCFilePath() != null) {
            Log.d(TAG, contactEntry.getBCFilePath());
            values.put(BusinessCardPhoto.PHOTO_FILE_ID, contactEntry.getBCFilePath());
            /*Bitmap b = PictureUtils.getScaledBitmap(
                    contactEntry.getPhotoFilePath(), 256, 256);
            if(b != null) {
                int bytes = b.getByteCount();
                ByteBuffer buffer = ByteBuffer.allocate(bytes);
                b.copyPixelsToBuffer(buffer);
                values.put(BusinessCardPhoto.PHOTO, buffer.array());
            }*/
        }
    }

    /**
     * Get the ContactEntry from the Android Contact Storage with the specified RawContact _ID
     * and assign it the provided UUID
     * @param rawContactId the value of the RawContact _ID to search by
     * @param uuid the UUID to assign
     * @return a ContactEntry with all of the stored information set
     */
    private ContactEntry getContact(long rawContactId, UUID uuid) {
        Log.d(TAG, "getContact");

        //Create a new ContactEntry with the provided UUID
        ContactEntry contactEntry = new ContactEntry(uuid);

        //Create a Uri that has the RawContact _ID and the RawContacts.Entity joined table path
        Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
        Uri entityUri = Uri.withAppendedPath(rawContactUri, RawContacts.Entity.CONTENT_DIRECTORY);

        //Get the relevant columns from the Entity table
        ContactEntryCursorWrapper cursor = new ContactEntryCursorWrapper(
                mContext.getContentResolver().query(
                        entityUri,
                        new String[]{RawContacts.SOURCE_ID, RawContacts.Entity.DATA_ID,
                                RawContacts.Entity.MIMETYPE, RawContacts.Entity.DATA1,
                                RawContacts.Entity.DATA2, RawContacts.Entity.DATA3,
                                RawContacts.Entity.DATA4, RawContacts.Entity.DATA5,
                                RawContacts.Entity.DATA14, RawContacts.Entity.DATA15},
                        null,
                        null,
                        null
                )
        );

        try {
            //Extract the values into the ContactEntry
            cursor.extractValuesToContactEntry(contactEntry);
            //getDisplayPhoto(rawContactId);
        } finally {
            //Close the cursor to not leak resources
            cursor.close();
        }

        //Return the ContactEntry with the extracted information set
        return contactEntry;
    }

    /**
     * Internal class that "wraps" a cursor object, allowing for adding methods to the basic
     * set provided by Cursor (by extending CursorWrapper)
     * This class is responsible for all methods that need to access data in the cursor
     */
    private class ContactEntryCursorWrapper extends CursorWrapper {

        /**
         * Create a new ContactEntryCursorWrapper around the provied cursor
         * @param cursor the cursor to "wrap"
         */
        public ContactEntryCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        /**
         * Extract all of the values from a cursor and store them in the provided
         * ContactEntry
         * @param contactEntry the ContactEntry with the data fields set
         */
        private void extractValuesToContactEntry(ContactEntry contactEntry) {
            while (this.moveToNext()) {
                String sourceId = this.getString(0);

                //Check if the data ID is null, if null no data to parse
                if (!this.isNull(1)) {

                    //Get the MIMETYPE to compare
                    String mimeType = this.getString(2);

                    //Get all of relevant data columns
                    String data1 = this.getString(3);
                    String data2 = this.getString(4);
                    String data3 = this.getString(5);
                    String data4 = this.getString(6);
                    String data5 = this.getString(7);
                    String data14 = this.getString(8);
                    byte[] data15 = this.getBlob(9);

                    //Case statement based on the MIMETYPE
                    switch (mimeType) {
                        case StructuredName.CONTENT_ITEM_TYPE:
                            //Set the name value
                            if (data1 != null) {
                                contactEntry.setName(data1);
                            }
                            break;
                        case Email.CONTENT_ITEM_TYPE:
                            //Set the email value
                            if (data1 != null) {
                                contactEntry.setEmail(data1);
                            }
                            break;
                        case Phone.CONTENT_ITEM_TYPE:
                            if (data2 != null) {
                                //Set the phone values
                                if (Integer.parseInt(data2) == Phone.TYPE_WORK) {
                                    if (data1 != null) {
                                        contactEntry.setPhoneNumber(data1);
                                    }
                                    if (data5 != null) {
                                        contactEntry.setExtension(data5);
                                    }
                                //Set the fax value
                                } else if (Integer.parseInt(data2) == Phone.TYPE_FAX_WORK) {
                                    if (data1 != null) {
                                        contactEntry.setFaxNumber(data1);
                                    }
                                }
                            }
                            break;
                        case Organization.CONTENT_ITEM_TYPE:
                            //Set the various company values
                            if (data1 != null) {
                                contactEntry.setCompany(data1);
                            }
                            if (data4 != null) {
                                contactEntry.setTitle(data4);
                            }
                            if (data5 != null) {
                                contactEntry.setDivision(data5);
                            }
                            break;
                        case Website.CONTENT_ITEM_TYPE:
                            //Set the website values
                            if (data1 != null) {
                                contactEntry.setWebsite(data1);
                            }
                            break;
                        case Note.CONTENT_ITEM_TYPE:
                            //set the note values
                            if (data1 != null) {
                                contactEntry.setNotes(data1);
                            }
                            break;
                        case Photo.CONTENT_ITEM_TYPE:
                            //Set the photo values
                            if(data14 != null) {
                                Log.d(TAG, "Retrieved Photo Path: " + data14);
                                contactEntry.setPhotoFilePath(data14);
                            }
                            break;
                        case BusinessCardPhoto.CONTENT_ITEM_TYPE:
                            //Set the business card photo values
                            if(data14 != null) {
                                Log.d(TAG, "Retrieved BC Path: " + data14);
                                contactEntry.setBCFilePath(data14);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        /**
         * Get the RawContact _ID from the cursor
         * @return the RawContact _ID value
         */
        private long getRawContactId() {
            this.moveToFirst();
            long rawContactId = 0;
            if (!this.isAfterLast()) {
                rawContactId = Long.valueOf(this.getString(0));
            }
            return rawContactId;
        }

        /**
         * Moves through the rows "pointed" to by the cursor, getting the RawContact _ID and UUID
         * and calling the method necessary to get the information for each individual contact
         * @param contactEntries A list of ContactEntry objects to add to
         * @param helper A reference to the ContactContentResolverHelper to call the individual get
         *               methods on
         */
        private void getRawContacts(List<ContactEntry> contactEntries, ContactContentResolverHelper helper) {
            //Navigate through the cursor
            this.moveToFirst();
            while(!this.isAfterLast()) {
                //Extract the RawContact _ID and the UUID
                long rawContactId = Long.valueOf(this.getString(0));
                UUID uuid = UUID.fromString(this.getString(1));

                //Get the contact data using the RawContact _ID and the UUID
                //and add to the list to return
                contactEntries.add(helper.getContact(rawContactId, uuid));
                this.moveToNext();
            }
        }
    }

    private class BusinessCardPhoto {
        private static final String CONTENT_ITEM_TYPE =
                "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact.businesscard";;
        private static final String PHOTO_FILE_ID = "data14";
        private static final String PHOTO = "data15";
    }
}
