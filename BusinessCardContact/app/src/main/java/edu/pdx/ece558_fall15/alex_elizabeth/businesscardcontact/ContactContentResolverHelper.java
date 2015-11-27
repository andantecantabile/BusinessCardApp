package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        //addPhotoData(rawContactId, contactEntry);
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
        //updatePhotoData(rawContactId, contactEntry);
    }

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

        //Navigate through the cursor
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            //Extract the RawContact _ID and the UUID
            long rawContactId = Long.valueOf(cursor.getString(0));
            UUID uuid = UUID.fromString(cursor.getString(1));

            //Get the contact data using the RawContact _ID and the UUID
            //and add to the list to return
            contactEntries.add(getContact(rawContactId, uuid));
            cursor.moveToNext();
        }
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
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
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

    private long getRawContact(UUID uuid) {
        Log.d(TAG, "getRawContact");
        ContactEntryCursorWrapper cursor = new ContactEntryCursorWrapper(
                mContext.getContentResolver().query(
                        RawContacts.CONTENT_URI,
                        new String[] { RawContacts._ID },
                        RawContacts.ACCOUNT_NAME + "='" + uuid.toString() + "'",
                        null,
                        null
                )
        );

        cursor.moveToFirst();
        long rawContactId = 0;
        if (!cursor.isAfterLast()) {
            rawContactId = Long.valueOf(cursor.getString(0));
        }
        cursor.close();

        return rawContactId;
    }

    private void updateNameData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateNameData");
        ContentValues values = new ContentValues();
        addNameValues(contactEntry, values);
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                        Data.MIMETYPE + "='" + StructuredName.CONTENT_ITEM_TYPE + "'",
                        null);
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    private void updateEmailData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateEmailData");
        ContentValues values = new ContentValues();
        addEmailValues(contactEntry, values);
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                        Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'",
                        null);
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    private void updatePhoneData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updatePhoneData");
        ContentValues values = new ContentValues();
        addPhoneValues(contactEntry, values);
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                        Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "' AND " +
                        Phone.TYPE + "='" + Phone.TYPE_WORK + "'",
                        null);
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    private void updateFaxData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateFaxData");
        ContentValues values = new ContentValues();
        addPhoneValues(contactEntry, values);
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                                Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "' AND " +
                                Phone.TYPE + "='" + Phone.TYPE_FAX_WORK + "'",
                        null);
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    private void updateCompanyData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateCompanyData");
        ContentValues values = new ContentValues();
        addCompanyValues(contactEntry, values);
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                                Data.MIMETYPE + "='" + Organization.CONTENT_ITEM_TYPE + "'",
                        null);
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    private void updateWebsiteData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateWebsiteData");
        ContentValues values = new ContentValues();
        addCompanyValues(contactEntry, values);
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                                Data.MIMETYPE + "='" + Website.CONTENT_ITEM_TYPE + "'",
                        null);
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

    private void updateNoteData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "updateNoteData");
        ContentValues values = new ContentValues();
        addCompanyValues(contactEntry, values);
        int numRowsModified = mContext.getContentResolver()
                .update(Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "='" + rawContactId + "' AND " +
                                Data.MIMETYPE + "='" + Note.CONTENT_ITEM_TYPE + "'",
                        null);
        if(numRowsModified > 1) {
            Log.e(TAG, "Multiple Contacts Updated in Error");
        }
    }

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

    private void addEmailValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getEmail() != null) {
            values.put(Email.ADDRESS, contactEntry.getEmail());
            values.put(Email.TYPE, Email.TYPE_WORK);
        }
    }

    private void addPhoneValues(ContactEntry contentEntry, ContentValues values) {
        if(contentEntry.getPhoneNumber() != null) {
            values.put(Phone.NUMBER, contentEntry.getPhoneNumber());
            values.put(Phone.TYPE, Phone.TYPE_WORK);
        }
        //If there is an extension store it in a custom column
        if(contentEntry.getExtension() != null) {
            values.put(Phone.DATA4, contentEntry.getExtension());
        }
    }

    private void addFaxValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getFaxNumber() != null) {
            values.put(Phone.NUMBER, contactEntry.getFaxNumber());
            values.put(Phone.TYPE, Phone.TYPE_FAX_WORK);
        }
    }

    private void addCompanyValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getCompany() != null) {
            values.put(Organization.COMPANY, contactEntry.getCompany());
            values.put(Organization.DEPARTMENT, contactEntry.getDivision());
            values.put(Organization.TYPE, Organization.TYPE_WORK);
            values.put(Organization.TITLE, contactEntry.getTitle());
        }
    }

    private void addWebsiteValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getWebsite() != null) {
            values.put(Website.TYPE, Website.TYPE_WORK);
            values.put(Website.URL, contactEntry.getWebsite());
        }
    }

    private void addNoteValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getNotes() != null) {
            values.put(Note.NOTE, contactEntry.getNotes());
        }
    }

    private void addPhotoValues(ContactEntry contactEntry, ContentValues values) {
        if(contactEntry.getPhotoFilename() != null) {
            values.put(Photo.PHOTO_FILE_ID, contactEntry.getPhotoFilename());
            Bitmap b = PictureUtils.getScaledBitmap(
                    contactEntry.getPhotoFilename(), 160, 160);
            int bytes = b.getByteCount();
            ByteBuffer buffer = ByteBuffer.allocate(bytes);
            b.copyPixelsToBuffer(buffer);
            values.put(Photo.PHOTO, buffer.array());
        }
    }

    private ContactEntry getContact(long rawContactId, UUID uuid) {
        Log.d(TAG, "getContact");

        ContactEntry contactEntry = new ContactEntry(uuid);
        Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
        Uri entityUri = Uri.withAppendedPath(rawContactUri, RawContacts.Entity.CONTENT_DIRECTORY);
        ContactEntryCursorWrapper cursor = new ContactEntryCursorWrapper(
                mContext.getContentResolver().query(
                        entityUri,
                        new String[]{RawContacts.SOURCE_ID, RawContacts.Entity.DATA_ID,
                                RawContacts.Entity.MIMETYPE, RawContacts.Entity.DATA1,
                                RawContacts.Entity.DATA2, RawContacts.Entity.DATA3,
                                RawContacts.Entity.DATA4, RawContacts.Entity.DATA5 },
                        null,
                        null,
                        null
                )
        );

        try {
            while (cursor.moveToNext()) {
                String sourceId = cursor.getString(0);
                if (!cursor.isNull(1)) {
                    String mimeType = cursor.getString(2);
                    String data1 = cursor.getString(3);
                    String data2 = cursor.getString(4);
                    String data3 = cursor.getString(5);
                    String data4 = cursor.getString(6);
                    String data5 = cursor.getString(7);
                    switch (mimeType) {
                        case StructuredName.CONTENT_ITEM_TYPE:
                            if (data1 != null) {
                                contactEntry.setName(data1);
                            }
                            break;
                        case Email.CONTENT_ITEM_TYPE:
                            if (data1 != null) {
                                contactEntry.setEmail(data1);
                            }
                            break;
                        case Phone.CONTENT_ITEM_TYPE:
                            if (data2 != null) {
                                if (Integer.parseInt(data2) == Phone.TYPE_WORK) {
                                    if (data1 != null) {
                                        contactEntry.setPhoneNumber(data1);
                                    }
                                    if (data4 != null) {
                                        contactEntry.setExtension(data4);
                                    }
                                } else if (Integer.parseInt(data2) == Phone.TYPE_FAX_WORK) {
                                    if (data1 != null) {
                                        contactEntry.setFaxNumber(data1);
                                    }
                                }
                            }
                            break;
                        case Organization.CONTENT_ITEM_TYPE:
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
                            if (data1 != null) {
                                contactEntry.setWebsite(data1);
                            }
                            break;
                        case Note.CONTENT_ITEM_TYPE:
                            if (data1 != null) {
                                contactEntry.setNotes(data1);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } finally {
            cursor.close();
        }

        return contactEntry;
    }

    private class ContactEntryCursorWrapper extends CursorWrapper {

        public ContactEntryCursorWrapper(Cursor cursor) {
            super(cursor);
        }
    }
}
