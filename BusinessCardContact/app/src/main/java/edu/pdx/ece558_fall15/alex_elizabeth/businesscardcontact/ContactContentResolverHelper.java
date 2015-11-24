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

public class ContactContentResolverHelper {
    private static final String TAG = "ContactCntResolvHelper";
    private static final String ACT_TYPE = "edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact";

    private Context mContext;

    public ContactContentResolverHelper(Context context) {
        mContext = context;
    }

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

    public List<ContactEntry> getAllContacts() {
        Log.d(TAG, "getAllContacts");
        List<ContactEntry> contactEntries = new ArrayList<>();
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
            Log.d(TAG, "NEXT ITEM");
            long rawContactId = Long.valueOf(cursor.getString(0));
            UUID uuid = UUID.fromString(cursor.getString(1));
            contactEntries.add(getContact(rawContactId, uuid));
            Log.d(TAG, "Name: " + contactEntries.get(contactEntries.size() - 1).getName());
            cursor.moveToNext();
        }
        cursor.close();

        return contactEntries;
    }

    public void deleteContact(ContactEntry contactEntry) {
        Log.d(TAG, "deleteContact");
        Uri contentSyncAdapter = RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build();
        mContext.getContentResolver()
                .delete(contentSyncAdapter,
                        RawContacts.ACCOUNT_NAME + "='" + contactEntry.getId().toString() + "'",
                        null);
    }

    private long addRawContact(UUID uuid) {
        Log.d(TAG, "addRawContact");
        ContentValues person = new ContentValues();
        person.put(RawContacts.ACCOUNT_TYPE, ACT_TYPE);
        person.put(RawContacts.ACCOUNT_NAME, uuid.toString());
        Uri rawContactUri = mContext.getContentResolver()
                .insert(RawContacts.CONTENT_URI, person);
        return ContentUris.parseId(rawContactUri);
    }

    private void addNameData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addNameData");
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        addNameValues(contactEntry, values);
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    private void addEmailData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addEmailData");
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
        addEmailValues(contactEntry, values);
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    private void addPhoneData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addPhoneData");
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        addPhoneValues(contactEntry, values);
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    private void addFaxData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addFaxData");
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        addFaxValues(contactEntry, values);
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    private void addCompanyData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addCompanyData");
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
        addCompanyValues(contactEntry, values);
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    private void addWebsiteData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addWebsiteData");
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Website.CONTENT_ITEM_TYPE);
        addWebsiteValues(contactEntry, values);
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    private void addNoteData(long rawContactId, ContactEntry contactEntry) {
        Log.d(TAG, "addNoteData");
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE);
        addNoteValues(contactEntry, values);
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

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
                                Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "' AND" +
                                Phone.TYPE + "='" + Phone.TYPE_WORK,
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
                                Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "' AND" +
                                Phone.TYPE + "='" + Phone.TYPE_FAX_WORK,
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
            if(contentEntry.getExtension() != null) {
                values.put(Phone.NUMBER, contentEntry.getPhoneNumber() +
                        " " + contentEntry.getExtension());
            } else {
                values.put(Phone.NUMBER, contentEntry.getPhoneNumber());
            }
            values.put(Phone.TYPE, Phone.TYPE_WORK);
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
                                RawContacts.Entity.DATA2, RawContacts.Entity.DATA3 },
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
                    if(mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                        if(data1 != null) {
                            contactEntry.setName(data1);
                        }
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
