package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContactStore {
    private static ContactStore sContactStore;

    private Context mContext;
    private ContactContentResolverHelper mResolverHelper;

    //TODO: Replace with ContentProvider
    private List<ContactEntry> mContactEntries = new ArrayList<ContactEntry>();

    public static ContactStore get(Context context) {
        if(sContactStore == null) {
            sContactStore = new ContactStore(context);
        }
        return sContactStore;
    }

    private ContactStore(Context context) {
        mContext = context;
        mResolverHelper = new ContactContentResolverHelper(mContext);
        //TODO: Remove once we can add contacts
        //for testing
        /*ContactEntry ce = new ContactEntryBuilder(null)
                .name("Alex")
                .company("Mentor Graphics")
                .email("Alex_Pearson@mentor.com")
                .website("www.mentor.com")
                .build();

        this.addContactEntry(ce);
        //mResolverHelper.addNewContact(ce);

        ContactEntry ce1 = new ContactEntryBuilder(null)
                .name("Elizabeth")
                .company("Intel")
                .build();

        this.addContactEntry(ce1);
        //mResolverHelper.addNewContact(ce1);

        mResolverHelper.getAllContacts();*/
        //end for testing
    }

    public void addContactEntry(ContactEntry ce) {
        mContactEntries.add(ce);
    }

    public void deleteContactEntry(ContactEntry ce) {
        int position = getContactEntryPosition(ce.getId());
        if(position >= 0) {
            mContactEntries.remove(position);
        }
    }

    public List<ContactEntry> getContactEntries() {
        return mContactEntries;
    }

    public ContactEntry getContactEntry(UUID id) {
        for(int i = 0; i < mContactEntries.size(); i++) {
            if(mContactEntries.get(i).getId().equals(id)) {
                return mContactEntries.get(i);
            }
        }
        return null;
    }

    public int getContactEntryPosition(UUID id) {
        for(int i = 0; i < mContactEntries.size(); i++) {
            if(mContactEntries.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public File getPhotoFile(ContactEntry ce) {
        return getImageFile(ce.getPhotoFilename());
    }

    public File getBCPhotoFile(ContactEntry ce) {
        return getImageFile(ce.getBCPhotoFilename());
    }

    private File getImageFile(String filename) {
        File externalFilesDir = mContext
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(externalFilesDir == null) {
            return null;
        }

        return new File(externalFilesDir, filename);
    }

    public void updateContactEntry(ContactEntry ce) {
        int position = getContactEntryPosition(ce.getId());
        if (position >= 0) {
            mContactEntries.remove(position);
            mContactEntries.add(ce);
        }
    }
}
