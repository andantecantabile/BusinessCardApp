package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AbbyyResponseXmlParser {
    private static final String ns = null;

    public ContactEntry parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readResponse(parser);
        } finally {
            in.close();
        }
    }

    private ContactEntry readResponse(XmlPullParser parser) throws XmlPullParserException, IOException {
        List fields = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "document");
        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            //Starts by looking for the businessCard tag
            if(name.equals("businessCard")) {
                fields = readBusinessCard(parser);
            } else {
                skip(parser);
            }
        }
        return createNewContactEntryFromFields(fields);
    }

    private ContactEntry createNewContactEntryFromFields(List<Fields> fields) {
        String phoneNumber = null;
        String extension = null;
        String faxNumber = null;
        String email = null;
        String name = null;
        String website = null;
        String company = null;
        String jobTitle = null;
        String division = null;
        for(int i = 0; i < fields.size(); i++) {
            String type = fields.get(i).type;
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
                List<Fields.FieldComponents> f = fields.get(i).fieldComponents;
                if(f.size() == 0) {
                    jobTitle = fields.get(i).value;
                } else {
                    for (int j = 0; j < f.size(); j++) {
                        String innerType = f.get(j).type;
                        if (innerType.equalsIgnoreCase("jobposition")) {
                            jobTitle = f.get(j).value;
                        } else if (innerType.equalsIgnoreCase("jobdepartment")) {
                            division = f.get(j).value;
                        }
                    }
                }
            }
        }
        ContactEntry contactEntry = new ContactEntryBuilder(null)
                .phoneNumber(phoneNumber, extension)
                .faxNumber(faxNumber)
                .email(email)
                .website(website)
                .name(name)
                .company(company)
                .title(jobTitle)
                .division(division)
                .build();
        return contactEntry;
    }

    public static class Fields {
        public final String type;
        public final String value;
        public final List<FieldComponents> fieldComponents;

        private Fields(String type, String value, List<FieldComponents> fieldComponents) {
            this.type = type;
            this.value = value;
            this.fieldComponents = fieldComponents;
        }

        public static class FieldComponents {
            public final String type;
            public final String value;

            private FieldComponents(String type, String value) {
                this.type = type;
                this.value = value;
            }
        }
    }

    private List readBusinessCard(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "businessCard");
        List fields = new ArrayList();
        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if(name.equals("field")) {
                fields.add(readField(parser));
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "businessCard");
        return fields;
    }

    private Fields readField(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "field");
        String type;
        String value = null;
        List<Fields.FieldComponents> fieldComponents = new ArrayList<>();
        type = parser.getAttributeValue(null, "type");

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if(name.equals("value")) {
                value = readValue(parser);
            } else if(name.equals("fieldComponents")) {
                fieldComponents = readFieldComponents(parser);
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "field");
        return new Fields(type, value, fieldComponents);
    }

    private String readValue(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "value");
        String value = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "value");
        return value;
    }

    private List readFieldComponents(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "fieldComponents");
        List<Fields.FieldComponents> fieldComponents = new ArrayList<>();
        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if(name.equals("fieldComponent")) {
                fieldComponents.add(readFieldComponent(parser));
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "fieldComponents");
        return fieldComponents;
    }

    private Fields.FieldComponents readFieldComponent(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "fieldComponent");
        String type = parser.getAttributeValue(null, "type");
        String value = null;
        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if(name.equals("value")) {
                value = readValue(parser);
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "fieldComponent");
        return new Fields.FieldComponents(type, value);
    }

    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";
        if(parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if(parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch(parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
