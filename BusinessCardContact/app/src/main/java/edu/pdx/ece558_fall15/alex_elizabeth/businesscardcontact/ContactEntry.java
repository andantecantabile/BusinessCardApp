package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import java.util.UUID;

public class ContactEntry {

    private UUID mId;
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

    public ContactEntry() {
        this(UUID.randomUUID());
    }

    private ContactEntry(UUID id) {
        mId = id;
    }

    //Returns the filename of the picture of the contact
    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + "_photo.jpg";
    }

    //Returns the filename of the picture of the business card
    public String getBCPhotoFilename() {
        return "IMG_" + getId().toString() + "_bc.jpg";
    }

    public UUID getId() {
        return mId;
    }

    public String getName() {
        if(mLastName == null) {
            return mFirstName;
        }
        return String.format("%s %s",mFirstName,mLastName);
    }

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

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getCompany() {
        return mCompany;
    }

    public void setCompany(String company) {
        mCompany = company;
    }

    public String getDivision() {
        return mDivision;
    }

    public void setDivision(String division) {
        mDivision = division;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getExtension() {
        return mExtension;
    }

    public void setExtension(String extension) {
        mExtension = extension;
    }

    public String getFaxNumber() {
        return mFaxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        mFaxNumber = faxNumber;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getWebsite() {
        return mWebsite;
    }

    public void setWebsite(String website) {
        mWebsite = website;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }
}
