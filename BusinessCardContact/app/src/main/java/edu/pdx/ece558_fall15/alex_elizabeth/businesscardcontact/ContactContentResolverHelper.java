package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
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
    }

    public void updateContact(ContactEntry contactEntry) {
        Log.d(TAG, "updateContact");
        long rawContactId = getRawContact(contactEntry.getId());
        updateNameData(rawContactId, contactEntry);
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
        Log.d(TAG, "Num Rows Updated: " + numRowsModified);
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
