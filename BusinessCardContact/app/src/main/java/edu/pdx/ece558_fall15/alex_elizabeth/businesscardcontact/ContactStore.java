package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The ContactStore class provides a single point of interaction between the view/controller
 * portion of the application and the data model.  All interactions with the list of
 * ContactEntries such as add, modify, delete, and list all go through this class.
**/
public class ContactStore {

    //Variable to store singleton reference
    private static ContactStore sContactStore;

    //Context from which the class is invoked.
    private Context mContext;
    //Reference to the class that interacts with the Android Contact List
    private ContactContentResolverHelper mResolverHelper;
    //List to store a local copy to facilitate quick access
    private List<ContactEntry> mContactEntries = new ArrayList<ContactEntry>();
    private ContactEntry mTempContactEntry;
    /**
     * Creates (or returns if already created) the reference to the ContactStore
     * @param context The context from which this is invoked
    **/
    public static ContactStore get(Context context) {
        if(sContactStore == null) {
            sContactStore = new ContactStore(context);
        }
        return sContactStore;
    }

    /**
     * Constructor to create ContactStore, sets the context and creates the
     * ContactContentResolverHelper to interact with the Android Contact Storage
     * @param context The context from which this is invoked
     */
    private ContactStore(Context context) {
        mContext = context;
        mResolverHelper = new ContactContentResolverHelper(mContext);
        mContactEntries = mResolverHelper.getAllContacts();

        //Uncomment to delete all contacts
        //for(int i = 0; i < mContactEntries.size(); i++) {
        //   deleteContactEntry(mContactEntries.get(i));
        //}
    }

    public void setTemporaryContact(ContactEntry ce) {
        mTempContactEntry = ce;
    }

    /**
     * Adds an entry to the Android Contact Storage
     * @param ce The ContactEntry to add
     */
    public void addContactEntry(ContactEntry ce) {
        mResolverHelper.addNewContact(ce);
        mContactEntries = mResolverHelper.getAllContacts();
    }

    /**
     * Deletes an entry in the Android Contact Storage
     * @param ce The ContactEntry to delete
     */
    public void deleteContactEntry(ContactEntry ce) {
        mResolverHelper.deleteContact(ce);
        mContactEntries = mResolverHelper.getAllContacts();
    }

    /**
     * Gets the list of ContactEntries from Android Contact Storage
     * @return List of ContactEntry objects
     */
    public List<ContactEntry> getContactEntries() {
        return mContactEntries;
    }

    /**
     * Gets a specific ContactEntry that has the specified Id
     * @param id Id of the ContactEntry to retrieve
     * @return The specified ContactEntry
     */
    public ContactEntry getContactEntry(UUID id) {
        for(int i = 0; i < mContactEntries.size(); i++) {
            if(mContactEntries.get(i).getId().equals(id)) {
                return mContactEntries.get(i);
            }
        }
        if(mTempContactEntry.getId().equals(id)) {
            return mTempContactEntry;
        }
        return null;
    }

    /**
     * Gets the position of the ContactEntry with the specified Id
     * @param id Id of the ContactEntry to get the position of
     * @return The position of the specified ContactEntry in the list
     */
    public int getContactEntryPosition(UUID id) {
        for(int i = 0; i < mContactEntries.size(); i++) {
            if(mContactEntries.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get a File reference to the ContactEntry photo
     * @param ce ContactEntry to get the photo of
     * @return File reference to the photo
     */
    public File getPhotoFile(ContactEntry ce) {
        return getImageFile(ce.getPhotoFilePath());
    }

    /**
     * Get a File reference to the suggested ContactEntry photo
     * @param ce ContactEntry to get the suggested photo location of
     * @return File reference to the suggested photo location
     */
    public File getSuggestedPhotoFile(ContactEntry ce) {
        return getImageFile(ce.getSuggestedPhotoFilename());
    }

    /**
     * Get a File reference to the ContactEntry business card photo
     * @param ce ContactEntry to get the photo of
     * @return File reference to the business card photo
     */
    public File getBCPhotoFile(ContactEntry ce) {
        return getImageFile(ce.getBCFilePath());
    }

    /**
     * Get a File reference to the suggested ContactEntry business card photo
     * @param ce ContactEntry to get the suggested business card photo location of
     * @return File reference to the suggested business card photo location
     */
    public File getSuggestedBCFile(ContactEntry ce) {
        return getImageFile(ce.getSuggestedBCFilename());
    }

    /**
     * Internal method to get a File from a filename
     * @param filename filename to get a File from
     * @return a File reference from a specified filename
     */
    private File getImageFile(String filename) {
        if(filename == null) {
            return null;
        }
        /*File externalFilesDir = mContext
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(externalFilesDir == null) {
            return null;
        }

        return new File(externalFilesDir, filename);*/
        return new File(filename);
    }

    /**
     * Updates the values of an already existing contact in the Android Contact Store
     * @param ce The ContactEntry to be modified
     */
    public void updateContactEntry(ContactEntry ce) {
        mResolverHelper.updateContact(ce);
        mContactEntries = mResolverHelper.getAllContacts();
    }

    /**
     * Gets the ContactEntry at a specified position
     * @param position position in the list to get the ContactEntry from
     * @return the ContactEntry at the specified position
     */
    public ContactEntry getContactEntryAtPosition(int position) {
        return mContactEntries.get(position);
    }
}
