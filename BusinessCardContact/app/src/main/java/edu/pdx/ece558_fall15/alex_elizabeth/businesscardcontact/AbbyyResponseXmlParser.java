package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a helper class that uses an XMLPullParser to extract data from the XML file
 * returned as a result of querying the Abbyy Cloud OCR SDK server.
 */
public class AbbyyResponseXmlParser {
    //Don't worry about namespaces
    private static final String ns = null;

    /**
     * High level method to call to create a ContactEntry from a XML response.
     * @param in an input stream wrapped around the XML file
     * @return A new ContactEntry with all of the extracted data populated
     * @throws XmlPullParserException If something goes wrong with the XmlPullParser
     * @throws IOException If something goes wrong with reading the file
     */
    public ContactEntry parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            //Setup the newly created XmlPullParser
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);

            //Read the first tag
            parser.nextTag();

            //Return the generated ContactEntry
            return readResponse(parser);
        } finally {
            //Make sure to cleanup the input stream by closing
            in.close();
        }
    }

    /**
     * Read the file and generate a ContactEntry from the returned data
     * @param parser The previously created XmlPullParser
     * @return The newly created ContactEntry
     * @throws XmlPullParserException If something goes wrong with the XmlPullParser
     * @throws IOException If something goes wrong with reading the file
     */
    private ContactEntry readResponse(XmlPullParser parser) throws XmlPullParserException, IOException {
        List fields = new ArrayList();

        //Require that the start tag is document
        parser.require(XmlPullParser.START_TAG, ns, "document");

        //Continue until an end tag is found
        while(parser.next() != XmlPullParser.END_TAG) {
            //If it's not a start tag, skip
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            //Starts by looking for the businessCard tag
            if(name.equals("businessCard")) {
                fields = readBusinessCard(parser);
            } else {
                //If it's not a businessCard tag, skip
                skip(parser);
            }
        }
        //Create the ContactEntry from the extracted data and return it
        return createNewContactEntryFromFields(fields);
    }

    /**
     * Create a new ContactEntry from the extracted Xml Data
     * @param fields List of extracted Xml data
     * @return A new ContactEntry with all of the parameters set to the extracted values
     */
    private ContactEntry createNewContactEntryFromFields(List<Fields> fields) {
        //Setup variables to hold the values
        String phoneNumber = null;
        String extension = null;
        String faxNumber = null;
        String email = null;
        String name = null;
        String website = null;
        String company = null;
        String jobTitle = null;
        String division = null;

        //Iterate over all of the extracted data
        for(int i = 0; i < fields.size(); i++) {
            String type = fields.get(i).type;

            //Compare the type to see if it's data we want
            if(type.equalsIgnoreCase("phone")) {
                phoneNumber = fields.get(i).value;
            } else if (type.equalsIgnoreCase("fax")) {
                faxNumber = fields.get(i).value;
            } else if (type.equalsIgnoreCase("email")) {
                email = fields.get(i).value;
            } else if (type.equalsIgnoreCase("web")) {
                website = fields.get(i).value;
            } else if (type.equalsIgnoreCase("name")) {
                name = fields.get(i).value;
            } else if (type.equalsIgnoreCase("company")) {
                company = fields.get(i).value;
            } else if (type.equalsIgnoreCase("job")) {
                //Job can have nested data that we want
                List<Fields.FieldComponents> f = fields.get(i).fieldComponents;

                //But if it doesn't we just use the data at this level
                if(f.size() == 0) {
                    jobTitle = fields.get(i).value;
                } else {
                    //If there is nested data, loop over it
                    for (int j = 0; j < f.size(); j++) {
                        String innerType = f.get(j).type;

                        //Compare types again to see if it's data we want
                        if (innerType.equalsIgnoreCase("jobposition")) {
                            jobTitle = f.get(j).value;
                        } else if (innerType.equalsIgnoreCase("jobdepartment")) {
                            division = f.get(j).value;
                        }
                    }
                }
            }
        }

        //Use the builder to build a ContactEntry from all of the extracted data
        return new ContactEntryBuilder(null)
                .phoneNumber(phoneNumber, extension)
                .faxNumber(faxNumber)
                .email(email)
                .website(website)
                .name(name)
                .company(company)
                .title(jobTitle)
                .division(division)
                .build();
    }

    /**
     * Fields is a static class that we will map the extracted data from the Xml file into
     */
    public static class Fields {
        //All of the data is public final because we will need to access it outside of this class
        //but we will never set or change it after we call the constructor.
        public final String type;
        public final String value;
        public final List<FieldComponents> fieldComponents;

        /**
         * We will only call the constructor inside this class.
         * @param type Xml type value extracted
         * @param value Xml value value extracted
         * @param fieldComponents List of sub-components that could be extracted
         */
        private Fields(String type, String value, List<FieldComponents> fieldComponents) {
            this.type = type;
            this.value = value;
            this.fieldComponents = fieldComponents;
        }

        /**
         * FieldComponents is a static class that we will map the nested extracted data from the Xml
         * file into
         */
        public static class FieldComponents {
            //All of the data is public final because we will need to access it outside of the class
            //but we will never set or change it after we call the constructor.
            public final String type;
            public final String value;

            /**
             * We will only call the constructor inside this class
             * @param type Xml type value extracted
             * @param value Xml value value extracted
             */
            private FieldComponents(String type, String value) {
                this.type = type;
                this.value = value;
            }
        }
    }

    /**
     * Parse the businessCard tag and it's contents
     * @param parser The previously created XmlPullParser
     * @return The list of extracted "fields"
     * @throws XmlPullParserException If something goes wrong with the XmlPullParser
     * @throws IOException If something goes wrong with reading the file
     */
    private List readBusinessCard(XmlPullParser parser) throws XmlPullParserException, IOException {
        //Require that this start with a "businessCard" tag
        parser.require(XmlPullParser.START_TAG, ns, "businessCard");
        List fields = new ArrayList();

        //Keep extracting data until we hit an end tag
        while(parser.next() != XmlPullParser.END_TAG) {
            //If it's not a start tag, skip over
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            //If it's a "field" tag, extract it and add to the list
            if(name.equals("field")) {
                fields.add(readField(parser));
            } else {
                //If it's a different tag, skip it
                skip(parser);
            }
        }

        //Require that the end tag match the "businessCard" tag
        parser.require(XmlPullParser.END_TAG, ns, "businessCard");

        //Return the list of extracted "fields"
        return fields;
    }

    /**
     * Parse the field tag and it's contents
     * @param parser The previously created XmlPullParser
     * @return The extracted field
     * @throws XmlPullParserException If something goes wrong with the XmlPullParser
     * @throws IOException If something goes wrong with reading the file
     */
    private Fields readField(XmlPullParser parser) throws XmlPullParserException, IOException {
        //Require that the start tag match the "field" tag
        parser.require(XmlPullParser.START_TAG, ns, "field");
        String type;
        String value = null;
        List<Fields.FieldComponents> fieldComponents = new ArrayList<>();

        //Get the "type" attribue from the tag
        type = parser.getAttributeValue(null, "type");

        //Keep extracting data until we get to an end tag
        while(parser.next() != XmlPullParser.END_TAG) {
            //If it's not a start tag, skip it
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            //If it's a "value" tag, extract the value and store it
            if(name.equals("value")) {
                value = readValue(parser);
            } else if(name.equals("fieldComponents")) {
                //If it's a "fieldComponents" tag, extract the contents of the tag and store it
                fieldComponents = readFieldComponents(parser);
            } else {
                //If it's a tag we don't care about, skip it.
                skip(parser);
            }
        }

        //Require that the end tag matches "field"
        parser.require(XmlPullParser.END_TAG, ns, "field");

        //Return a new Fields object with the data set to the extracted data
        return new Fields(type, value, fieldComponents);
    }

    /**
     * Parse the value tag and extract it's contents
     * @param parser The previously created XmlPullParser
     * @return The extracted value
     * @throws XmlPullParserException If something goes wrong with the XmlPullParser
     * @throws IOException If something goes wrong with reading the file
     */
    private String readValue(XmlPullParser parser) throws XmlPullParserException, IOException {
        //Require the start tag to match "value"
        parser.require(XmlPullParser.START_TAG, ns, "value");

        //Read the tag's text
        String value = readText(parser);

        //Require the end tag to match "value"
        parser.require(XmlPullParser.END_TAG, ns, "value");

        //Return the tag's text
        return value;
    }

    /**
     * Parse the fieldComponents value and extract it's contents
     * @param parser The previously created XmlPullParser
     * @return A list of extracted FieldComponents
     * @throws XmlPullParserException If something goes wrong with the XmlPullParser
     * @throws IOException If something goes wrong with reading the file
     */
    private List readFieldComponents(XmlPullParser parser) throws XmlPullParserException, IOException {
        //Require that the start tag matches fieldComponents
        parser.require(XmlPullParser.START_TAG, ns, "fieldComponents");
        List<Fields.FieldComponents> fieldComponents = new ArrayList<>();

        //Keep extracting data until we hit an end tag
        while(parser.next() != XmlPullParser.END_TAG) {
            //If it's not a start tag, skip it
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            //If the tag is a "fieldComponent", extract and add it to the list
            if(name.equals("fieldComponent")) {
                fieldComponents.add(readFieldComponent(parser));
            } else {
                //If it's not a tag we care about, skip it
                skip(parser);
            }
        }

        //Require that the end tag matches fieldComponents
        parser.require(XmlPullParser.END_TAG, ns, "fieldComponents");

        //Return the list of fieldComponents
        return fieldComponents;
    }

    /**
     * Parse a "fieldComponents" tag and it's contents
     * @param parser The previously created XmlPullParser
     * @return An extracted FieldComponent
     * @throws XmlPullParserException If something goes wrong with the XmlPullParser
     * @throws IOException If something goes wrong with reading the file
     */
    private Fields.FieldComponents readFieldComponent(XmlPullParser parser) throws XmlPullParserException, IOException {
        //Require that the start tag matches "fieldComponent"
        parser.require(XmlPullParser.START_TAG, ns, "fieldComponent");

        //Get the attribute of the tag "type"
        String type = parser.getAttributeValue(null, "type");
        String value = null;

        //Keep extracting data until an end tag is reached
        while(parser.next() != XmlPullParser.END_TAG) {
            //If it's not a start tag, skip it
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            //If the tag equals value, extract the value
            if(name.equals("value")) {
                value = readValue(parser);
            } else {
                //If it's not a tag we care about, skip it
                skip(parser);
            }
        }

        //Require that the end tag matches "fieldComponent"
        parser.require(XmlPullParser.END_TAG, ns, "fieldComponent");

        //Return the newly created FieldComponents with the extracted data
        return new Fields.FieldComponents(type, value);
    }

    /**
     * Read the text out of the tag and move to the next tag
     * @param parser The previously created XmlPullParser
     * @return The extracted text
     * @throws XmlPullParserException If something goes wrong with the XmlPullParser
     * @throws IOException If something goes wrong with reading the file
     */
    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";
        if(parser.next() == XmlPullParser.TEXT) {
            //Get the text
            result = parser.getText();

            //Navigate to the next tag
            parser.nextTag();
        }
        return result;
    }

    /**
     * Helper method to skip a tag and all of it's contents
     * @param parser The previously created XmlPullParser
     * @throws XmlPullParserException If something goes wrong with the XmlPullParser
     * @throws IOException If something goes wrong with reading the file
     */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        //Make sure we have a start tag
        if(parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        //Start with a depth of 1 (this tag)
        int depth = 1;

        //While we haven't found the matching end tag
        while (depth != 0) {
            //Get the next tag
            switch(parser.next()) {
                //If it's an end tag, decrement the depth
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                //If it's a start tag, increment the depth
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
