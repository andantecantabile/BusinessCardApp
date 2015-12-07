package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.util.Log;

import java.util.UUID;

/**
 * Class that describes an entry in the Android Contact Storage and provides methods
 * to set and get the various pieces of information in the in-memory contact storage
 * representation (as opposed to the file backed Android Contact Storage)
 *
 * @author Alex Pearson & Elizabeth Reed
 * Date:   December 6th, 2015
 */
public class ContactEntry implements Cloneable{

    //Unique Id per contact
    private UUID mId;

    //Information for each contact
    private String mFirstName;
    private String mLastName;
    private String mTitle;
    private String mCompany;
    private String mDivision;
    private String mPhoneNumber;
    private String mExtension;
    private String mFaxNumber;
    private String mEmail;
    private String mWebsite;
    private String mNotes;
    private String mPhotoFilePath;
    private String mBCFilePath;

    /**
     * Constructs a new empty contact with a random Id
     */
    public ContactEntry() {
        this(UUID.randomUUID());
    }

    /**
     * Constructs a empty contact with the specified Id
     * Should only be used when creating a contact from an existing record
     * @param id Id to be used to re-create the contact entry
     */
    public ContactEntry(UUID id) {
        mId = id;
    }

    /**
     * Gets the suggested filename of the picture of the contact
     * @return the suggested filename of the picture of the contact
     */
    public String getSuggestedPhotoFilename() {
        return "IMG_" + getId().toString() + "_photo";
    }

    /**
     * Gets the suggested filename of the picture of the business card
     * @return the suggested filename of the picture of the business card
     */
    public String getSuggestedBCFilename() {
        return "IMG_" + getId().toString() + "_bc";
    }

    /**
     * Gets the filename of the picture of the contact
     * @return the filename of the picture of the contact
     */
    public String getPhotoFilePath() {
        return mPhotoFilePath;
    }

    /**
     * Sets the filename of the picture of the contact
     * @param photoFilePath the filename of the picture of the contact
     */
    public void setPhotoFilePath(String photoFilePath) {
        mPhotoFilePath = photoFilePath;
    }

    /**
     * Gets the filename of the business card image of the contact
     * @return the filename of the business card image of the contact
     */
    public String getBCFilePath() {
        return mBCFilePath;
    }

    /**
     * Sets the filename of the business card image of the contact
     * @param bCFilePath the filename of the business card image of the contact
     */
    public void setBCFilePath(String bCFilePath) {
        mBCFilePath = bCFilePath;
    }

    /**
     * Gets the Id
     * @return the Id
     */
    public UUID getId() {
        return mId;
    }

    /**
     * Gets the name
     * @return the concatenation of first and last name
     */
    public String getName() {
        if(mFirstName == null) {
            return null;
        }
        if(mLastName == null) {
            return mFirstName;
        }
        return String.format("%s %s",mFirstName,mLastName);
    }

    /**
     * Gets the first name
     * @return the first name
     */
    public String getFirstName() {
        return mFirstName;
    }

    /**
     * Gets the last name
     * @return the last name
     */
    public String getLastName() {
        return mLastName;
    }

    /**
     * Set the name; the name is stored as first and last
     * @param name the name
     */
    public void setName(String name) {
        int split = name.indexOf(' ');
        if(split < 0) {
            mFirstName = name;
            mLastName = null;
        } else {
            mFirstName = name.substring(0, split);
            mLastName = name.substring(split + 1, name.length());
        }
    }

    /**
     * Get the title
     * @return the tile
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Set the title
     * @param title the title
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * Get the company
     * @return the company
     */
    public String getCompany() {
        return mCompany;
    }

    /**
     * Set the company
     * @param company the company
     */
    public void setCompany(String company) {
        mCompany = company;
    }

    /**
     * Get the division/group
     * @return the division/group
     */
    public String getDivision() {
        return mDivision;
    }

    /**
     * Set the division/group
     * @param division the division/group
     */
    public void setDivision(String division) {
        mDivision = division;
    }

    /**
     * Get the phone number
     * @return the phone number
     */
    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    /**
     * Set the phone number
     * @param phoneNumber the phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    /**
     * Get the extension
     * @return the extension
     */
    public String getExtension() {
        return mExtension;
    }

    /**
     * Set the extension
     * @param extension the extension
     */
    public void setExtension(String extension) {
        mExtension = extension;
    }

    /**
     * Get the fax number
     * @return the fax number
     */
    public String getFaxNumber() {
        return mFaxNumber;
    }

    /**
     * Set the fax number
     * @param faxNumber the fax number
     */
    public void setFaxNumber(String faxNumber) {
        mFaxNumber = faxNumber;
    }

    /**
     * Get the email
     * @return the email
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Set the email
     * @param email the email
     */
    public void setEmail(String email) {
        mEmail = email;
    }

    /**
     * Get the website
     * @return the website
     */
    public String getWebsite() {
        return mWebsite;
    }

    /**
     * Set the website
     * @param website the website
     */
    public void setWebsite(String website) {
        mWebsite = website;
    }

    /**
     * Get the notes
     * @return the notes
     */
    public String getNotes() {
        return mNotes;
    }

    /**
     * Set the notes
     * @param notes the notes
     */
    public void setNotes(String notes) {
        mNotes = notes;
    }

    public Object clone() {
        ContactEntry contactEntry = null;
        try {
            //Call the super class method to clone all of the instance variables
            contactEntry = (ContactEntry)super.clone();
        } catch (CloneNotSupportedException e){
            Log.e("ContactEntry", "Cloning didn't work");
        }
        //Call the UUID fromString method to "deep" clone the instance object references
        contactEntry.mId = UUID.fromString(mId.toString());

        //Return the newly created ContactEntry that is a "cloned copy" of the original
        return contactEntry;
    }
}
