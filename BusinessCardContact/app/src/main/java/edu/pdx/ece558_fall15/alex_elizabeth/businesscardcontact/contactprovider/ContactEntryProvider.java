package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact.contactprovider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.util.Log;

import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.Data;
import static android.provider.ContactsContract.RawContacts;

public class ContactEntryProvider {
    private static final String TAG = "ContactEntryProvider";

    private Context mContext;

    public ContactEntryProvider(Context context) {
        mContext = context;
    }

    public void add() {

        ContentValues person = new ContentValues();
        person.put(RawContacts.ACCOUNT_TYPE, "pdx.edu.apearson");
        person.put(RawContacts.ACCOUNT_NAME, "Alex");
        Uri rawContactUri = mContext.getContentResolver().insert(RawContacts.CONTENT_URI, person);
        long rawContactId = ContentUris.parseId(rawContactUri);

        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
        values.put(Email.ADDRESS, "pearsoal@gmail.com");
        values.put(Email.TYPE, Email.TYPE_HOME);
        values.put(Email.LABEL, "Alex's Email");
        Uri dataUri = mContext.getContentResolver().insert(Data.CONTENT_URI, values);
    }

    public void get() {
        Cursor c = mContext.getContentResolver().query(Data.CONTENT_URI,
                new String[] {Data._ID, Email.ADDRESS, Email.TYPE, Email.LABEL},
                Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'",
                null,
                null);
        ContactEntryCursorWrapper cw = new ContactEntryCursorWrapper(c);
        for(int i = 0; i < cw.getColumnCount(); i++) {
            Log.d(TAG, cw.getColumnNames()[i]);
        }
        Log.d(TAG, Integer.toString(cw.getCount()));
        cw.moveToFirst();
        for(int i = 0; i < cw.getCount(); i++) {
            Log.d(TAG, "ITEM: " + i);
            for(int j = 0; j < cw.getColumnCount(); j++) {
                if(cw.getString(j) != null) {
                    Log.d(TAG, cw.getString(j));
                } else {
                    Log.d(TAG, "null");
                }
            }
            cw.moveToNext();
        }
    }

    private class ContactEntryCursorWrapper extends CursorWrapper {

        public ContactEntryCursorWrapper(Cursor cursor) {
            super(cursor);
        }
    }
}
