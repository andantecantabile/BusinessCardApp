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

    //Fields to hold the chosen parameter values.
    private String language = "English";
    private OutputFormat outputFormat = OutputFormat.xml;
    private ImageSource imageSource = ImageSource.photo;
    private String writeFieldComponents = "true";

    /**
     * Generates the URL parameters that need to be appended to the base URL
     * @return a string with the (relevant) URL parameters formatted correctly
     */
	public String asUrlParams() {
		// For all possible parameters, see documentation at
		// http://ocrsdk.com/documentation/apireference/processBusinessCard/
		return String.format("language=%s&exportFormat=%s&imageSource=%s&xml:writeFieldComponents=%s"
                , language, outputFormat, imageSource, writeFieldComponents);
	}

    /**
     * Defines the possible output return formats
     */
	public enum OutputFormat {
		vCard, xml, csv
	}

    /**
     * Sets the output format
     * @param format the specified output format
     */
	public void setOutputFormat(OutputFormat format) {
		outputFormat = format;
	}

    /**
     * Gets the output format
     * @return the output format
     */
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

    /**
     * Gets the language that is set
     * @return the language
     */
	public String getLanguage() {
		return language;
	}

    /**
     * List of possible image sources
     */
    public enum ImageSource {
        auto, photo, scanner
    }
}
