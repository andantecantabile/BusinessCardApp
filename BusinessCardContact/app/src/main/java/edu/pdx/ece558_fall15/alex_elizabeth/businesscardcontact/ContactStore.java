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

    //Temporary storage point for a contact that isn't committed to the Android Contact
    //Storage but also needs to be accessed across multiple Activities
    private ContactEntry mTempContactEntry;

    /**
     * Creates (or returns if already created) the reference to the ContactStore
     * @param context The context from which this is invoked
    **/
    public static ContactStore get(Context context) {
        if(sContactStore == null) {
            //Create a new one only if it doesn't exist
            sContactStore = new ContactStore(context);
        }
        //Return the old one if it exists or the new one if it didn't
        return sContactStore;
    }

    /**
     * Constructor to create ContactStore, sets the context and creates the
     * ContactContentResolverHelper to interact with the Android Contact Storage
     * @param context The context from which this is invoked
     */
    private ContactStore(Context context) {
        mContext = context;

        //Create the ContactContentResolverHelper to use to write to the database
        mResolverHelper = new ContactContentResolverHelper(mContext);

        //Update the local copy list of entries
        mContactEntries = mResolverHelper.getAllContacts();
    }

    /**
     * Set a temporary contact entry
     * @param ce Temporary ContactEntry to set
     */
    public void setTemporaryContact(ContactEntry ce) {
        mTempContactEntry = ce;
    }

    /**
     * Get a temporary contact entry
     * @return the temporary ContactEntry
     */
    public ContactEntry getTemporaryContact() {
        return mTempContactEntry;
    }

    /**
     * Adds an entry to the Android Contact Storage
     * @param ce The ContactEntry to add
     */
    public void addContactEntry(ContactEntry ce) {
        //Add a new contact to the database
        mResolverHelper.addNewContact(ce);

        //Update the local copy list of entries
        mContactEntries = mResolverHelper.getAllContacts();
    }

    /**
     * Deletes an entry in the Android Contact Storage
     * @param ce The ContactEntry to delete
     */
    public void deleteContactEntry(ContactEntry ce) {
        //Delete an existing contact from the database
        mResolverHelper.deleteContact(ce);

        //Update the local copy list of entries
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
        //Check the normal list for the contact
        for(int i = 0; i < mContactEntries.size(); i++) {
            if(mContactEntries.get(i).getId().equals(id)) {
                return (ContactEntry) mContactEntries.get(i).clone();
            }
        }

        //If it's not found in the normal list check the temporary contact for a match
        if(mTempContactEntry.getId().equals(id)) {
            return mTempContactEntry;
        }

        //Return null if it's not found at all
        return null;
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
        return new File(filename);
    }

    /**
     * Updates the values of an already existing contact in the Android Contact Store
     * @param ce The ContactEntry to be modified
     */
    public void updateContactEntry(ContactEntry ce) {
        //Update an existing contact in the database
        mResolverHelper.updateContact(ce);

        //Update the local copy list of entries
        mContactEntries = mResolverHelper.getAllContacts();
    }

    /**
     * Gets the ContactEntry at a specified position
     * @param position position in the list to get the ContactEntry from
     * @return the ContactEntry at the specified position
     */
    public ContactEntry getContactEntryAtPosition(int position) {
        //Return clones of the ContactEntry so accidental modifications aren't made
        return (ContactEntry) mContactEntries.get(position).clone();
    }
}
