package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact.contactprovider;


import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.Data;

public class ContactEntryProvider {

    private Context mContext;

    public ContactEntryProvider(Context context) {
        mContext = context;
    }

    public void add() {
        ContentValues values = new ContentValues();
        values.put(Data.RAW_CONTACT_ID, 001);
        values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
        values.put(Email.ADDRESS, "pearsoal@gmail.com");
        values.put(Email.TYPE, Email.TYPE_HOME);
        values.put(Email.LABEL, "Alex's Email");
        Uri dataUri = mContext.getContentResolver().insert(Data.CONTENT_URI, values);
    }

}
