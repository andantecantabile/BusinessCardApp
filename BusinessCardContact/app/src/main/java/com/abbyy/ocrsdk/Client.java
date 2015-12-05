package com.abbyy.ocrsdk;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Client class to handle internet connection and sending/receiving data from the server.
 * Initial version from ABBYY Cloud OCR example.  Modified by Alex Pearson.
 */
public class Client {
	//Fields for the application ID and password
	public String applicationId;
	public String password;

    //URL for the Cloud OCR SDK server
	public String serverUrl = "http://cloud.ocrsdk.com";

    /**
     * Method to create the full URL from the filepath and parameters, creates the Http connection,
     * and uploads the file
     * @param filePath String for the image to upload
     * @param settings Settings for processing the uploaded image
     * @return the server response as a Task status
     * @throws Exception If issues with reading the files or accessing the internet occur
     */
	public Task processBusinessCard(String filePath, BusCardSettings settings)
			throws Exception {

        //Create the URL
		URL url = new URL(serverUrl + "/processBusinessCard?"
				+ settings.asUrlParams());
        //Read in the file
		byte[] fileContents = readDataFromFile(filePath);

        //Create a connection to the server
		HttpURLConnection connection = openPostConnection(url);

        //Write the contents to the server
		connection.setRequestProperty("Content-Length",
				Integer.toString(fileContents.length));
		connection.getOutputStream().write(fileContents);

        //Get the response from the server and return the task status
		return getResponse(connection);
	}

    /**
     * Creates the URL to check the task status
     * @param taskId ID of the task to check on
     * @return the task status
     * @throws Exception If issues with the internet connection
     */
	public Task getTaskStatus(String taskId) throws Exception {
        //Create the URL
		URL url = new URL(serverUrl + "/getTaskStatus?taskId=" + taskId);

        //Check on the task status and return it
		HttpURLConnection connection = openGetConnection(url);
		return getResponse(connection);
	}

    /**
     * Method to download the result when the task is finished
     * @param task task to download the result from
     * @param out FileOutputStream to accept the data from
     * @throws Exception If issues with the internet connection
     */
	public void downloadResult(Task task, FileOutputStream out) throws Exception {
        //Throw an exception if the task wasn't completed correctly
		if (task.Status != Task.TaskStatus.Completed) {
			throw new IllegalArgumentException("Invalid task status");
		}

        //If the URL is null, throw an exception
		if (task.DownloadUrl == null) {
			throw new IllegalArgumentException(
					"Cannot download result without url");
		}

        //Create a connection to download the response
		URL url = new URL(task.DownloadUrl);
		URLConnection connection = url.openConnection(); // do not use
															// authenticated
															// connection

        //Create a reader around the input stream returned
		BufferedInputStream reader = new BufferedInputStream(
				connection.getInputStream());

        //Read the data
		byte[] data = new byte[1024];
		int count;
		while ((count = reader.read(data, 0, data.length)) != -1) {
			out.write(data, 0, count);
		}
	}

	/** Activate application on a new mobile device.
	 * @param deviceId string that uniquely identifies current device
	 * @return string that should be added to application id for all API calls
	 * @throws Exception
	 */
	public String activateNewInstallation(String deviceId) throws Exception {
        //Create the URL
		URL url = new URL(serverUrl + "/activateNewInstallation?deviceId=" + deviceId);

        //Open tuhe connection
		HttpURLConnection connection = openGetConnection(url);

        //Get the HTTP response code
		int responseCode = connection.getResponseCode();

        //If the response good is OKAY
		if (responseCode == 200) {
            //Create an input stream and reader
			InputStream inputStream = connection.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader reader = new BufferedReader(inputStreamReader);

            //Parse the response to get the authToken
			InputSource source = new InputSource();
			source.setCharacterStream(reader);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(source);

			NodeList nodes = doc.getElementsByTagName("authToken");
			Element authTokenNode = (Element) nodes.item(0);
			
			Node textNode = authTokenNode.getFirstChild();

            //Return the authToken or null if one doesn't exist
			String installationId = textNode != null ? textNode.getNodeValue() : "";
			if( installationId == null) 
				installationId = "";
			
			return installationId;
		//If the response code isn't OKAY
		} else {
            //Throw an exception with the response code
			String response = connection.getResponseMessage();
			throw new Exception(response);
		}
	}

    /**
     * Create a POST connection with the required settings
     * @param url URL to connect to
     * @return the connection that is created
     * @throws Exception If there are issues with the internet
     */
	private HttpURLConnection openPostConnection(URL url) throws Exception {
        //Create a new connection
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        //We need input and output capabilities
		connection.setDoOutput(true);
		connection.setDoInput(true);

        //We are using POST to access the REST API
		connection.setRequestMethod("POST");

        //We need authorization
		setupAuthorization(connection);

        //Setup the what type of request we are getting back
		connection
				.setRequestProperty("Content-Type", "application/octet-stream");

        //Return the created connection
		return connection;
	}

    /**
     * Create a GET connection with the required settings
     * @param url URL to connect to
     * @return the connection that is created
     * @throws Exception If there are issues with the internet
     */
	private HttpURLConnection openGetConnection(URL url) throws Exception {
        //Create the connection
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		// connection.setRequestMethod("GET");

        //We need authorization
		setupAuthorization(connection);

        //Return the created connection
		return connection;
	}

    /**
     * Setup authorization by adding the request property with the user's password
     * @param connection the connection that the property was added to
     */
	private void setupAuthorization(URLConnection connection) {
        //Encode the user's password
		String authString = "Basic " + encodeUserPassword();
		authString = authString.replaceAll("\n", "");

        //Add the request property to the connection
		connection.addRequestProperty("Authorization", authString);
	}

    /**
     * Read the data from the file into a byte[]
     * @param filePath the filepath to read from
     * @return the byte[] of read data
     * @throws Exception If anything goes wrong reading the file
     */
	private byte[] readDataFromFile(String filePath) throws Exception {

        //Create a new File for the given filepath
		File file = new File(filePath);

        //Get the length of the file
		long fileLength = file.length();

        //Create a buffer to hold the read-in data
		byte[] dataBuffer = new byte[(int) fileLength];

        //Create an input stream to read in the data
		InputStream inputStream = new FileInputStream(file);

		try {
			int offset = 0;

            //Read in the file as long as there is still data
			while (true) {
                //Break if we are past the end of the file
				if (offset >= dataBuffer.length) {
					break;
				}

                //Read data and how much we read
				int numRead = inputStream.read(
						dataBuffer, offset, dataBuffer.length - offset);

                //Break if nothing was read
				if (numRead < 0) {
					break;
				}

                //Increase the offset of where we are in the file
				offset += numRead;
			}

            //If something went wrong and we couldn't read the entire file
			if (offset < dataBuffer.length) {
				throw new IOException(
						"Could not completely read file " + file.getName());
			}
		} finally {
            //Close the input stream
			inputStream.close();
		}

        //Return the extracted data
		return dataBuffer;
	}

    /**
     * Encode the user's password to send
     * @return the encoded password
     */
	private String encodeUserPassword() {
		String toEncode = applicationId + ":" + password;
		return Base64.encode(toEncode);
	}

	/**
	 * Read server response from HTTP connection and return task description.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	private Task getResponse(HttpURLConnection connection) throws Exception {
        //Get the response code
		int responseCode = connection.getResponseCode();

        //If the response code is OKAY
		if (responseCode == 200) {

            //Create a input stream and reader and create a new Task
			InputStream inputStream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			return new Task(reader);
		} else if (responseCode == 401) {
            //If the response code indicates we are unauthorized
			throw new Exception(
					"HTTP 401 Unauthorized. Please check your application id and password");
		} else if (responseCode == 407) {
            //If the response code indicates we had an issue with the proxy
			throw new Exception("HTTP 407. Proxy authentication error");
		} else {
            //Otherwise try to get the error and throw it
			String message = "";
			try {
				InputStream errorStream = connection.getErrorStream();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(errorStream));

				// Parse xml error response
				InputSource source = new InputSource();
				source.setCharacterStream(reader);
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(source);
				
				NodeList error = doc.getElementsByTagName("error");
				Element err = (Element) error.item(0);
				
				message = err.getTextContent();
			} catch (Exception e) {
                //Throw an exception if we had an issue getting the error response
				throw new Exception("Error getting server response");
			}

            //Throw an exception with the returned response
			throw new Exception("Error: " + message);
		}
	}

}
