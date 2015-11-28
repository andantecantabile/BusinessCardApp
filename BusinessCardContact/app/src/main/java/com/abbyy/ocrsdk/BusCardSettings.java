/**
 * 
 */
package com.abbyy.ocrsdk;

/**
 * Business card processing settings.
 * Initial version from ABBYY Cloud OCR example.  Modified by Alex Pearson
 * For all possible settings see
 * http://ocrsdk.com/documentation/apireference/processBusinessCard/
 */
public class BusCardSettings {

	public String asUrlParams() {
		// For all possible parameters, see documentation at
		// http://ocrsdk.com/documentation/apireference/processBusinessCard/
		return String.format("language=%s&exportFormat=%s&imageSource=%s&xml:writeFieldComponents=%s"
                , language, outputFormat, imageSource, writeFieldComponents);
	}

	public enum OutputFormat {
		vCard, xml, csv
	}

	public void setOutputFormat(OutputFormat format) {
		outputFormat = format;
	}

	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	
	/*
	 * Set recognition language. You can set any language listed at
	 * http://ocrsdk.com/documentation/specifications/recognition-languages/ or
	 * set comma-separated combination of them.
	 * 
	 * Examples: English English,ChinesePRC English,French,German
	 */
	public void setLanguage(String newLanguage) {
		language = newLanguage;
	}

	public String getLanguage() {
		return language;
	}

    public enum ImageSource {
        auto, photo, scanner;
    }

	private String language = "English";
	private OutputFormat outputFormat = OutputFormat.xml;
	private ImageSource imageSource = ImageSource.photo;
    private String writeExtendedCharacterInfo = "true";
    private String writeFieldComponents = "true";
}
