package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import java.io.File;

/**
 * Class to help facilitate the creation of ContactEntry objects
 * through the use of a fluent interface.  The ContactEntryBuilder
 * can be used to create new ContactEntry objects or modify ContactEntry
 * objects from existing Contact Entry objects.
 */
public class ContactEntryBuilder {
    //ContactEntry associated with this ContactEntryBuilder
    private ContactEntry mContactEntry;

    /**
     * Constructor for the ContactEntryBuilder, provide null to create a new
     * ContactEntry or an existing ContactEntry to modify it
     * @param contactEntry an existing ContactEntry or null
     */
    public ContactEntryBuilder(ContactEntry contactEntry) {
        mContactEntry = contactEntry == null ? new ContactEntry() : contactEntry;
    }

    /**
     * Set the name of the ContactEntry
     * @param name the name
     * @return the ContactEntryBuilder
     */
    public ContactEntryBuilder name(String name) {
        mContactEntry.setName(name);
        return this;
    }

    /**
     * Set the title of the ContactEntry
     * @param title the title
     * @return the ContactEntryBuilder
     */
    public ContactEntryBuilder title(String title) {
        mContactEntry.setTitle(title);
        return this;
    }

    /**
     * Set the company of the ContactEntry
     * @param company the company
     * @return the ContactEntryBuilder
     */
    public ContactEntryBuilder company(String company) {
        mContactEntry.setCompany(company);
        return this;
    }

    /**
     * Set the division of the ContactEntry
     * @param division the division
     * @return the ContactEntryBuilder
     */
    public ContactEntryBuilder division(String division) {
        mContactEntry.setDivision(division);
        return this;
    }

    /**
     * Set the phone number and extension of the ContactEntry
     * @param phoneNumber the phone number
     * @param extension the extension
     * @return the ContactEntryBuilder
     */
    public ContactEntryBuilder phoneNumber(String phoneNumber, String extension) {
        if(phoneNumber != null) {
            mContactEntry.setPhoneNumber(phoneNumber);
        }
        if(extension != null) {
            mContactEntry.setExtension(extension);
        }
        return this;
    }

    /**
     * Set the fax number of the ContactEntry
     * @param faxNumber the fax number
     * @return the ContactEntryBuilder
     */
    public ContactEntryBuilder faxNumber(String faxNumber) {
        mContactEntry.setFaxNumber(faxNumber);
        return this;
    }

    /**
     * Set the email of the ContactEntry
     * @param email the email
     * @return the ContactEntryBuilder
     */
    public ContactEntryBuilder email(String email) {
        mContactEntry.setEmail(email);
        return this;
    }

    /**
     * Set the website of the ContactEntry
     * @param website the website
     * @return the ContactEntryBuilder
     */
    public ContactEntryBuilder website(String website) {
        mContactEntry.setWebsite(website);
        return this;
    }

    /**
     * Set the notes of the ContactEntry
     * @param notes the notes
     * @return the ContactEntryBuilder
     */
    public ContactEntryBuilder notes(String notes) {
        mContactEntry.setNotes(notes);
        return this;
    }

    public ContactEntryBuilder photo(File photoFile) {
        if(photoFile != null) {
            mContactEntry.setPhotoFilePath(photoFile.getPath());
        }
        return this;
    }

    public ContactEntryBuilder businessCard(File businessCardFile) {
        if(businessCardFile != null) {
            mContactEntry.setBCFilePath(businessCardFile.getPath());
        }
        return this;
    }

    /**
     * Creates the ContactEntry from the ContactEntryBuilder
     * @return the ContactEntry
     */
    public ContactEntry build() {
        return mContactEntry;
    }
}
