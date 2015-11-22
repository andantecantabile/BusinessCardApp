package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

public class ContactEntryBuilder {
    private ContactEntry mContactEntry;

    public ContactEntryBuilder() {
        mContactEntry = new ContactEntry();
    }

    public ContactEntryBuilder(ContactEntry contactEntry) { mContactEntry = contactEntry; }

    public ContactEntryBuilder name(String name) {
        mContactEntry.setName(name);
        return this;
    }

    public ContactEntryBuilder title(String title) {
        mContactEntry.setTitle(title);
        return this;
    }

    public ContactEntryBuilder company(String company) {
        mContactEntry.setCompany(company);
        return this;
    }

    public ContactEntryBuilder division(String division) {
        mContactEntry.setDivision(division);
        return this;
    }

    public ContactEntryBuilder phoneNumber(String phoneNumber, String extension) {
        mContactEntry.setPhoneNumber(phoneNumber);
        mContactEntry.setExtension(extension);
        return this;
    }

    public ContactEntryBuilder faxNumber(String faxNumber) {
        mContactEntry.setFaxNumber(faxNumber);
        return this;
    }

    public ContactEntryBuilder email(String email) {
        mContactEntry.setEmail(email);
        return this;
    }

    public ContactEntryBuilder website(String website) {
        mContactEntry.setWebsite(website);
        return this;
    }

    public ContactEntryBuilder notes(String notes) {
        mContactEntry.setNotes(notes);
        return this;
    }

    public ContactEntry build() {
        return mContactEntry;
    }
}
