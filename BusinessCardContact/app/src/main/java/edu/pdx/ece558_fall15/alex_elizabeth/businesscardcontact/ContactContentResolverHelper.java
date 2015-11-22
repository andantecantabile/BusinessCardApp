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
        if(contactEntry.getName() != null) {
            values.put(StructuredName.DISPLAY_NAME, contactEntry.getName());
        }
        if(contactEntry.getFirstName() != null) {
            values.put(StructuredName.GIVEN_NAME, contactEntry.getFirstName());
        }
        if(contactEntry.getLastName() != null) {
            values.put(StructuredName.FAMILY_NAME, contactEntry.getLastName());
        }
        Uri dataUri = mContext.getContentResolver()
                .insert(Data.CONTENT_URI, values);
    }

    public List<ContactEntry> getAllContacts() {
        List<ContactEntry> contactEntries = new ArrayList<>();
        ContactEntryCursorWrapper cursor = new ContactEntryCursorWrapper(
                mContext.getContentResolver().query(
                        Data.CONTENT_URI,
                        new String[] {RawContacts._ID, RawContacts.ACCOUNT_NAME},
                        RawContacts.ACCOUNT_TYPE + "='" + ACT_TYPE + "'",
                        null,
                        null
                )
        );

        //Navigate through the cursor
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Log.d(TAG, "NEXT ITEM");
            for(int i = 0; i < cursor.getColumnCount(); i++) {
                if(cursor.getString(i) != null) {
                    Log.d(TAG, cursor.getString(i));
                } else {
                    Log.d(TAG, "null");
                }
            }
            cursor.moveToNext();
        }

        return contactEntries;
    }

    public ContactEntry getContact() {
        ContactEntry contactEntry = new ContactEntry();

        return contactEntry;
    }

    private class ContactEntryCursorWrapper extends CursorWrapper {

        public ContactEntryCursorWrapper(Cursor cursor) {
            super(cursor);
        }
    }
}
