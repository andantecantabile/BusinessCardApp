package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.abbyy.ocrsdk.BusCardSettings;
import com.abbyy.ocrsdk.Client;
import com.abbyy.ocrsdk.Task;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * AsyncTask that implements the logic necessary to upload the image to the web-based OCR, download
 * the result, and create a new ContactEntry object from the returned data.
 * Parts of this class are adapted from ABBYY Cloud OCR Github Sample Project
 *
 * @author Alex Pearson & Elizabeth Reed
 * Date:   December 6th, 2015
 */
public class OCRAsyncTask extends DialogAsyncTask<String, String, Boolean> {
    //Unique identifiers for this class
    private static final int TASK_ID = 42;
    private static final String TAG = "OCRAsyncTask";
    private static final String instIdName = "installationId";

    //Constructor that calls the super, no other setup is necessary
    public OCRAsyncTask(String initialStatus, Context context, Callbacks callbacks) {
        super(initialStatus, context, callbacks, TASK_ID);
    }

    /**
     * Implementation of doInBackground that uploads an image, waits for the result, downloads
     * the results, parses the result, and creates a new ContactEntry from the result.
     * @param params array of strings, the first string is the input image file, the second is the
     *               temp file that is used to store the xml response from the server
     * @return The boolean result of the processing
     */
    @Override
    protected Boolean doInBackground(String... params) {
        //Get the input file and temp file names
        String inputFile = params[0];
        String tempFile = params[1];

        try {
            //Setup a Client to interact with the cloud OCR service
            Client restClient = new Client();
            //Set the ID and password for our application
            restClient.applicationId = "Business Card Contact";
            restClient.password = "g0aDEnHkRELtduP3qlynsErQ";

            //If we are running for the first time, obtain the installation id
            SharedPreferences settings = super.getContext()
                    .getSharedPreferences("abbyy_settings", Context.MODE_PRIVATE);
            if(!settings.contains(instIdName)) {
                //Get installation id from server using device id
                String deviceId = Settings.Secure
                        .getString(super.getContext().getContentResolver(),
                                Settings.Secure.ANDROID_ID);

                //Obtain installation id from server
                publishProgress( "First run: obtaining installation id..");
                String installationId = restClient.activateNewInstallation(deviceId);
                publishProgress( "Done. Installation id is '" + installationId + "'");

                //Store the Id in the shared preferences
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(instIdName, installationId);
                editor.commit();
            }

            //Get the previously stored Id from shared preferences, and assign it to the client
            String installationId = settings.getString(instIdName, "");
            restClient.applicationId += installationId;

            //Change the message that the dialog displays
            publishProgress("Uploading image...");

            //Setup what we want the settings to be for our processing task
            String language = "English";

            BusCardSettings busCardSettings = new BusCardSettings();
            busCardSettings.setLanguage(language);
            busCardSettings.setOutputFormat(BusCardSettings.OutputFormat.xml);
            Task task = restClient.processBusinessCard(inputFile, busCardSettings);

            //Change the message that the dialog displays
            publishProgress("Uploading...");

            //Check the server until the task is no longer active
            while ( task.isTaskActive() ) {
                // Check on the OCR task status every 3 seconds, more frequently than every
                // 2 seconds is discouraged
                Thread.sleep(3000);

                //Change the message that the dialog displays
                publishProgress("Processing..");

                //Get the status from the server
                task = restClient.getTaskStatus(task.Id);
            }

            //Check if the task completed correctly
            if(task.Status == Task.TaskStatus.Completed) {
                //Change the message that the dialog displays
                publishProgress("Downloading..");

                //Create a new temp file and file output stream to wrap it
                File tmpFile = new File(super.getContext()
                        .getExternalFilesDir(Environment.DIRECTORY_PICTURES),tempFile);
                FileOutputStream fos = new FileOutputStream(tmpFile);

                try {
                    //Download the result into the file
                    restClient.downloadResult(task, fos);
                } finally {
                    //Cleanup the file output stream
                    fos.close();
                }

                //Change the message that the dialog displays
                publishProgress("Ready");
            } else if( task.Status == Task.TaskStatus.NotEnoughCredits ) {
                //If our account doesn't have enough credit to process images
                throw new Exception("Not enough credits to process task. " +
                        "Add more pages to your application's account.");
            } else {
                //If something else went wrong with the processing, image was bad or something
                throw new Exception("Task failed");
            }
        } catch (Exception e) {
            //Other errors that might occur and aren't already handled, task will finish gracefully
            Log.e(TAG, "Error during OCR: " + e.getMessage(), e);
            return false;
        }

        //Process the returned XML file.
        File xmlFile = new File(super.getContext()
              .getExternalFilesDir(Environment.DIRECTORY_PICTURES), tempFile);

        FileInputStream fis;
        try {
            //Parse the file and get a new contactEntry using the AbbyyResponseXmlParser
            fis = new FileInputStream(xmlFile);
            AbbyyResponseXmlParser parser = new AbbyyResponseXmlParser();
            ContactEntry contactEntry = parser.parse(fis);

            //Set the BC image path
            contactEntry.setBCFilePath(new File(inputFile).getPath());

            //Set the newly create ContactEntry to be associated with this task
            super.setContactEntry(contactEntry);

            //Close the file input stream
            fis.close();
        } catch (XmlPullParserException xppe) {
            //Issue with the XML Parser
            Log.e(TAG, "Problem with XML parser: ", xppe);
            return false;
        } catch (FileNotFoundException e) {
            //Issue with not finding the xml file
            Log.e(TAG, "Problem finding the xml file we just created: ", e);
            return false;
        } catch (IOException ioe) {
            //Other issues with the xml file
            Log.e(TAG, "Problem parsing file: ", ioe);
            return false;
        }

        //Return true if there were no issues
        return true;
    }
}
